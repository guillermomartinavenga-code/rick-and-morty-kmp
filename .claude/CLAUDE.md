# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Kotlin Multiplatform (KMP) encyclopedia app for the Rick and Morty universe, targeting **Android (emulator)**, **Desktop (Linux JVM)**, and **iOS (physical device, `iosArm64`)** from a shared codebase. It fetches data from the public [Rick and Morty API](https://rickandmortyapi.com/) and displays Characters, Locations, and Episodes. Built with Jetpack Compose Multiplatform and a layered Clean Architecture (`data` / `domain` / `presentation`) + MVVM.

**Purpose**: this is a personal learning/practice project (not client work) used to demonstrate completion of engineering-training objectives — networking, KMP, testing, etc. Each engineering topic tends to live on its own git branch:

- `main` / `develop` — created in 2025 for a KMP course: MVVM + Retrofit against the Rick and Morty API.
- `openapi-kmp-gen` (merged into current work) — reused the project in 2026 to swap Retrofit for **Ktor** as the HTTP engine and to generate DTOs/endpoints/HTTP clients from the OpenAPI spec, first via `openapi2ktor` then via `openapi-kmp-gen` (see `openapi` branch for the earlier attempt). This is the state documented below — already completed.
- `proxy-test` — routes app traffic through a local proxy server. Already completed.
- `unit-test` — test data & fixtures, test doubles vs. mocks, parameterized tests. Already completed — see "Testing Conventions" below for what shipped.
- `kmp-distribution` (current branch) — multiplatform build configuration (adding a real iOS target) and publishing shared code in a monorepo. See "Current Engineering Focus" below for status.

Future work beyond that may cover further KMP topics and/or cybersecurity topics, time permitting.

## Commands

```shell
# Run all shared unit tests (live in jvmTest, executed on the JVM target — Android host tests are not enabled)
./gradlew :composeApp:jvmTest

# Run a single test class or method
./gradlew :composeApp:jvmTest --tests "com.example.rickandmorty.presentation.character.CharacterViewModelTest"
./gradlew :composeApp:jvmTest --tests "*.CharacterViewModelTest.loadCharacters should update characters state"

# Build & install the Android app (androidApp is the installable application module)
./gradlew :androidApp:assembleDebug

# Run the Desktop (JVM) app
./gradlew :composeApp:run

# Compile the shared module for the iOS device target (no simulator target — see "iOS target" below)
./gradlew :composeApp:compileKotlinIosArm64

# Link the iOS framework — only runs on macOS (unconditionally SKIPPED on Linux, see "iOS target" below)
./gradlew :composeApp:linkDebugFrameworkIosArm64

# Android lint
./gradlew :androidApp:lint
```

On Windows use `gradlew.bat` in place of `./gradlew`.

## Module Structure

- **`composeApp`** — a Kotlin Multiplatform *library* module (uses AGP 9's `com.android.kotlin.multiplatform.library` plugin, not `com.android.application`). It declares `android()`, `jvm()`, and `iosArm64()` targets and contains essentially all app code: `commonMain` (shared business logic + Compose UI), `androidMain`, `jvmMain`, `iosMain`, and `jvmTest`. Because it's a library module it has no `assembleDebug`/APK task of its own.
- **`androidApp`** — a thin `com.android.application` module whose only job is to depend on `composeApp` and produce the installable Android APK (`MainActivity.kt` just hosts the shared Compose UI). This is the module to target for Android builds/installs.
- Desktop distribution (`compose.desktop`) is configured inside `composeApp/build.gradle.kts` directly, since JVM is a target of that same module.
- There is no iOS app module/Xcode project yet. `iosArm64()` compiles (`compileKotlinIosArm64`) and, on a macOS host, links a `ComposeApp.framework` (`linkDebugFrameworkIosArm64`) exporting `MainViewControllerKt`/`MainViewController()` to Swift — verified in the project's macOS VM. The Xcode project (`iosApp/`) and on-device run are later tasks in `ios-support-implementation-plan.md`.

## Architecture

Data flows in one direction: **ApiService → Repository → UseCase → ViewModel → Compose Screen**, mirrored per feature (`character`, `location`, `episode`). Paginated endpoints return a `Page<T>` (`items` + `hasNextPage`) wrapped in an `Lce<T>` sealed state (`Lce.Loading` / `Lce.Content` / `Lce.Error`, in `domain/model/`), which ViewModels collect to drive incremental "load next page" pagination (see `CharacterViewModel.loadNextPageIfNeeded()`).

- **Networking / OpenAPI codegen**: `ApiService` (interface, `commonMain`) is implemented by `ApiServiceImpl`, which delegates to generated clients (`CharacterApi`, `LocationApi`, `EpisodeApi`) from the `com.kroegerama.openapi-kmp-gen` Gradle plugin (`kmpgen`). The plugin is configured in `composeApp/build.gradle.kts` (`kmpgen { spec(...) }`) against the spec at `composeApp/src/commonMain/kotlin/com/example/rickandmorty/data/remote/openapi/rick-and-morty-openapi.json`. Generated sources land under `composeApp/build/generated/...` (package `com.example.rickandmorty.data.remote.kmpgen`) and are **not** checked into git — run a build/sync before expecting IDE resolution of `kmpgen.*` imports. Raw generated API models are converted to domain models via extension functions in `ApiMappers.kt` (e.g. `CharacterApiModel.toDomain()`).
- **HTTP client configuration**: the generated `Api` singleton's underlying Ktor client is configured once in `di/Module.kt` (`Api.updateClient { install(HttpTimeout); install(Logging) }`), run from an `init` block the first time `Module` is loaded — the generated `Api` client manages its own engine internally. Per-target Ktor engines are declared in `composeApp/build.gradle.kts`: `ktor-client-okhttp` (`androidMain`), `ktor-client-cio` (`jvmMain`), `ktor-client-darwin` (`iosMain`). (The old pre-`kmpgen` `expect fun httpClientEngine()` in `data/remote/HttpClientFactory.kt` plus its `androidMain`/`jvmMain` actuals were dead code and have been deleted.)
- **Dependency injection**: no DI framework. `di/Module.kt` is a plain Kotlin `object` wiring `ApiService` → `*RepositoryImpl` → `*UseCaseImpl` as lazily-initialized singletons. ViewModels *are* constructor-injected, but with a default argument that falls back to the `Module` singleton (e.g. `class CharacterViewModel(private val getCharactersUseCase: GetCharactersUseCase = Module.getCharactersUseCase)`) — production call sites (`CharacterViewModel()`) get the same manual wiring as before, while tests pass in fakes/mocks explicitly.
- **Use cases**: split into an `api` sub-package (interfaces, e.g. `domain/use_case/api/GetCharactersUseCase.kt`) and an `impl` sub-package (implementations, e.g. `domain/use_case/impl/GetCharactersUseCaseImpl.kt`). `Module` wires the `impl` classes behind the `api` interfaces — this indirection exists so tests/ViewModels can depend on the interface and swap in a fake or MockK mock.
- **ViewModels**: plain Kotlin classes (`CharacterViewModel`, `LocationViewModel`, `EpisodeViewModel`) that do **not** extend AndroidX `ViewModel`, so they work unmodified on both Android and Desktop. State is exposed as `StateFlow` (accumulated characters/locations/episodes, `isLoadingNextPage`, `errorMessage`), populated by suspend `loadX()`/`loadNextPageIfNeeded()` functions called from the Composable screen.
- **Navigation**: no navigation library. `presentation/navigation/NavigationHost.kt` holds a `remember { mutableStateOf<Screen>(...) }` and a `when` that swaps between `MainScreen`/`CharacterScreen`/`LocationScreen`/`EpisodeScreen`; `Screen` (in `Screen.kt`) is the sealed class enumerating destinations.
- **Platform split**: `androidApp/.../MainActivity.kt` (Android entry point) and `jvmMain/.../main.kt` (Desktop entry point) each launch the shared `App()` composable. `iosMain/.../MainViewController.kt` (`fun MainViewController() = ComposeUIViewController { App() }`) is the iOS equivalent — it compiles and exports correctly to Swift (confirmed via the linked framework's header, see "iOS target" below), but has no Xcode project/app shell calling it yet. The `Logger`/`LoggerImpl`/`platformLog` `expect`/`actual` (in `utils/log/`) is the other boundary: `androidMain` uses `android.util.Log`, `jvmMain` uses SLF4J, and `iosMain` (`LoggerIos.kt`) uses `platform.Foundation.NSLog`.
- **iOS target**: only `iosArm64()` (physical device) is configured — `iosX64()` (Intel simulator) was tried and dropped because neither `kmpgen`'s mandatory `companion` klib dependency nor the Compose Multiplatform `runtime`/`foundation`/`ui` artifacts publish an `iosX64` Gradle variant at the versions this project uses (confirmed via "No matching variant" resolution errors) — this is an upstream ecosystem gap, not a config mistake. `iosSimulatorArm64()` was never added since the dev environment is an Intel (not Apple Silicon) macOS VM, so that target wouldn't be runnable there either. Net effect: **no iOS Simulator testing is possible in this project's environment**; all iOS verification has to happen on the physical device. `kmpgen`'s `companion` klib is compiled against a specific Kotlin version per plugin release (e.g. `kmpgen` 1.5.0 → Kotlin 2.4.0) and is not configurable — bumping `kotlin`/`composeMultiplatform`/`composeHotReload` versions in `gradle/libs.versions.toml` has to keep pace with whatever `kmpgen` version is pinned, or KLIB ABI resolution fails. `compose-material-icons-extended` is intentionally *not* pinned via `version.ref = "composeMultiplatform"` — that artifact was permanently frozen at `1.7.3` by JetBrains (deprecated in favor of "Material Symbols") and has its own literal version in the catalog. `binaries.framework { baseName = "ComposeApp"; isStatic = true }` is configured on `iosArm64` and produces `ComposeApp.framework`, but **only on macOS** — `linkDebugFrameworkIosArm64` (and any other Apple binary/framework *link* task, as opposed to *compile*) is unconditionally `SKIPPED` on the Linux dev host (`onlyIf 'Task is enabled' is false` in the Kotlin Gradle plugin, since the final link step needs Apple's `ld64` linker). `compileKotlinIosArm64` (klib compilation) has no such restriction and works fine on Linux. Practical effect: **any iOS work from framework-linking onward must happen inside the project's macOS VM** — there's no way to dry-run it on the Linux host first.

## Current Engineering Focus

The `unit-test` objective (fixtures, test doubles vs. mocks, parameterized tests) is complete — see "Testing Conventions" below for what actually shipped, since it differs from what was originally planned (Kotest/Kluent were dropped in favor of JUnit5).

The `kmp-distribution` branch (current) covers two objectives from the "Proficient" tier of the user's KMP training plan, per `ios-support-implementation-plan.md` (and its Spanish counterpart) at the repo root:

- **Multiplatform build configuration / platform interop** — in progress. `composeApp`'s `iosArm64` target compiles and, verified inside the project's macOS VM, links a working `ComposeApp.framework` exporting `MainViewController()` to Swift (`binaries.framework {}` configured, `MainViewController.kt` created, `linkDebugFrameworkIosArm64` confirmed — see "iOS target" above); the shared `Logger` gained an `iosMain` actual (`NSLog`-based). Still open: creating the actual Xcode project/app shell (`iosApp/`) and running on the user's physical iPhone (no Mac hardware — only an Intel macOS VM — so no simulator target is usable; see "iOS target" above).
- **Publishing shared code in a monorepo** — not yet started. Per the user's own research (referenced in the implementation plan), local/source distribution (Xcode building directly against the Gradle project) is the intended starting point, with remote/artifact publishing (Maven Local at minimum) as a later stretch phase, not a blocker for getting iOS running.

Don't assume any monorepo tooling, publishing setup (e.g. `maven-publish`), or Xcode project/app shell exist yet beyond what's described above — check `settings.gradle.kts` and the relevant `build.gradle.kts` files first, and check the implementation plan for the latest task status.

## Testing Conventions

- Tests live in `composeApp/src/jvmTest` (not `commonTest`) and run purely on the JVM (no Android instrumentation/robolectric), executed via JUnit5 (`tasks.withType<Test> { useJUnitPlatform() }` in `composeApp/build.gradle.kts`), pulled in through `kotlin-test-junit5` + `junit-params`, with `junit-platform-launcher`/`junit-engine` as `runtimeOnly`. They were originally written under `commonTest` when the project had no Kotlin/Native targets, which happened to work because `commonTest`'s dependencies were only ever resolved for the JVM target; once `iosArm64()` was added, `iosArm64Test`/`iosTest`/`appleTest` inherited from `commonTest` too and tried (and failed) to resolve JUnit5/MockK for Kotlin/Native. The whole suite was relocated to `jvmTest` (and its dependencies moved to a `jvmTest.dependencies {}` block) to fix that — the tests themselves are JVM/JUnit5/MockK-specific by design (per CLAUDE.md's own testing plan) and were never intended to run on Kotlin/Native, so this was a relocation, not a rewrite.
- **MockK** (`libs.mockk`) is used for interaction-based tests: `@MockK` fields + `MockKAnnotations.init(this)` in `@BeforeTest`, `every { ... } returns ...` to stub, `verify(exactly = n) { ... }` to assert calls (see `CharacterViewModelTest.kt`).
- Hand-written fakes are still used for simpler state-based tests, but are now named top-level classes rather than inline anonymous objects — e.g. `FakeApiService` in `data/repository/FakeApiService.kt`, which defaults every page to empty so a test only has to configure the one it cares about. `CharacterViewModelTest` deliberately keeps one fake-based test and one mock-based test side by side (`... using fake` / `... using mock`) to contrast the two styles.
- Shared fixtures live in `composeApp/src/jvmTest/.../testutil/DomainFixtures.kt`: a `createX(...)` builder per domain model with sensible defaults, plus a canonical `defaultX` instance (e.g. `createCharacter(...)` / `defaultCharacter`). Prefer `defaultX.copy(...)` over a fresh `createX(...)` call when a test only needs to override one or two fields.
- Parameterized tests use plain JUnit5 (`@ParameterizedTest`, `@MethodSource`, `Arguments.of(...)`), e.g. `ApiMappersTest.kt`. **Kotest and Kluent were evaluated but not adopted** — don't assume Kotest specs/matchers or Kluent assertions are available anywhere in the codebase.
- **Kover** (`libs.plugins.kover`, applied in `composeApp/build.gradle.kts`) generates HTML/XML coverage reports on `./gradlew check`.
- Coroutine-based ViewModel tests use `kotlinx-coroutines-test` with `StandardTestDispatcher` + `Dispatchers.setMain/resetMain` in `@BeforeTest`/`@AfterTest`, and `runTest` with `testScheduler.advanceUntilIdle()` / `runCurrent()` / `advanceTimeBy(...)` to drive suspend calls and assert timing-sensitive behavior like the minimum-loading-duration and in-flight-request-dedup logic in `CharacterViewModel` (see `CharacterViewModelTest.kt`).
