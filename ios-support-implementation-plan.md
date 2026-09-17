# Implementation Plan: iOS Target Support (Compile & Run on Physical iPhone)

> **Spec Source:** User request (conversation, 2026-09-17) — add a working iOS target to the KMP project as the first concrete step of the "Proficient" tier KMP training objective (Multiplatform build configuration + Publishing of shared code in monorepo), scoped narrowly to "compiles for iOS and runs on my physical iPhone." Informed by the user's own research doc `kmp-publishing-shared-code-monorepo.md` (Engineering Objectives/2026/Objective 5).
> **Plan Created:** 2026-09-17
> **Plan Last Updated:** 2026-09-17 (post-implementation revision — see §0)
> **Estimated Total Effort:** 20-32 developer-hours (revised down from the initial 24-38h estimate now that Xcode and Android Studio are already installed in the VM and Q1-Q4 are resolved — see §11; the weighting shifted from Phase 4 toward Phase 1/3 after a clarification: the user's prior stuck point with a different KMP project was actually a failure to compile/run for the **iOS Simulator** at all, due to that project's own library/config incompatibilities — the user never got as far as attempting physical-device signing or USB passthrough. This makes R-1's early spike (T-002) the single most load-bearing risk mitigation in this plan — see Risk R-1). **Post-implementation note:** R-1 materialized almost immediately (see §0) and cost considerably more than its 1-2h budget once the full remediation — a Kotlin/Compose version cascade plus an abandoned kmpgen-downgrade attempt — is counted; §0 tracks real effort separately from this original estimate rather than silently revising the number.
> **Recommended Team Size:** 1 engineer (solo/learning project) — no QA role, self-verified via manual run on a physical iPhone (iOS 18)

---

## Table of Contents

0. [Implementation Status](#0-implementation-status)
1. [Executive Summary](#1-executive-summary)
2. [Specification Analysis](#2-specification-analysis)
3. [Architecture & Design Decisions](#3-architecture--design-decisions)
4. [Required Skills & Resources](#4-required-skills--resources)
5. [Phase Breakdown](#5-phase-breakdown)
6. [Detailed Task List](#6-detailed-task-list)
7. [Dependency Map](#7-dependency-map)
8. [Risk Assessment](#8-risk-assessment)
9. [Testing Strategy](#9-testing-strategy)
10. [Definition of Done](#10-definition-of-done)
11. [Open Questions](#11-open-questions)

---

## 0. Implementation Status

*(Added retroactively on 2026-09-17 after real implementation work — kept separate from the original plan text below so the plan still reads as it did when written; treat this section as the current source of truth where it conflicts with anything below.)*

**Done, deviating from plan:**
- `iosArm64()` target added and compiling clean (`./gradlew :composeApp:compileKotlinIosArm64` → `BUILD SUCCESSFUL`). **`iosX64()` was added, attempted, and then dropped entirely** — see D1 (revised) below. This is a bigger scope change than it sounds: it removes the iOS Simulator entirely as an option for this project (not just "no `iosX64`, use `iosSimulatorArm64` instead" — that target was never viable here either, since the VM is Intel). Phase 3's whole "prove the pipeline on the Simulator before touching physical-device signing" strategy (T-013) is no longer executable. See the new Risk R-8 below.
- `kmpgen` (Risk R-1) did materialize, exactly as flagged, but the failure mode and fix were more involved than the plan's mitigation anticipated:
  - The `kmpgen` 1.5.0 `companion` klib was built with a newer Kotlin compiler (ABI 2.4.0) than the project's pinned Kotlin 2.2.21 could read. Mitigation option A (downgrade `kmpgen` to 1.0.2, which targets Kotlin 2.2.21) was attempted and **failed differently**: AGP 9's `com.android.kotlin.multiplatform.library` plugin threw an NPE inside `kmpgen` 1.0.2's `KgenPlugin.configureAndroid` — that old `kmpgen` release predates AGP 9's Android Components API surface. Abandoned.
  - Went with option B instead: bumped `kotlin` 2.2.21→2.4.0, `composeMultiplatform` 1.6.10→1.12.0, `composeHotReload` →1.2.0 to match what `kmpgen` 1.5.0 was actually built against.
  - That cascade broke `org.jetbrains.compose.material:material-icons-extended`, which is permanently frozen at `1.7.3` upstream (deprecated in favor of "Material Symbols") — it doesn't track the main Compose Multiplatform version train. Fixed by unlinking its `version.ref` and pinning a literal `"1.7.3"` in `gradle/libs.versions.toml`.
  - `ApiMappers.kt` used JVM-only `java.util.Locale.getDefault()` inside `toLowerCaseAndCapital` — invisible on Android/JVM targets, but broke Kotlin/Native compilation (`Unresolved reference 'java'`). Fixed by switching to the common-Kotlin no-arg `lowercase()`/`titlecase()`.
- **New, not anticipated by any FR/task in this plan:** `Logger`'s `expect fun platformLog(...)` (`utils/log/LoggerImpl.kt`) had no `iosMain` actual — this plan's FR list only ever tracked the dead `httpClientEngine()` expect/actual (D5/FR-7), not `Logger`'s. Added `composeApp/src/iosMain/kotlin/com/example/rickandmorty/utils/log/LoggerIos.kt` using `platform.Foundation.NSLog`. See D9 below.
- **New, not anticipated by NFR-3:** adding `iosArm64()` caused Gradle to also wire `commonTest`'s JVM/JUnit5/MockK dependencies into the new `iosArm64Test`/`iosTest`/`appleTest` source sets (KMP's default source-set hierarchy), which cannot resolve those JVM-only libraries for Kotlin/Native — 15 sync errors (`kotlin-test-junit5`, `junit-jupiter-params`, `mockk` all "Could not resolve" for Apple targets). NFR-3's letter held (no test was ported to *run* on Kotlin/Native), but its spirit — "adding iOS targets doesn't touch `commonTest`" — didn't survive contact with reality. Fixed by relocating the entire test source set from `commonTest` to `jvmTest` (all 14 files, `git mv`, same relative paths) and moving the dependency block accordingly. See D10 below. `CLAUDE.md` has been updated to reflect this.
- Dead `httpClientEngine()` expect/actual (D5/FR-7) deleted as planned: `HttpClientFactory.kt` (commonMain) + both `HttpClient.kt` actuals (androidMain, jvmMain).

**Not started yet:** Phase 2 (`binaries.framework {}`, `MainViewController.kt`), Phase 3 (Xcode project, `iosApp/`), Phase 4 (signing, on-device run), Phase 5 (final cleanup docs — partially done ahead of schedule, since `CLAUDE.md` was already updated for the `jvmTest` move and the iOS target above).

**Net effect on the plan below:** treat every iosX64/Simulator reference in §§1-10 as historical (what was planned, not what happened) — D1, Phase 1, Phase 3, the task table, the risk table, and the Definition of Done are annotated inline where they're now inaccurate, rather than rewritten wholesale, so this document still shows its own reasoning trail.

---

## 1. Executive Summary

Today `composeApp`'s `kotlin {}` block declares only `android()` and `jvm()` targets — there is no `iosArm64`/`iosX64` target, no `iosMain` source set, no `iosApp/` Xcode project, and no CocoaPods/SPM wiring anywhere in the repo. This plan adds a third platform, iOS, end-to-end: two Kotlin/Native targets (`iosArm64` for the physical iPhone, `iosX64` for the Intel simulator — chosen specifically because the user's only macOS environment is an **Intel-based VM**, not Apple Silicon, so `iosSimulatorArm64` is irrelevant), a minimal Compose-Multiplatform-standard `MainViewController.kt` entry point analogous to the existing `MainActivity.kt`/`main.kt` platform shells, an `iosMain` Ktor Darwin engine dependency, and a hand-created SwiftUI Xcode project (`iosApp/`) that consumes the Kotlin framework via **direct Gradle-backed local/source distribution** (a `binaries.framework {}` block + an Xcode "Run Script" build phase calling `embedAndSignAppleFrameworkForXcode`) rather than CocoaPods or a published artifact — matching the user's own research doc, which recommends local/source distribution as the correct default for a single-developer monorepo, reserving remote/artifact publishing (Maven Local, XCFramework via SPM) as an explicit later phase that isn't required for this plan's goal. No changes are needed to `domain`/`presentation` layers — ViewModels are already plain Kotlin/`StateFlow` classes with no Android or JVM dependency, so they should port to iOS unmodified; the real unknowns are entirely in build configuration, Xcode project mechanics, and — because the user has never shipped an app to a physical iPhone before and has no real Mac — Apple code-signing/provisioning and Intel-VM toolchain setup, which this plan treats as first-class risk, not an afterthought.

---

## 2. Specification Analysis

### 2.1 Functional Requirements

| ID | Requirement | Source | Priority |
|----|-------------|--------|----------|
| FR-1 | `composeApp` gains `iosArm64` and `iosX64` Kotlin/Native targets, each producing a `RickAndMortyKMP.framework` (or similar) via `binaries.framework {}` | User spec | P0 |
| ~~FR-1 (revised)~~ | **`iosX64` dropped.** `iosArm64` only — neither `kmpgen`'s `companion` klib nor Compose Multiplatform 1.12.0's `runtime`/`foundation`/`ui` artifacts publish an `iosX64` Gradle variant (confirmed via "No matching variant" resolution errors listing every published variant — `iosArm64*`, `iosSimulatorArm64*`, android, jvm, linuxX64, macosArm64, mingwX64, common metadata — with `iosX64` absent from all of them). This is an upstream ecosystem gap (the toolchain has moved on to Apple-Silicon-only simulators), not a config mistake to fix. See §0, D1 (revised). | Implementation finding, 2026-09-17 | — |
| FR-8 (new) | `Logger`'s `expect fun platformLog(...)` gains an `iosMain` actual (`NSLog`-based) | Implementation finding, 2026-09-17 — not anticipated by the original spec, which only tracked `httpClientEngine()` (FR-7) | P0 |
| FR-2 | A `iosMain` source set exists with an `expect`/`actual`-style `MainViewController()` (or equivalent) that hosts the existing shared `App()` composable via `ComposeUIViewController` | User spec, implied by existing Android/JVM entry-point pattern | P0 |
| FR-3 | Networking (`ApiService`/generated `kmpgen` clients) functions on iOS — Ktor is configured with the Darwin engine for `iosMain`, no code changes needed in `commonMain` | User spec | P0 |
| FR-4 | A real Xcode project/workspace (`iosApp/`) exists, embeds the Kotlin framework via a Run Script build phase (not CocoaPods), and launches a minimal SwiftUI shell hosting the Compose UI | User spec | P0 |
| FR-5 | The app builds, installs, and launches on the user's physical iPhone (iOS 18) via Xcode running inside an Intel macOS VM, with code signing configured for on-device debug run | User spec | P0 |
| FR-6 | All three existing screens (Character, Location, Episode list) are reachable and functional on iOS, fetching real data from the Rick and Morty API, at the same functional bar as Android/Desktop | User spec | P0 |
| FR-7 | The dead/unreferenced `expect fun httpClientEngine()` (`HttpClientFactory.kt` + its `androidMain`/`jvmMain` actuals) is either deleted or given an iOS actual — decided explicitly, not left ambiguous | Derived from current repo state | P1 |

### 2.2 Non-Functional Requirements

| ID | Requirement | Target | Measurement |
|----|-------------|--------|-------------|
| NFR-1 | No changes to `domain`/`presentation`/`data` business logic beyond what's strictly required for iOS compilation (no new DI framework, no premature abstraction) | Zero unrelated diffs in `domain`/`presentation` | Code review against CLAUDE.md conventions |
| NFR-2 | Existing Android and Desktop targets keep building and running unmodified after this change | `./gradlew :androidApp:assembleDebug` and `:composeApp:run` still succeed | Manual re-run after each phase touching `build.gradle.kts` |
| NFR-3 | `./gradlew :composeApp:jvmTest` (the only test task, JVM-executed) is unaffected — adding Kotlin/Native targets must not require porting `commonTest` to run on Kotlin/Native | Test task still passes exactly as before | `./gradlew :composeApp:jvmTest` after target changes |
| ~~NFR-3 (revised)~~ | Held in letter (no test runs on Kotlin/Native) but not in spirit — adding `iosArm64()` forced the entire test source set to relocate from `commonTest` to `jvmTest`, since KMP's default source-set hierarchy wires `commonTest`'s dependencies into every target's test compilation, including `iosArm64Test`/`iosTest`/`appleTest`, which can't resolve JVM-only JUnit5/MockK. See §0, D10. | Implementation finding, 2026-09-17 | `./gradlew :composeApp:jvmTest` still green after the relocation |
| NFR-4 | Signing/provisioning setup is the minimum needed to run one debug build on one physical device — no distribution certificate, no TestFlight, no App Store metadata | Xcode "automatically manage signing" + free/paid Apple ID, nothing beyond | Manual verification during Phase 4 |
| NFR-5 | The iOS build path uses local/source distribution (Xcode → Gradle framework build, live), not a versioned/published artifact, per the research doc's recommendation for a single-developer monorepo | No `publishToMavenLocal`/CocoaPods podspec/SPM manifest introduced in this plan | Code review of `build.gradle.kts` diff |

### 2.3 Business Rules & Constraints

- This is a solo, self-directed learning project (CLAUDE.md) — no team coordination, no CI, no release process; guidance should stay proportionate (skip anything that only matters for teams or App Store distribution).
- Per CLAUDE.md: don't introduce a DI framework (e.g. Koin) just to support a third platform — the existing plain-singleton `Module` object pattern must be reused as-is; `Module.kt` lives in `commonMain` already and needs no changes.
- The user has **no physical Mac** — only an Intel (non-Apple-Silicon) macOS VM. This rules out `iosSimulatorArm64` entirely and shapes every toolchain/signing step in Phases 3-4.
- The target device is iOS 18 on a real iPhone — the plan must not stop at "builds a framework," it must reach an actual on-device launch.
- Per the user's own research doc §2/§4/§7: local/source distribution is the correct choice here (single dev, monorepo, active iteration) — remote/artifact publishing is explicitly deferred, not part of this plan's Definition of Done.

### 2.4 Ambiguities & Gaps

- **VM ↔ repo access mechanism** — unclear whether the Intel macOS VM will access this exact Git working copy via a shared folder/mount, or via its own `git clone` of the same repository. This changes nothing about the Kotlin/Gradle work but materially affects Phase 3 setup steps. **Resolved (§11 Q1):** the user will do an independent `git clone` inside the VM — T-010 and its estimate are adjusted accordingly, and the plan now calls out the manual sync discipline this requires.
- **Free vs. paid Apple Developer account** — blocks the exact signing flow in Phase 4 (free Apple ID gives a 7-day-expiring provisioning profile that must be re-signed weekly; a paid $99/year account removes that limit). **Resolved (§11 Q2):** the user will use the free Apple ID — Phase 4 tasks assume the 7-day provisioning refresh cycle, not Developer Program enrollment/payment, and the Q2-related enrollment-delay dependency in §7.3 no longer applies.
- **`kmpgen` iOS/Kotlin-Native compatibility** — the `com.kroegerama.openapi-kmp-gen` plugin's generated code has, per CLAUDE.md, only ever been exercised on Android/JVM targets in this project. Ktor itself is fully multiplatform and the generated client code is expected to land in a target-agnostic source set, but this has **not been verified against a real Kotlin/Native compile** in this codebase before. Treated as a resolvable technical risk (R-1) with an early spike task (T-002), not an ambiguity that blocks planning — this one is not resolved by user decision, only by running T-002.
- **VM hypervisor / USB passthrough capability** — which virtualization software hosts the Intel macOS VM (VMware, VirtualBox, QEMU, UTM, etc.) determines whether the physical iPhone can be USB-passed-through directly, or whether Xcode's network-device-debugging fallback is needed instead. **Resolved (§11 Q3):** the hypervisor is QEMU — Risk R-3's mitigation below is sharpened with QEMU-specific guidance instead of staying generic. Note this is genuinely untested territory: per the user's clarification, their prior KMP attempt never reached the USB-passthrough/device-signing stage at all (it stalled earlier, at the iOS Simulator build itself), so R-3 carries no negative precedent — just the ordinary uncertainty of a first attempt.

---

## 3. Architecture & Design Decisions

### 3.1 High-Level Architecture

```
iosApp/ (Xcode project, SwiftUI)
   │  Run Script build phase → ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ▼
composeApp iosArm64/iosX64 binaries.framework { baseName = "ComposeApp" }
   │
   ▼
iosMain/MainViewController.kt → ComposeUIViewController { App() }   (mirrors MainActivity.kt / main.kt)
   │
   ▼
commonMain: App() → NavigationHost() → Character/Location/Episode screens
   │                                        │
   │                                        ▼
   │                              ViewModels (StateFlow, unmodified)
   │                                        │
   ▼                                        ▼
iosMain: HttpClient(Darwin)  ←──── di/Module.kt (commonMain, unmodified) ──── UseCase → Repository → ApiService
                                             │
                                             ▼
                              kmpgen-generated CharacterApi/LocationApi/EpisodeApi
                              (commonMain, Ktor-based — target-agnostic in principle)
```

No new modules, no new layers. iOS slots in as a third leaf alongside `androidMain`/`jvmMain`, consuming the exact same `commonMain` business logic. The only new "layer" is the Xcode project itself, which is intentionally kept minimal (a single SwiftUI `App` + `ContentView` wrapping the Kotlin-generated `UIViewController`) — no SwiftUI screens of substance are built, since all UI already lives in Compose.

### 3.2 Key Technical Decisions

| Decision | Recommendation | Rationale | Alternatives Considered |
|----------|---------------|-----------|--------------------------|
| D1: Which iOS targets to add | `iosArm64()` (physical device) + `iosX64()` (Intel simulator) | Matches the user's actual hardware: an Intel macOS VM cannot run `iosSimulatorArm64` (Apple-Silicon-only simulator ABI), and the goal explicitly includes running on a real iPhone (`iosArm64`) | `iosSimulatorArm64()` — rejected outright, incompatible with an Intel VM; adding all three targets "for completeness" — rejected as premature per CLAUDE.md, `iosX64` already covers "test in a simulator without the physical device attached" |
| **D1 (revised, 2026-09-17):** `iosX64` dropped after implementation | `iosArm64()` only | `iosX64()` was added exactly as planned, but `compileKotlinIosX64`/sync failed with "No matching variant" for `kmpgen`'s `companion` klib and for `compose.runtime`/`compose.foundation`/`compose.ui` 1.12.0 — none of them publish an `iosX64` variant anymore. This isn't fixable by a version bump; the ecosystem has moved on to `iosSimulatorArm64`-only simulator support, which this project's Intel VM can't use either. **Net result: no iOS Simulator path exists for this project at all** — every remaining phase's "verify on Simulator first" step (Phase 3/T-013) is dead and Phase 4 becomes the only place iOS UI gets verified at all. See Risk R-8. | Keeping `iosX64` — impossible, not a tradeoff; `iosSimulatorArm64()` — also impossible on this Intel VM |
| D2: Framework export mechanism | Direct Kotlin Gradle DSL: `listOf(iosArm64(), iosX64()).forEach { it.binaries.framework { baseName = "ComposeApp" } }`, embedded into Xcode via the standard `embedAndSignAppleFrameworkForXcode` Gradle task wired into an Xcode "Run Script" build phase | This *is* the mechanical implementation of the research doc's "local/source distribution" recommendation (§2 Option A) — Xcode calls into Gradle live, no versioning, no publish step; it's also strictly less setup than CocoaPods (no Ruby/CocoaPods toolchain to install inside an already-constrained Intel VM) | CocoaPods Gradle plugin (`org.jetbrains.kotlin.native.cocoapods`) — rejected for now: adds a Ruby/CocoaPods dependency to install/debug inside the VM for no benefit at this stage (single module, no multi-pod dependency graph, no team members needing Podfile-based version pinning); can be revisited if/when the remote-publishing stretch phase happens |
| D3: iOS entry point shape | `iosMain/kotlin/.../MainViewController.kt` exposing `fun MainViewController() = ComposeUIViewController { App() }`, called from Swift's `ContentViewController.swift`/`iOSApp.swift` | Mirrors the existing pattern exactly: `MainActivity.kt` and `main.kt` are both thin, target-specific shells that call the same shared `App()` — no new `expect`/`actual` boundary is even needed here since this function only exists in `iosMain`, not shared logic | Introducing an `expect fun platformEntryPoint()` spanning all 3 platforms — rejected, over-engineering; Android/JVM entry points are already irreducibly different in shape (Activity lifecycle vs. `application {}` vs. `UIViewController`), so a shared abstraction buys nothing |
| D4: Ktor engine for iOS | Add `implementation(libs.ktor.client.darwin)` to `iosMain.dependencies` | `ktor-client-darwin` is **already declared (but unused) in `gradle/libs.versions.toml`** — confirms this was anticipated; Darwin is Ktor's standard/only supported engine for iOS/macOS/watchOS/tvOS targets | `ktor-client-ios` (deprecated legacy artifact, superseded by Darwin) — rejected, obsolete |
| D5: Fate of the dead `expect fun httpClientEngine()` | **Delete** `HttpClientFactory.kt` and its `androidMain`/`jvmMain` `HttpClient.kt` actuals entirely, rather than adding a third (iOS) actual | Per CLAUDE.md, this code is already confirmed unreferenced (`Api`'s generated client manages its own engine internally) — extending dead code to a third platform is the wrong direction; deleting fully-unused code is explicitly preferred over backwards-compat shims | Adding an iOS `actual fun httpClientEngine(): HttpClientEngine = Darwin.create()` for "symmetry" — rejected, would be dead code on all 3 platforms, not just 2 |
| D6: `kmpgen` iOS compatibility handling | Treat as a **spike task early in Phase 1** (T-002: add the target, attempt `:composeApp:compileKotlinIosArm64` before building anything else) rather than assuming it works | The generated OpenAPI client has never been compiled against a Kotlin/Native target in this project; failing fast here (before Xcode/VM work) avoids sinking hours into signing/provisioning only to discover a codegen incompatibility | Assuming compatibility and discovering issues only in Phase 4 (on-device run) — rejected, would waste VM/signing setup time on a broken foundation |
| **D6 (outcome, 2026-09-17):** the spike was the right call, but the incompatibility was a KLIB ABI mismatch, not a code-level one | Bumped `kotlin`/`composeMultiplatform`/`composeHotReload` to match what `kmpgen` 1.5.0 was built against (2.4.0/1.12.0/1.2.0), after an abandoned attempt to downgrade `kmpgen` to 1.0.2 instead (hit an unrelated AGP9 incompatibility — NPE in `KgenPlugin.configureAndroid`) | Confirms D6's core premise (fail fast, before Xcode/VM work) worked exactly as intended — this was caught in minutes via T-002, not hours into Phase 4 — but the actual fix needed was a version-cascade decision, not a code fix, and it surfaced a second, unrelated incompatibility (kmpgen-1.0.2 × AGP9) that the original risk description didn't anticipate | Downgrading `kmpgen` (Option A) — tried, abandoned once AGP9 broke it; hand-writing a Ktor client bypassing `kmpgen` for iOS only — not needed, the version bump resolved it cleanly |
| D7: Xcode project structure/location | `iosApp/` at repo root (JetBrains' own Compose Multiplatform wizard convention: `iosApp/iosApp.xcodeproj`, `iosApp/iosApp/iOSApp.swift`, `iosApp/Configuration/` for shared build settings) | Matches the ecosystem-standard layout so any future JetBrains tooling/docs/templates the user consults line up with what's in the repo; keeps the Xcode project a sibling of `composeApp`/`androidApp`, consistent with the existing "each platform gets its own top-level module-like folder" pattern | Nesting the Xcode project inside `composeApp/` — rejected, Xcode projects expect to sit outside the Gradle module they consume, and every reference implementation uses a sibling folder |
| D8: Remote/artifact publishing scope | **Explicitly out of scope for this plan** — Maven Local publish, XCFramework + SPM/CocoaPods distribution, and any `maven-publish` wiring are deferred to a later, separate plan once local/source distribution is proven working end-to-end | Directly follows the research doc's own hybrid-flow guidance (§4): prove the local flow first, publish later only if/when the "publishing" competency needs to be explicitly demonstrated | Doing both in one pass — rejected, conflates two different learning objectives and roughly doubles this plan's risk surface for no benefit toward "compiles and runs on my iPhone" |
| **D9 (new, 2026-09-17): `Logger` iOS actual** | `composeApp/src/iosMain/.../LoggerIos.kt` — `actual fun platformLog(...)` using `platform.Foundation.NSLog` | Not tracked by any FR in the original spec (only `httpClientEngine()`'s expect/actual was on the radar); discovered when `compileKotlinIosArm64` failed with "Expected platformLog has no actual declaration ... for Native" — every `expect` needs an actual per target, and `Logger`'s was missed because Android/JVM already had theirs | A no-op/println-based actual — rejected, `NSLog` is the idiomatic Kotlin/Native binding and costs nothing extra |
| **D10 (new, 2026-09-17): relocate tests from `commonTest` to `jvmTest`** | `git mv` all 14 files from `composeApp/src/commonTest/...` to `composeApp/src/jvmTest/...` (same relative paths); move the JUnit5/MockK dependency block from `commonTest.dependencies` to `jvmTest.dependencies` | Adding `iosArm64()` made Gradle try to resolve `commonTest`'s JVM-only deps for the new `iosArm64Test`/`iosTest`/`appleTest` source sets too (KMP's default hierarchy), which fails outright — JUnit5/MockK have no Kotlin/Native artifacts. Relocating out of `commonTest` entirely (rather than leaving it there and hoping iOS ignores it) is the only fix, since the test *code* itself uses JUnit5-only APIs (`@ParameterizedTest`) that can't compile for Native either | Leaving deps in `commonTest` and adding target exclusions — not a supported Gradle/KMP mechanism for this; porting the actual tests to run on Kotlin/Native (`kotlin.test` only, no JUnit5/MockK) — rejected, a much larger effort explicitly out of scope (§9.1), and orthogonal to what broke |

### 3.3 Data Model Changes

None. No new domain models, no schema, no persistence. This is purely a build-configuration + platform-shell change; `domain`/`data` layers are consumed as-is.

### 3.4 Integration Points

| System | Direction | Protocol | Auth | Notes |
|--------|-----------|----------|------|-------|
| Rick and Morty public API (`rickandmortyapi.com`) | Outbound (app → API) | HTTPS / REST via Ktor (generated `kmpgen` client, now also running under the Darwin engine on iOS) | None (public, unauthenticated) | No endpoint/contract changes — only a new HTTP engine backing the same generated client calls |
| Apple Developer / code signing | Local tooling (Xcode ↔ Apple ID) | Xcode automatic signing, on-device provisioning profile | Apple ID (free or paid Developer Program) | New integration point introduced by this plan — see Phase 4, D-signing decisions in Open Questions Q2 |

---

## 4. Required Skills & Resources

### 4.1 Technical Skills

| Skill Area | Specific Expertise | Phases Needed | Criticality |
|------------|--------------------|----------------|-------------|
| Kotlin Multiplatform / Kotlin-Native | Adding `iosArm64`/`iosX64` targets, `binaries.framework {}` config, `iosMain` source set wiring in Gradle Kotlin DSL | Phase 1 | Critical |
| Compose Multiplatform (iOS) | `ComposeUIViewController`, iOS-specific Compose resource/theming quirks (status bar, safe area) | Phase 2 | Critical |
| Ktor Multiplatform | Darwin engine setup, confirming generated-client compatibility across engines | Phase 1 | Medium |
| Xcode / iOS project mechanics | Creating an Xcode project from scratch, Run Script build phases, target/scheme configuration, Info.plist basics | Phase 3 | Critical (new to user) |
| Apple code signing & provisioning | Apple ID enrollment, automatic signing, provisioning profiles, trusting a dev cert on-device | Phase 4 | Critical (new to user) |
| macOS/VM administration | Installing Xcode + Command Line Tools inside an Intel macOS VM, USB/network device passthrough for a physical iPhone | Phase 3 | High (environment-specific, largely outside Kotlin expertise) |
| Swift (minimal) | Reading/lightly editing the generated `iOSApp.swift`/`ContentView.swift` — no substantial Swift code needed | Phase 3 | Low |

### 4.2 Domain Knowledge

- No business-domain knowledge beyond what's already established (public read-only API). This is a pure platform/tooling expansion.
- Apple's device-registration and provisioning-profile model is the one genuinely new "domain" here for the user — treated as a first-class learning objective in Phase 4, not a footnote.

### 4.3 Tools & Infrastructure

| Tool/Service | Purpose | Already Available? | Setup Effort |
|--------------|---------|---------------------|----------------|
| Intel macOS VM (hypervisor: **QEMU**, confirmed §11 Q3) | Host for Xcode, only way to build/sign iOS apps | Yes (already installed per user) | Unknown — depends on current VM state/resources; verify it has enough disk/RAM for Xcode (typically 15-40 GB) |
| Xcode | Build, sign, and deploy the iOS app; only tool that can produce a signed on-device binary | **Already installed** in the VM, left over from the user's prior (unfinished) KMP attempt | Low — just verify it's recent enough for the iOS 18 SDK (`xcodebuild -version`/`-showsdks`, see R-2); not a fresh multi-GB install |
| Android Studio | Not required by this plan (all iOS work is Gradle/Xcode-driven), but confirmed already installed in the same VM from the prior attempt | Yes | None — noted only as evidence the VM is already a capable KMP dev environment |
| Apple ID (**free tier**, confirmed §11 Q2) | Required for any code signing, even for local debug-only device runs | Assumed available (user has an Apple ID for their iPhone) | Low — no Developer Program enrollment, payment, or approval wait; the tradeoff accepted is the 7-day provisioning-profile refresh cycle (NFR-4) |
| Physical iPhone (iOS 18) running in Developer Mode | Deployment target | Yes | Low — enabling "Developer Mode" under Settings > Privacy & Security is a one-time toggle on iOS 16+ |
| `ktor-client-darwin` | HTTP engine for iOS | Declared in `libs.versions.toml` already, just unused | None — just add the dependency line |
| Git access to this repo from inside the VM | So Xcode/Gradle can build against the same source | **Resolved (§11 Q1):** the user will do an independent `git clone` inside the VM | Low to set up, but requires an ongoing manual sync discipline between the host and VM clones during iterative Phase 3-4 debugging (see T-010) |

---

## 5. Phase Breakdown

### Phase 1: Kotlin/Gradle Foundation — iOS Targets Compile (4-6 hours) — ✅ DONE (2026-09-17, `iosArm64` only)
**Goal:** ~~`composeApp` declares `iosArm64`/`iosX64` targets~~ **`composeApp` declares an `iosArm64` target** (revised — `iosX64` dropped, see D1 revised in §0/§3.2) with an `iosMain` source set, Darwin Ktor engine wired, and the module compiles cleanly from the Linux host (no Xcode/VM needed yet — Kotlin/Native cross-compilation to `.klib`/object files works from Linux; only final framework-signing/linking needs a Mac).
**Deliverable:** ~~`./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosX64` succeeds~~ **`./gradlew :composeApp:compileKotlinIosArm64` succeeds (confirmed `BUILD SUCCESSFUL`); `compileKotlinIosX64` is not attempted anymore — it cannot succeed (D1 revised).** `git diff` ended up touching more than `build.gradle.kts` + `iosMain`/dead-code files as originally scoped — also `gradle/libs.versions.toml` (Kotlin/Compose version bumps, D6 outcome), `ApiMappers.kt` (Locale fix), and the entire test source set relocation (D10).
**Prerequisites:** None.

### Phase 2: iOS Entry Point & UI Smoke Path (3-5 hours) — NOT STARTED
**Goal:** A minimal `MainViewController.kt` hosts the shared `App()` composable; `binaries.framework {}` is configured and produces a `.framework` artifact via Gradle alone (still no Xcode yet).
**Deliverable:** ~~`./gradlew :composeApp:linkDebugFrameworkIosArm64` (and `IosX64`)~~ **`./gradlew :composeApp:linkDebugFrameworkIosArm64`** produces `composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework` (or equivalent path) containing the expected public API (`MainViewControllerKt` visible from Swift).
**Prerequisites:** Phase 1 complete. ✅

### Phase 3: Xcode Project & VM Toolchain (5-8 hours — reduced from the original 8-14h now that Xcode is already installed; see Risk R-4) — NOT STARTED; **goal changed, see below**
**Goal:** ~~A working `iosApp/` Xcode project exists, embeds the Gradle-built framework via a Run Script phase, and successfully launches on the **iOS Simulator** (Intel, `iosX64`) inside the VM — proving the whole pipeline before tackling physical-device signing.~~ **Revised (D1 revised, §0): there is no iOS Simulator path for this project anymore — `iosX64` is gone and `iosSimulatorArm64` was never viable on this Intel VM.** The goal is now: a working `iosApp/` Xcode project exists and embeds the Gradle-built `iosArm64` framework via a Run Script phase — verified by attempting a build (not necessarily a run) targeting the physical device directly, since no simulator target can even be selected in Xcode's scheme picker. **This removes the plan's original "prove the pipeline cheaply on a simulator before risking device signing" safety net entirely — see new Risk R-8.**
**Deliverable:** ~~App launches in the iOS Simulator inside the VM, shows the shared UI, and can navigate to at least one list screen with live data.~~ **`iosApp` Xcode project builds successfully against the `iosArm64` framework (device destination only) — first actual UI verification now happens in Phase 4, on the real device, not here.**
**Prerequisites:** Phase 2 complete; Xcode version confirmed to support the iOS 18 SDK (T-009); VM has its own `git clone` of the repo (§11 Q1).

### Phase 4: Code Signing & On-Device Run (6-11 hours — first-time signing, budget generously; see Risk R-5)
**Goal:** The app is signed with the free Apple ID, the physical iPhone trusts the dev certificate, and the app launches and is fully navigable on the real device — ideally over USB via QEMU passthrough, falling back to Wi-Fi debugging if that doesn't cooperate (§11 Q3, Risk R-3). This is genuinely new territory for the user — on a prior KMP project they stopped before ever reaching USB passthrough or device signing (that attempt stalled earlier, at getting the iOS Simulator to build at all). Precisely because there's no personal precedent to lean on here, this phase is deliberately broken into small, independently-verifiable checkpoints — confirm the VM can see the device at all, *then* configure signing, *then* attempt the full build+run — rather than one blind end-to-end attempt.
**Deliverable:** App runs on the physical iPhone (iOS 18), all three list screens (Character/Location/Episode) load real API data, matching Android/Desktop functional parity.
**Prerequisites:** Phase 3 complete (simulator run proven); free Apple ID signed into Xcode; iPhone in Developer Mode.

### Phase 5: Cleanup & Documentation (2-3 hours)
**Goal:** Remove dead code (D5), update `CLAUDE.md`'s "Current Engineering Focus"/"Platform split" sections to reflect the new iOS target and entry point, confirm Android/Desktop are unaffected.
**Deliverable:** `CLAUDE.md` accurately describes 3 platforms; `androidApp:assembleDebug` and `composeApp:run` (Desktop) both still succeed; `composeApp:jvmTest` still passes.
**Prerequisites:** Phase 4 complete (or at minimum Phase 3, if device signing is deferred — see Risk R-6).

> Remote/artifact publishing (Maven Local, XCFramework/SPM distribution), ObjC/Swift interop beyond the minimal shell, SKIE integration, and CI for iOS are explicitly **not phased here** (D8) — candidates for a follow-up plan once this one's Definition of Done is met.

---

## 6. Detailed Task List

| ID | Task | Description | Phase | Priority | Estimate | Dependencies | Skills |
|----|------|-------------|-------|----------|----------|---------------|--------|
| T-001 | ✅ DONE (revised scope) | ~~Add `iosArm64()`/`iosX64()` targets~~ Added `iosArm64()` only — `iosX64()` was added, hit "No matching variant" for `kmpgen`/Compose artifacts, and was removed (D1 revised) | 1 | P0 | 1 hour (actual: ~same, plus the extra iosX64 removal step) | None | KMP/Gradle |
| T-002 | ✅ DONE (took much longer than budgeted) | Ran `./gradlew :composeApp:compileKotlinIosArm64` per D6; hit a KLIB ABI mismatch (kmpgen 1.5.0 companion vs. Kotlin 2.2.21), not the anticipated "generated-code-level" incompatibility. Resolved via a Kotlin 2.4.0/Compose MP 1.12.0/composeHotReload 1.2.0 version bump, after an abandoned attempt to downgrade `kmpgen` instead (AGP9 NPE — see D6 outcome, §3.2) | 1 | P0 | 1-2 hours estimated → **actual: several hours across two failed/one successful remediation path** | T-001 | KMP/Gradle, Ktor |
| T-003 | ✅ DONE | `iosMain.dependencies { implementation(libs.ktor.client.darwin) }` | 1 | P0 | 0.5 hour | T-002 | Ktor Multiplatform |
| T-004 | ✅ DONE | Removed `HttpClientFactory.kt` (commonMain) and both `HttpClient.kt` actuals (androidMain, jvmMain) per D5 | 1 | P1 | 0.5 hour | None (independent) | Kotlin |
| T-004b (new) | ✅ DONE | Added `iosMain/.../LoggerIos.kt` (`Logger` actual, D9) — not in the original task list, discovered via a compile failure after T-002 | 1 | P0 | ~0.5 hour | T-002 | Kotlin/Native |
| T-004c (new) | ✅ DONE | Fixed `ApiMappers.kt`'s JVM-only `java.util.Locale` usage (common-Kotlin `lowercase()`/`titlecase()`) — not in the original task list | 1 | P0 | ~0.25 hour | T-002 | Kotlin |
| T-004d (new) | ✅ DONE | Relocated `commonTest` → `jvmTest` (14 files + dependency block, D10) — not in the original task list, triggered by T-001 | 1 | P0 | ~1 hour | T-001 | Gradle, KMP |
| T-005 | ✅ DONE | `./gradlew :androidApp:assembleDebug`, `:composeApp:run`, `:composeApp:jvmTest` all still pass after Phase 1 changes | 1 | P0 | 0.5 hour | T-001, T-003, T-004, T-004b, T-004c, T-004d | Gradle |
| T-006 | NOT STARTED | ~~Configure `binaries.framework {}` for both iOS targets~~ Configure `binaries.framework {}` for `iosArm64` only | 2 | P0 | 1-2 hours | T-005 ✅ | KMP/Gradle |
| T-007 | NOT STARTED | Create `iosMain/.../MainViewController.kt` — `fun MainViewController() = ComposeUIViewController { App() }`, matching D3 | 2 | P0 | 1-2 hours | T-006 | Compose Multiplatform |
| T-008 | NOT STARTED | ~~Verify framework link task produces a usable `.framework`~~ `./gradlew :composeApp:linkDebugFrameworkIosArm64` only; inspect the output framework's headers for `MainViewControllerKt` | 2 | P0 | 1 hour | T-007 | KMP/Gradle |
| T-009 | Verify the VM's already-installed Xcode supports the iOS 18 SDK | Xcode + Command Line Tools are already present from the user's prior KMP attempt — run `xcodebuild -version`/`xcodebuild -showsdks` inside the VM and compare against the iOS 18 SDK requirement; only reinstall/update if this check fails | 3 | P0 | 0.5-1 hour (verification only — see R-2 for the update-required contingency) | None (parallel with Phase 1-2) | macOS/VM admin |
| T-010 | Set up VM ↔ repo access via `git clone` | Independent `git clone` of this repository inside the VM (confirmed approach, §11 Q1); confirm the VM can build the Gradle project standalone; establish a manual routine (push/pull against the same remote) for keeping the VM clone and the host working copy in sync during iterative Phase 3-4 debugging | 3 | P0 | 1-2 hours | T-009 | macOS/VM admin, Git |
| T-011 | Create `iosApp/` Xcode project (SwiftUI App template) | `iosApp/iosApp.xcodeproj`, minimal `iOSApp.swift` + `ContentView.swift` calling `MainViewController()` via a `UIViewControllerRepresentable` wrapper | 3 | P0 | 2-3 hours | T-010, T-008 | Xcode, Swift (minimal) |
| T-012 | Add Run Script build phase to embed the Gradle framework | Standard Compose Multiplatform snippet invoking `../gradlew :composeApp:embedAndSignAppleFrameworkForXcode` with the Xcode-provided env vars (`SDK_NAME`, `CONFIGURATION`, `ARCHS`) | 3 | P0 | 2-3 hours (first-time debugging build-phase env var issues is common) | T-011 | Xcode, Gradle |
| ~~T-013~~ | **REMOVED — impossible (D1 revised).** ~~Run app on iOS Simulator (Intel, `iosX64`) inside the VM~~ There is no simulator target this project can build for anymore; Phase 3's exit criterion becomes "Xcode project builds for the device destination" instead (see Phase 3 goal above) — first actual app *launch* verification is now T-018/T-019 in Phase 4, with no cheaper dry run beforehand. See Risk R-8. | 3 | — | — | — | — |
| T-014 | Sign in with the free Apple ID in Xcode | Free Apple ID confirmed (§11 Q2) — add the Apple ID under Xcode's Accounts preferences; no Developer Program enrollment, payment, or approval wait needed | 4 | P0 | 0.5 hour | None (can start in parallel with Phase 3) | Apple signing |
| T-015 | Configure automatic signing in Xcode for the `iosApp` target | Select signing team (the free personal team tied to the Apple ID), let Xcode manage the provisioning profile | 4 | P0 | 1-2 hours | T-014, T-012 (T-013 removed — see above) | Apple signing, Xcode |
| T-016 | Enable Developer Mode on the physical iPhone | Settings > Privacy & Security > Developer Mode (iOS 16+ requirement) | 4 | P0 | 0.25 hour | None (parallel) | iOS device admin |
| T-017 | Confirm the VM (QEMU) can see the physical iPhone at all, *before* attempting a full Xcode build+run | Connect the iPhone over USB; inside the VM, check device visibility independently of Xcode first — e.g. `system_profiler SPUSBDataType`, or `idevice_id -l`/`ideviceinfo` if `libimobiledevice` is installed — then register the device in Xcode's Devices & Simulators window. QEMU's USB passthrough for iOS devices is a known friction point (needs an XHCI USB controller configured and the device's vendor/product ID passed through). This is untested territory for the user (their prior KMP attempt never got this far), so isolating "does the VM see the phone" from "does Xcode's full pipeline work" is just good debugging hygiene for a first attempt, not a response to a known prior failure at this specific step | 4 | P0 | 1-4 hours (QEMU USB passthrough for iOS devices can be finicky — see R-3; fall back to Wi-Fi debugging if it doesn't cooperate within ~1 hour of troubleshooting) | T-016, T-015 | macOS/VM admin |
| T-018 | Build & run on physical device; trust developer certificate | First on-device launch will prompt "Untrusted Developer" on the iPhone — resolve via Settings > General > VPN & Device Management | 4 | P0 | 1-2 hours | T-017 | Apple signing, Xcode |
| T-019 | Functional smoke test on-device | Navigate Character → Location → Episode screens, confirm live data loads, no crashes | 4 | P0 | 1 hour | T-018 | Manual QA |
| T-020 | Update `CLAUDE.md` (Platform split, Current Engineering Focus sections) | Reflect new iOS target, `MainViewController.kt`, deleted dead code, mark this objective's Phase 1 done | 5 | P1 | 1 hour | T-019 | Documentation |
| T-021 | Final regression check — Android + Desktop | Re-run `:androidApp:assembleDebug`, `:composeApp:run`, `:composeApp:jvmTest` after all iOS changes | 5 | P0 | 1 hour | T-019 | Gradle, manual QA |

---

## 7. Dependency Map

### 7.1 Critical Path

```
T-001 → T-002 → T-003 → T-005 → T-006 → T-007 → T-008 → [T-010 needs T-009 in parallel] → T-011 → T-012 → T-015 → T-017 → T-018 → T-019 → T-021
(1h)   (1-2h)  (0.5h)  (0.5h)  (1-2h)  (1-2h)  (1h)                                        (2-3h) (2-3h)         (1-2h) (1-3h) (1-2h) (1h)   (1h)
```
**Revised (D1):** `T-013` (Simulator run) dropped from the critical path entirely — it's no longer executable (§0). This *shortens* the nominal critical path by 1-2 hours, but that's a misleading improvement: it also removes the cheapest, lowest-stakes checkpoint in the whole plan, so Phase 3→4 now jumps straight from "Xcode project builds" to "first-ever attempt on the physical device," with no intermediate signal if something in the Compose-Multiplatform-on-iOS or Xcode-integration layer is broken. See Risk R-8.

Total critical path: **≈16-23 hours** (revised down from the original ≈18-27h now that T-009 shrinks to a verification-only check and T-014 no longer risks a Developer Program approval wait; T-017 grew slightly to account for the QEMU-specific device-visibility checkpoint), dominated by Phase 3-4's Xcode/VM/signing work (T-009 through T-018), which is inherently sequential (can't configure a Run Script phase before a project exists; can't sign before a project builds; can't run on-device before signing works). T-014 (Apple ID sign-in) and T-009 (Xcode SDK check) can start immediately and in parallel with Phase 1-2's Kotlin work, shortening wall-clock time if done concurrently rather than strictly in phase order. **Actual time spent so far (Phase 1, §0): well above the original 4-6h Phase 1 budget** once T-002's real remediation cost (version cascade + abandoned kmpgen downgrade) and the three unbudgeted tasks (T-004b/c/d) are counted — see §0 and the Post-implementation note in the header.

### 7.2 Parallelizable Work

- **T-009 (Xcode SDK check) and T-014 (Apple ID sign-in)** can both start on day one, in parallel with all of Phase 1-2's Kotlin/Gradle work — they have no Kotlin-side dependency.
- **T-004 (delete dead code)** is fully independent and can happen any time after the codebase is understood, even before T-001.
- **T-016 (enable Developer Mode on iPhone)** is a 15-second task independent of everything else and should just be done immediately.

### 7.3 External Dependencies

| Dependency | Owner | Status | Impact if Delayed |
|------------|-------|--------|---------------------|
| Rick and Morty public API availability | Third-party (rickandmortyapi.com) | Assumed stable | Blocks T-013/T-019 functional verification only |
| ~~Apple Developer Program enrollment~~ | N/A | **No longer applicable** — free Apple ID confirmed (§11 Q2), no external approval process in the critical path | N/A |
| Xcode being recent enough for the iOS 18 SDK, given it's a pre-existing install | User's VM (self-managed) | Unknown until T-009 runs | If it needs an update after all, could reintroduce the original multi-GB-download delay this plan had assumed away — see R-2 |

---

## 8. Risk Assessment

| ID | Risk | Likelihood | Impact | Mitigation | Contingency |
|----|------|------------|--------|------------|-------------|
| R-1 | `kmpgen`-generated Ktor client code (or some other library/config combination in this project) fails to compile or run for Kotlin/Native (`iosArm64`/`iosX64`) — e.g. relies on a JVM-only API, reflection, or an engine-specific assumption | **✅ MATERIALIZED (2026-09-17), resolved.** Original estimate: Medium-High. Actual failure mode was a **KLIB ABI version mismatch** (`kmpgen` 1.5.0's `companion` klib built with a newer Kotlin than this project's pinned 2.2.21), not a code-level JVM-only-API issue as hypothesized — plus a second, unrelated failure when the first fix attempt (downgrading `kmpgen`) hit an AGP9 incompatibility. Both resolved; see D6 outcome, §0/§3.2. | High (blocks everything downstream) — confirmed accurate, it did block all further Phase 1 work until resolved | T-002 spike caught this in the first work session, before any Xcode/VM investment, exactly as designed — this part of the mitigation worked. ~~T-013 (first Simulator run) is the second checkpoint~~ **T-013 no longer exists (D1 revised) — there is no second checkpoint before Phase 4 anymore. See new Risk R-8.** | What actually worked: bumped `kotlin`/`composeMultiplatform`/`composeHotReload` to match `kmpgen` 1.5.0's build target, after ruling out a `kmpgen` downgrade (AGP9 NPE). The plan's (a)/(b)/(c) fallbacks were not needed. |
| **R-8 (new, 2026-09-17): no Simulator safety net before physical-device signing** | Since `iosX64` is gone and `iosSimulatorArm64` was never viable (Intel VM), Phase 3's original purpose — proving the whole Xcode/framework/Compose-on-iOS pipeline cheaply on a Simulator before risking first-time Apple signing — no longer exists. Any remaining bug in `MainViewController.kt`, the Run Script embed step, or Compose-Multiplatform-on-iOS rendering will now surface for the first time *during* Phase 4's on-device attempt, compounding with first-time signing/USB-passthrough unknowns (R-3, R-5) instead of being isolated from them | Medium-High (a bug that would have been a 10-minute Simulator fix now costs a full signing+device debugging cycle to even notice) | Medium-High (extends Phase 4's already-wide 6-11h estimate; makes failures harder to diagnose — is it signing, USB, or the framework itself?) | Treat T-011/T-012 (Xcode project + Run Script) as needing extra scrutiny before moving to Phase 4, since they're now the last checkpoint; consider a throwaway minimal SwiftUI view (not the full Compose UI) as a cheap manual sanity check that the framework embeds and the Xcode project builds, before wiring in `ComposeUIViewController` | If Phase 4 fails and it's unclear which layer is broken: temporarily add back a print/log statement at the very top of `MainViewController()` and check Xcode's device console output — this at least confirms whether the framework loaded and the entry point was reached, independent of whether the UI renders correctly |
| R-2 | The Xcode already installed in the VM (from the prior KMP attempt) predates iOS 18 SDK support (Apple sometimes drops Intel-Mac support in current Xcode versions faster than expected, so an older install may be stuck on an old SDK) | Low-Medium (already installed and presumably was working for *some* KMP attempt, but its exact version/SDK support is unconfirmed — T-009 checks this directly) | High | T-009 runs `xcodebuild -version`/`-showsdks` as the very first Phase 3 action, before any Xcode-project work, to confirm iOS 18 SDK support; older Xcode can still often build for/deploy to a newer iOS device even without the very latest SDK | If blocked: update Xcode via the App Store/developer.apple.com inside the VM (reintroduces the original multi-GB-download time this plan otherwise assumes away), or target iOS 17 SDK deployment as a fallback (a device running iOS 18 can usually still run apps built against a slightly older SDK) — document this as an accepted constraint, not a blocker |
| R-3 | USB passthrough for the physical iPhone doesn't work reliably through **QEMU** specifically (confirmed hypervisor, §11 Q3) — QEMU's USB passthrough for iOS devices is a well-known rough edge (requires an XHCI controller configured in the VM, the iPhone's vendor/product ID passed through, and sometimes `usbredir`) | Medium (QEMU is one of the less turnkey hypervisors for this specifically; genuinely untested for this user — their prior KMP attempt never got far enough to try USB passthrough at all, so there's no personal precedent either way, positive or negative) | Medium | T-017 explicitly checks device visibility from the VM (`system_profiler SPUSBDataType`, or `idevice_id -l` via `libimobiledevice`) *before* touching Xcode, isolating a QEMU/USB problem from an Xcode/signing problem; verify the VM's QEMU launch config includes an XHCI USB controller with the iPhone's IDs passed through | Fallback: Xcode supports **wireless (network) device debugging** — pair once over USB (or even without USB via Wi-Fi Sync if already trusted from a prior Mac), then debug entirely over Wi-Fi, sidestepping QEMU passthrough issues entirely |
| R-4 | Xcode "Run Script" build phase for `embedAndSignAppleFrameworkForXcode` fails due to environment variable mismatches (a very common first-time KMP+iOS pain point — `PATH`, `JAVA_HOME`, Gradle daemon issues when invoked from Xcode's sandboxed script environment) | High | Medium | Follow the official JetBrains Compose Multiplatform iOS quickstart snippet verbatim first, resist the urge to customize before it works once; check `local.properties`/`JAVA_HOME` is discoverable from Xcode's script environment | If persistent: run `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` manually from Terminal with the same env vars Xcode would set, to isolate whether the failure is Gradle-side or Xcode-script-side |
| R-5 | First-time Apple code signing/provisioning is confusing enough to stall Phase 4 significantly beyond estimate (unfamiliar UI, unclear error messages like "no signing certificate found") | High (explicitly the user's first iOS app and first time signing anything for Apple — genuinely untested territory, since their prior KMP attempt stalled at the Simulator stage and never reached signing at all, so there's no personal precedent to draw on either way) | Medium | Budget the wide 6-11 hour range in Phase 4 deliberately; use Xcode's "automatically manage signing" rather than manual certificate/profile management; the T-017 checkpoint (verify device visibility before touching signing) and T-015→T-017→T-018 ordering are deliberately sequenced as separate, individually-verifiable steps so that if something fails, it's immediately clear *which* layer (QEMU/USB vs. signing vs. trust-on-device) is the culprit | ~~If stuck >2x estimate: fall back to running on the **Simulator only** for this plan's initial pass (T-013 already proves the pipeline)~~ **No longer a valid fallback (D1 revised) — there is no Simulator to fall back to.** If stuck >2x estimate: fall back to isolating the Xcode-build-succeeds checkpoint (T-012) as the interim deliverable and treat physical-device *launch* as the sole remaining blocker, documented explicitly as incomplete rather than silently downgraded — see Risk R-6 |
| R-6 | Physical-device signing turns out to need more calendar time than expected (e.g. paid account approval delay) while the user wants to close out this training objective | Low-Medium | Low | T-014 started on day one to surface this risk early | ~~If it slips: treat "runs in the iOS Simulator with full functional parity" (Phase 3 deliverable) as an acceptable interim Definition of Done~~ **Revised (D1): no Simulator deliverable exists to fall back to.** If it slips: treat "Xcode project builds against the device-targeted framework" (revised Phase 3 deliverable) as the acceptable interim state, with physical-device confirmation as a fast-follow once signing is resolved — flagged explicitly rather than silently downgrading scope |
| R-7 | Compose Multiplatform 1.6.10 (current pinned version) has a known iOS-specific rendering/interop bug affecting this app's UI (e.g. `LazyColumn` scroll performance, text rendering) | Low | Low-Medium | Check JetBrains' Compose Multiplatform release notes/issue tracker for 1.6.10 iOS-specific known issues before Phase 2 | If hit: bump `composeMultiplatform` version in `libs.versions.toml` (verify compatibility with pinned `kotlin = "2.2.21"` first) |

---

## 9. Testing Strategy

### 9.1 Unit Testing
No new unit tests are introduced by this plan — this is a build-configuration/platform-shell change, not new business logic. `commonTest` (JUnit5, JVM-executed per CLAUDE.md) is unaffected and must continue passing unmodified (NFR-3, T-021). Porting `commonTest` to also execute on Kotlin/Native targets is explicitly out of scope (per the original spec's "explicitly out of scope" list) — it's a separate, non-trivial effort (different test runner mechanics) better suited to its own future plan.

### 9.2 Integration Testing
No automated integration tests. ~~The generated `kmpgen` client's behavior against the real Rick and Morty API on iOS is verified manually (T-013 on Simulator, T-019 on-device)~~ **Revised (D1): verified manually on-device only (T-019) — there is no Simulator to verify on first** — rather than via an automated integration suite, consistent with how Android/Desktop were originally verified in this project.

### 9.3 End-to-End Testing
Manual only, per CLAUDE.md convention for this project (no E2E framework in use anywhere):
- ~~**Simulator (T-013):** launch app in iOS Simulator inside the VM, confirm the shared UI renders, navigate to at least one list screen, confirm live API data loads.~~ **Removed (D1 revised) — no Simulator target exists for this project.**
- **Physical device (T-019):** full smoke test — Character list → Location list → Episode list, confirm live data on all three, confirm no crashes, confirm acceptable performance (no unusable jank) on the real iPhone. **This is now the first and only iOS UI verification point in the entire plan** (see Risk R-8).

### 9.4 Performance Testing
Not applicable — no performance requirements beyond "usable on a real device," verified qualitatively during T-019. No load/scale dimension exists for a client-only app against a public read-only API.

### 9.5 Security Testing
Not applicable — no new auth, no new data handling, no new attack surface (same unauthenticated public API as Android/Desktop). Code signing (Phase 4) is a distribution-integrity mechanism, not an application security control, and is scoped to "debug run on one owned device," not covered by a security review.

---

## 10. Definition of Done

- [x] ~~`composeApp/build.gradle.kts` declares `iosArm64()` and `iosX64()` targets~~ **`composeApp/build.gradle.kts` declares an `iosArm64()` target** (D1 revised — `iosX64()` dropped, no Gradle variant published) — `binaries.framework {}` **not yet configured** (T-001 done, T-006 not started).
- [x] `iosMain` contains a Darwin Ktor engine dependency (T-003 done) — `MainViewController.kt` **not yet created** (T-007 not started).
- [x] Dead `expect fun httpClientEngine()` code is removed (T-004 done).
- [x] **(New, not in original DoD)** `Logger`'s `expect fun platformLog(...)` has an `iosMain` actual (D9, done).
- [x] **(New, not in original DoD)** `commonTest` relocated to `jvmTest` so iOS test source sets stop trying to resolve JVM-only test libraries (D10, done); `CLAUDE.md` updated accordingly.
- [ ] ~~`iosApp/` Xcode project exists at repo root, embeds the Gradle-built framework via a Run Script build phase (not CocoaPods), and successfully launches in the iOS Simulator with live API data on at least one screen (T-011-T-013).~~ **Revised (D1): no Simulator to launch in.** `iosApp/` Xcode project exists at repo root, embeds the Gradle-built framework via a Run Script build phase (not CocoaPods), and builds successfully against the device destination (T-011-T-012). **Not started.**
- [ ] App is signed (automatic signing, free or paid Apple ID per Open Questions Q2) and launches on the user's physical iPhone (iOS 18) (T-014-T-018). **Not started.**
- [ ] All three list screens (Character, Location, Episode) are reachable and load live data on the physical device, matching Android/Desktop functional parity (T-019). **Not started.**
- [x] Android (`:androidApp:assembleDebug`) and Desktop (`:composeApp:run`) targets still build and run unmodified (T-005 done for Phase 1 changes; re-verify at T-021 once Phase 2-4 land).
- [x] `./gradlew :composeApp:jvmTest` still passes — now from `jvmTest`, not `commonTest` (T-005 done for Phase 1 changes; re-verify at T-021).
- [x] `CLAUDE.md` updated to reflect the new iOS target and entry point (T-020) — done ahead of schedule for the Phase 1 state; will need a follow-up pass once `MainViewController.kt`/Xcode land.
- [ ] Explicitly **not** required for this plan to be considered done: any published artifact (Maven Local, XCFramework/SPM/CocoaPods), Objective-C/Swift interop beyond the minimal SwiftUI shell, SKIE integration, App Store/TestFlight distribution, CI for iOS builds, or Kotlin/Native-targeted `jvmTest`/`commonTest` execution — all explicitly deferred (D8).
- [ ] ~~If Risk R-6 materializes: a working Simulator run (Phase 3 deliverable) with physical-device confirmation flagged as a documented fast-follow is an acceptable interim state, not silently treated as full completion.~~ **Revised (D1): no Simulator run is possible, so this fallback no longer applies as written — see R-6's updated contingency in §8.**

---

## 11. Open Questions (All Resolved)

All four open questions were answered by the user on 2026-09-17, right after this plan's first draft; kept here for the record, with the resulting plan adjustments cross-referenced throughout (§2.4, §4.3, Phase 3/4 headers, T-006/T-009/T-010/T-014/T-017, Risk R-2/R-3/R-5, §7.3).

| # | Question | Resolution |
|---|----------|------------|
| Q1 | How will the Intel macOS VM access this Git repository — a shared folder/mount from the host, or its own independent `git clone`? | **Independent `git clone` inside the VM.** Simplest option per the user; requires manually keeping the VM clone and host working copy in sync during iterative Phase 3-4 debugging (T-010). |
| Q2 | Free Apple ID or paid Apple Developer Program? | **Free Apple ID.** Accepts the 7-day provisioning-profile expiry/re-sign cycle in exchange for no enrollment cost or approval wait (T-014, NFR-4). |
| Q3 | Which hypervisor hosts the Intel macOS VM? | **QEMU.** Sharpened Risk R-3 with QEMU-specific USB-passthrough guidance (XHCI controller, vendor/product ID passthrough, `libimobiledevice` verification) and added an explicit device-visibility checkpoint (T-017) before attempting a full Xcode build+run. |
| Q4 | Should `binaries.framework { isStatic = ... }` default to static or dynamic linking? | **Static (`isStatic = true`).** Confirmed the plan's own recommended default — simplest for local dev, revisit only if a later remote-publishing phase needs dynamic linking (T-006). |

**Additional context volunteered by the user, later clarified (not a formal open question, but material to how risk is weighted across this plan):** Xcode and Android Studio are already installed in the VM from a previous attempt to build/run a different "real" KMP project — the user was not the iOS developer on that project and was doing it purely to learn. That attempt's actual blocker was that the project's own library/configuration choices prevented it from **compiling or running for the iOS Simulator at all** inside the macOS VM. Given the time cost of then also setting up the USB bridge to a real device, and given it wasn't their responsibility, the user stopped there without ever attempting physical-device signing or USB passthrough, and refocused on the Android side of that project instead. Two consequences for this plan: (a) Phases 1-3 (Kotlin/Native compilation, Compose Multiplatform on iOS, the Xcode+Simulator pipeline) are where real, concrete precedent for failure exists — reflected in R-1's elevated likelihood and T-002's early spike being the plan's most load-bearing risk mitigation; (b) Phase 4 (signing, USB passthrough, on-device run) is *not* backed by any negative precedent — it's simply untested, first-time territory, so R-3/R-5 reflect ordinary first-attempt uncertainty rather than a known prior failure at that specific stage.
