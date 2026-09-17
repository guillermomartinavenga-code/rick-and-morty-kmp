# Implementation Plan: Pagination Support for Character, Location & Episode Lists

> **Spec Source:** User request (conversation) — add pagination/infinite-scroll to the Character, Location, and Episode listing screens, consuming the Rick and Morty API's `info` object (count/pages/next/prev) which is currently fetched but discarded.
> **Plan Created:** 2026-09-09
> **Estimated Total Effort:** 20-30 developer-hours (production code only — test-writing is explicitly out of scope, see §2.4/D7)
> **Recommended Team Size:** 1 engineer (solo/learning project) — no QA role, self-verified via manual run on Desktop + Android emulator

---

## Table of Contents

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

## 1. Executive Summary

Today the app calls `CharacterApi.getAllCharacters()` / `LocationApi.getAllLocations()` / `EpisodeApi.getAllEpisodes()` with no `page` argument and immediately discards the response's `info` object, keeping only `.results`. Because the public API always paginates at 20 items/page, every list screen silently shows only the first page (e.g. 20 of 826 characters) with no indication more data exists. This plan adds page-aware fetching end-to-end — `ApiService → Repository → UseCase → ViewModel → Screen` — for all three features symmetrically, since they already share one identical architecture. Codegen inspection (see §2.4/T-001, resolved) confirms the generated `CharacterApi.getAllCharacters(page: Long? = null, ...)` (and Location/Episode equivalents) **already accept a page parameter** — no `kmpgen` reconfiguration is needed, only wiring it up. The approach: introduce a generic `Page<T>` domain wrapper carrying `items` + `hasNextPage`, thread a `page: Int` parameter down to the call (converted to `Long` at the `ApiServiceImpl` boundary), and give each ViewModel simple, non-persisted pagination state (`currentPage`, `endReached`, `isLoadingNextPage`, plus a lightweight error signal) driven by a scroll-position check against the Compose `LazyColumn`'s last visible item. Per the user's explicit direction, **this plan intentionally excludes writing or modifying any unit tests** — that is being reserved as separate, self-directed practice for the `unit-test` branch's stated learning goal; see the note in §9 and the flagged compilation impact this has on the existing test suite.

---

## 2. Specification Analysis

### 2.1 Functional Requirements

| ID | Requirement | Source | Priority |
|----|-------------|--------|----------|
| FR-1 | `ApiService` methods accept a `page: Int` parameter and return both results and pagination info | User spec | P0 |
| FR-2 | Domain layer exposes whether a next page exists (`info.next != null`) to the ViewModel | User spec | P0 |
| FR-3 | Repository methods accept a page parameter and return the paginated result | User spec | P0 |
| FR-4 | UseCases accept a page parameter and return the paginated result | User spec | P0 |
| FR-5 | ViewModel automatically requests the next page when the user scrolls to the end of the current list | User spec | P0 |
| FR-6 | ViewModel does not issue a request if a page is already in flight (no duplicate requests) | User spec | P0 |
| FR-7 | ViewModel does not issue a request if the last known page had no `next` (end of data reached) | User spec | P0 |
| FR-8 | Newly fetched items are appended to the existing list, not replacing it | Implied by "infinite scroll" | P0 |
| FR-9 | Behavior is identical across Character, Location, and Episode features | User spec | P0 |
| FR-10 | Compose screens detect "reached last visible item" (literal last item, no prefetch-ahead) and trigger the ViewModel's load-next-page | User spec, confirmed | P0 |
| FR-11 | If a next-page request fails, the user sees an informative error message; pagination does not silently stop with no feedback | User answer (Q1) | P1 |
| FR-12 | Pagination state is not persisted across screen re-entry — each time a list screen is (re)mounted, it starts fresh at page 1 (matches current `remember { XViewModel() }` behavior) | User answer (Q2) | P0 |

### 2.2 Non-Functional Requirements

| ID | Requirement | Target | Measurement |
|----|-------------|--------|-------------|
| NFR-1 | No duplicate/overlapping network calls for the same page | Zero duplicate calls observed | Manual test + unit test on ViewModel guard flag |
| NFR-2 | Existing single-page behavior (`Module.kt` wiring, initial page-1 load UX) must not regress | Manual verification only — `./gradlew :composeApp:jvmTest` is **expected to fail to compile** after signature changes since existing tests aren't updated (see D7); not a regression gate for this task | Manual run on Desktop/Android |
| NFR-3 | Scroll-triggered fetches must not block the UI thread | Runs via `viewModelScope`-equivalent (coroutine launched from Composable's `LaunchedEffect`/`rememberCoroutineScope`) | Manual verification, no ANR/jank on Desktop run |
| NFR-4 | Loading-next-page state should be visible to the user (footer spinner) | A loading indicator row appears at list end while fetching | Manual visual check |
| NFR-5 | Error feedback should be simple/low-effort — this is a personal learning project, not production software; a basic inline message is sufficient, no retry/backoff/analytics needed | A short error row/snackbar appears at list end on failure | Manual visual check |

### 2.3 Business Rules & Constraints

- The Rick and Morty public API fixes page size at 20 and is not configurable — the app must treat `pageSize` as an API-given constant, not something it can request.
- `info.next` is the authoritative "is there more data" signal — pages are not knowable in advance beyond `info.pages`, so the app should drive off `next != null` rather than comparing `currentPage < totalPages` (both work, but `next` matches the API's own contract and survives if `pages`/`count` are ever wrong or stale).
- Existing ViewModels are plain Kotlin classes (not AndroidX `ViewModel`), with no built-in coroutine scope — the current code calls `loadCharacters()` as a `suspend fun` from a `LaunchedEffect` in the screen. Pagination logic must fit this same model (no `viewModelScope`, no DI framework) — this constrains how "in-flight" state is tracked (must be plain `StateFlow`/boolean flags, not a coroutine-cancellation-based approach).
- `Module.kt` singletons are stateless services (UseCase/Repository hold no per-request state) — page state must live in the ViewModel, not the UseCase/Repository, so that Module singletons remain shareable/stateless.

### 2.4 Ambiguities & Gaps (Resolved)

All ambiguities originally flagged here were resolved by the user before implementation started; kept for record:

- **Error handling on a failed "next page" request** — *Resolved (Q1):* show the user a simple informative message on failure. Given this is a personal/learning project, no retry logic, exponential backoff, or telemetry is warranted — a plain error row/snackbar is sufficient (see FR-11, NFR-5).
- **Reset behavior on screen re-entry** — *Resolved (Q2):* keep it simple, no persistence — pagination state resets to page 1 every time a list screen is (re)mounted, consistent with today's `remember { XViewModel() }` behavior (see FR-12).
- **Scroll-trigger threshold** — *Resolved (Q4):* literal last-item-visible trigger, no N-item prefetch-ahead. Matches the spec's literal wording and keeps the implementation simple, per user preference.
- **Generated client page-param support** — *Resolved (Q3/T-001):* inspected `composeApp/build/generated/kmpgen/.../api/Services.kt` and `.../models/Models.kt` directly. Confirmed `CharacterApi.getAllCharacters(page: Long? = null, name: String? = null, status: ..., species: ..., type: ..., gender: ..., decorator: ...)` and the Location/Episode equivalents already declare `page: Long? = null` as their first parameter. **No `kmpgen` reconfiguration or spec change needed.** One implementation detail this surfaces: the generated param type is `Long?`, while the domain/UI-facing page counter is more naturally an `Int` (matching `Page<T>` design and existing `id: Int` conventions elsewhere in domain models) — `ApiServiceImpl` will convert `page.toLong()` at the call boundary. Response types are confirmed exactly:
  ```kotlin
  data class Info(val count: Long? = null, val pages: Long? = null, val next: String? = null, val prev: String? = null)
  data class Character200Response(val info: Info? = null, val results: List<Character>? = null)
  data class Location200Response(val info: Info? = null, val results: List<Location>? = null)
  data class Episode200Response(val info: Info? = null, val results: List<Episode>? = null)
  ```
  So `hasNextPage` maps as `info?.next != null` (or the safer `!info?.next.isNullOrBlank()`), and `results.orEmpty()` follows the same null-safety pattern already used in today's `ApiServiceImpl` (`response.data.results.orEmpty().map { it.toDomain() }`).
- **Test scope** — *Resolved (Q5):* this plan explicitly does **not** write or modify any unit tests. The user is deliberately reserving test-writing (fixtures, MockK, parameterized tests) as separate, self-directed practice for this branch's stated learning objective, and will ask for help in a later session if needed. Important consequence flagged in [§9](#9-testing-strategy) and [Definition of Done](#10-definition-of-done): changing method signatures across `ApiService`/`Repository`/`UseCase` will break compilation of the *existing* tests (`CharacterRepositoryImplTest`, `GetCharactersUseCaseTest`, `CharacterViewModelTest`, and their Location/Episode equivalents), since they currently call today's zero-arg signatures. `./gradlew :composeApp:jvmTest` is expected to fail to compile until the user updates those tests themselves — this is accepted as out-of-scope fallout, not a defect in this plan.

---

## 3. Architecture & Design Decisions

### 3.1 High-Level Architecture

No new modules or layers — this is a signature/state change threaded through the existing five layers, applied identically to Character, Location, and Episode:

```
CharacterScreen (LazyColumn + scroll-end detector)
        │  calls loadNextPageIfNeeded()
        ▼
CharacterViewModel (page state: currentPage, endReached, isLoadingNextPage)
        │  calls invoke(page = currentPage + 1)
        ▼
GetCharactersUseCase (passthrough, now page-aware)
        │
        ▼
CharacterRepository / CharacterRepositoryImpl (passthrough, now page-aware)
        │
        ▼
ApiService / ApiServiceImpl.getCharacters(page: Int)
        │  calls CharacterApi.getAllCharacters(page = page)
        ▼
kmpgen-generated CharacterApi → returns {info, results}
```

A new shared domain type, `Page<T>`, is introduced once (not per-feature) and reused by all three features:

```kotlin
// domain/model/Page.kt
data class Page<T>(
    val items: List<T>,
    val hasNextPage: Boolean
)
```

This is intentionally minimal — it exposes only what the ViewModel needs (`items` + `hasNextPage`), not the full `info` object (count/pages/prev), since nothing in the current UI needs total-count display or backward pagination. See [decision D1](#32-key-technical-decisions).

### 3.2 Key Technical Decisions

| Decision | Recommendation | Rationale | Alternatives Considered |
|----------|---------------|-----------|--------------------------|
| D1: Shape of the pagination-carrying domain type | Introduce one generic `Page<T>(items: List<T>, hasNextPage: Boolean)` in `domain/model/`, shared by all 3 features | Minimal surface, satisfies FR-2/FR-8 without exposing API-shaped fields (`count`/`pages`/`prev`) that nothing consumes; a generic type avoids writing `CharacterPage`/`LocationPage`/`EpisodePage` boilerplate three times | (a) Three separate per-feature wrapper classes — rejected, pure duplication; (b) Passing raw `info` object down to ViewModel — rejected, leaks API/DTO shape into domain layer, violates existing `ApiMappers.kt`-based domain boundary |
| D2: Where page-tracking state lives | ViewModel (new `MutableStateFlow` fields: `currentPage: Int`, `endReached: Boolean`, `isLoadingNextPage: Boolean`) | Matches existing architecture where ViewModels already own `StateFlow` state and UseCase/Repository are stateless singletons (per `Module.kt`) | Storing page cursor in Repository — rejected, would make repository singleton stateful and break if two screens/ViewModels shared it concurrently |
| D3: How "reached end of list" is detected in Compose | Derive from `LazyListState.layoutInfo.visibleItemsInfo` — trigger `loadNextPageIfNeeded()` when the last visible item's index equals `list.size - 1` (last item itself, per user's confirmed preference, Q4) | Matches FR-10 exactly as specified and confirmed ("solicitar la próxima página al visualizar el último elemento") — simplest correct implementation | Prefetch-before-end (N-from-last) — rejected by user for now (Q4), kept simple; easy follow-up if perceived latency becomes an issue |
| D4: Duplicate-request guard | Single `isLoadingNextPage: Boolean` StateFlow checked before issuing a new page request; screen-side effect re-fires on every recomposition where the condition holds, but the guard makes repeat fires no-ops | Simplest mechanism consistent with existing plain-class ViewModel (no coroutine-scope cancellation infrastructure to lean on) | Debouncing scroll events — unnecessary complexity for a "value increases monotonically" list; job-cancellation via stored `Job` reference — more idiomatic with a real `viewModelScope`, but these ViewModels don't have one today (out of scope to introduce) |
| D5: Error surfacing on failed next-page fetch | Add a simple `errorMessage: String?` (or similar) field to each ViewModel's state, set when `loadNextPageIfNeeded()`'s underlying call throws, cleared on the next successful fetch or user-dismiss; screen shows a plain text row/snackbar at the list's end | Directly satisfies FR-11/NFR-5 (user confirmed: simple message is enough, "no es vital" given this is a learning project) — no need for retry/backoff machinery | Silent failure (original spec ambiguity) — rejected per Q1 answer; full retry-with-backoff UX — rejected as over-engineering for a personal project |
| D6: kmpgen page parameter | Pass `page` as a query parameter to the generated `CharacterApi.getAllCharacters(page = page.toLong())` (and Location/Episode equivalents) | **Confirmed via direct inspection of the generated `Services.kt`** (T-001): `getAllCharacters`/`getAllLocations`/`getAllEpisodes` already declare `page: Long? = null` as their first parameter — no `kmpgen` spec/config change needed, only a domain-side `Int → Long` conversion at the `ApiServiceImpl` call site | Manually constructing pagination URLs (using `info.next` string directly) — rejected, ties client to a raw URL rather than the typed generated client, harder to test/mock. Passing `Long` all the way up through Repository/UseCase/ViewModel — rejected, needlessly leaks a codegen implementation detail into domain/presentation layers where `Int` is the existing convention (see `id: Int` on all domain models) |
| D7: Test-writing scope | **Do not write or modify any unit tests as part of this implementation** (user directive, Q5) — production code changes only across Data/Domain/Presentation layers | User is deliberately reserving test-writing for separate, self-directed practice tied to this branch's learning objective | Updating just the tests broken by signature changes — considered, but user explicitly asked to handle all test work themselves, including fixing breakage; see §2.4 and §9 for the resulting compilation caveat |

### 3.3 Data Model Changes

- **New:** `domain/model/Page.kt` — `data class Page<T>(val items: List<T>, val hasNextPage: Boolean)`.
- **Changed:** `ApiMappers.kt` — add a mapping step (or a small extension per feature) that converts the generated `Character200Response`/`Location200Response`/`Episode200Response` (each confirmed as `{info: Info?, results: List<X>?}`, see §2.4) into `Page<Character>` / `Page<Location>` / `Page<Episode>`, e.g. `Character200Response.toDomainPage(): Page<Character> = Page(items = results.orEmpty().map { it.toDomain() }, hasNextPage = info?.next != null)`.
- **No persistence/schema changes** — no database involved in this app; state is in-memory only (ViewModel `StateFlow`), consistent with current architecture, and explicitly not retained across screen re-entry (FR-12/Q2).
- **No breaking change to existing single-item mappers** (`CharacterApiModel.toDomain()` etc.) — those remain as-is for mapping each item inside `results`.
- **Changed (new, per D5/FR-11):** each ViewModel's state gains a simple `errorMessage: String?` (or a small sealed `UiState`/flag, implementer's choice) surfaced by the screen when a next-page fetch throws.

### 3.4 Integration Points

| System | Direction | Protocol | Auth | Notes |
|--------|-----------|----------|------|-------|
| Rick and Morty public API (`rickandmortyapi.com`) | Outbound (app → API) | HTTPS / REST via Ktor (generated `kmpgen` client) | None (public, unauthenticated API) | Only change is adding `?page=N` query param to existing `/character`, `/location`, `/episode` GET calls — no new endpoints |

---

## 4. Required Skills & Resources

### 4.1 Technical Skills

| Skill Area | Specific Expertise | Phases Needed | Criticality |
|------------|--------------------|----------------|-------------|
| Kotlin Multiplatform | `expect`/`actual`-free shared code, Gradle KMP module structure, `commonMain`/`commonTest` conventions | All phases | Critical |
| Jetpack Compose Multiplatform | `LazyColumn`, `LazyListState`, `derivedStateOf`, `snapshotFlow`/side-effect patterns for scroll detection | Phase 3 | Critical |
| Kotlin Coroutines & `StateFlow` | `MutableStateFlow`, `StandardTestDispatcher`, `runTest`, manual (non-`viewModelScope`) coroutine launching from Composables | Phases 1-4 | Critical |
| OpenAPI codegen (`openapi-kmp-gen` / `kmpgen` Gradle plugin) | Reading generated client signatures to confirm param support (already done for T-001 — `page: Long?` confirmed present); no regeneration/config change expected | Phase 1 | Medium |
| Clean Architecture / layered refactor discipline | Threading a new parameter through interface→impl pairs across 4 layers without breaking the unidirectional data flow | Phases 1-2 | Medium |
| Basic error-state UI in Compose | A minimal error row/snackbar tied to ViewModel state (D5) — no new library needed, reuses existing Compose primitives | Phase 4 | Low |

### 4.2 Domain Knowledge

- Familiarity with the Rick and Morty public API's pagination contract (already documented by the user in this request — `info.count/pages/next/prev`, 20 items/page, page param on all 3 list endpoints).
- No regulatory/compliance/business-domain knowledge needed (public read-only API, no PII, no auth).

### 4.3 Tools & Infrastructure

| Tool/Service | Purpose | Already Available? | Setup Effort |
|--------------|---------|---------------------|----------------|
| `kmpgen` OpenAPI codegen plugin | Provides `page`-param-aware client methods | Yes, confirmed already exposed (T-001) | None |
| Desktop run (`./gradlew :composeApp:run`) | Manual verification of infinite scroll UX | Yes | None |
| Android emulator (`:androidApp:assembleDebug`) | Manual verification on Android target | Yes (per CLAUDE.md, targets Android emulator) | None |
| `./gradlew :composeApp:jvmTest` | Not used as a gate for this task (see NFR-2/D7) — will fail to compile until the user updates tests separately | Yes, but out of scope | N/A |

---

## 5. Phase Breakdown

### Phase 1: Foundation — Data & Domain Layer Pagination Plumbing (5-8 hours)
**Goal:** Make pagination info flow from the network response into a usable domain type, for all 3 features.
**Deliverable:** `ApiService`, `ApiServiceImpl`, domain `Page<T>` model, and `ApiMappers` all compile and correctly extract `info.next` alongside `results`.
**Prerequisites:** None — T-001 (generated client supports `page`) is already resolved.

### Phase 2: Repository & UseCase Pass-Through (3-5 hours)
**Goal:** Extend `CharacterRepository`/`LocationRepository`/`EpisodeRepository` and their `GetXUseCase` counterparts to accept a page and return `Page<T>`.
**Deliverable:** All 3 repository interfaces + impls + use cases updated and compiling; `Module.kt` wiring unaffected (still stateless singletons).
**Prerequisites:** Phase 1 complete (domain `Page<T>` type exists).

### Phase 3: ViewModel Pagination State Machine (7-10 hours)
**Goal:** Each ViewModel accumulates pages, exposes `endReached`/`isLoadingNextPage`/`errorMessage`, and exposes a `loadNextPageIfNeeded()` (or similarly named) suspend function callable from the screen's scroll-detection side effect.
**Deliverable:** `CharacterViewModel`/`LocationViewModel`/`EpisodeViewModel` support: initial load (page 1), append-on-scroll, guard against duplicate/over-the-end requests, and surface a simple error message on failed fetches.
**Prerequisites:** Phase 2 complete.

### Phase 4: Compose UI — Scroll-Triggered Loading (5-8 hours)
**Goal:** Wire `LazyListState` scroll position to `loadNextPageIfNeeded()`, show a loading footer while the next page fetches, and show a simple error row on failure.
**Deliverable:** Running the Desktop and Android app shows continuous scrolling across all pages of Characters/Locations/Episodes with a spinner at the bottom while fetching, stops cleanly at the last page, and shows an error message if a page fetch fails.
**Prerequisites:** Phase 3 complete.

> Test-writing (Kotest/MockK, parameterized guard-matrix tests, updating the existing broken tests) is **explicitly out of scope** for this plan per the user's direction (D7/Q5) and is not phased here — the user is handling it separately.

---

## 6. Detailed Task List

| ID | Task | Description | Phase | Priority | Estimate | Dependencies | Skills |
|----|------|-------------|-------|----------|----------|---------------|--------|
| T-001 | ~~Verify/enable `page` param on generated clients~~ **DONE** | Confirmed via direct inspection of generated `Services.kt`: `getAllCharacters`/`getAllLocations`/`getAllEpisodes` already declare `page: Long? = null`. No further action needed. | 1 | P0 | 0 hours (resolved) | None | OpenAPI codegen |
| T-002 | Add `domain/model/Page.kt` | Create generic `data class Page<T>(val items: List<T>, val hasNextPage: Boolean)` | 1 | P0 | 0.5-1 hour | None | Kotlin |
| T-003 | Update `ApiMappers.kt` for Character | Add `Character200Response.toDomainPage(): Page<Character>` = `Page(items = results.orEmpty().map { it.toDomain() }, hasNextPage = info?.next != null)` | 1 | P0 | 1-2 hours | T-002 | Kotlin |
| T-004 | Update `ApiMappers.kt` for Location | Same as T-003 for `Location200Response`/Location | 1 | P0 | 1-2 hours | T-002 | Kotlin |
| T-005 | Update `ApiMappers.kt` for Episode | Same as T-003 for `Episode200Response`/Episode | 1 | P0 | 1-2 hours | T-002 | Kotlin |
| T-006 | Update `ApiService`/`ApiServiceImpl.getCharacters` | Change signature to `suspend fun getCharacters(page: Int): Page<Character>`; call `CharacterApi.getAllCharacters(page = page.toLong())`; map via T-003 | 1 | P0 | 1-2 hours | T-003 | Kotlin, Ktor/OpenAPI client |
| T-007 | Update `ApiService`/`ApiServiceImpl.getLocations` | Same as T-006 for Location | 1 | P0 | 1-2 hours | T-004 | Kotlin, Ktor/OpenAPI client |
| T-008 | Update `ApiService`/`ApiServiceImpl.getEpisodes` | Same as T-006 for Episode | 1 | P0 | 1-2 hours | T-005 | Kotlin, Ktor/OpenAPI client |
| T-009 | Update `CharacterRepository`/`Impl` | Change signature to `suspend fun getCharacters(page: Int): Page<Character>`, passthrough to `ApiService` | 2 | P0 | 1 hour | T-006 | Kotlin |
| T-010 | Update `LocationRepository`/`Impl` | Same as T-009 for Location | 2 | P0 | 1 hour | T-007 | Kotlin |
| T-011 | Update `EpisodeRepository`/`Impl` | Same as T-009 for Episode | 2 | P0 | 1 hour | T-008 | Kotlin |
| T-012 | Update `GetCharactersUseCase` | Change `invoke(page: Int): Page<Character>`, passthrough to repository | 2 | P0 | 0.5-1 hour | T-009 | Kotlin |
| T-013 | Update `GetLocationsUseCase` | Same as T-012 for Location | 2 | P0 | 0.5-1 hour | T-010 | Kotlin |
| T-014 | Update `GetEpisodesUseCase` | Same as T-012 for Episode | 2 | P0 | 0.5-1 hour | T-011 | Kotlin |
| T-015 | Design ViewModel pagination state shape | Decide exact field names/types (`currentPage`, `endReached`, `isLoadingNextPage`, `errorMessage`, accumulated `items` StateFlow) shared conceptually across all 3 ViewModels; replicate the pattern 3×, not a shared base class per CLAUDE.md's "no premature abstraction" guidance unless duplication becomes painful | 3 | P0 | 1-2 hours | T-012, T-013, T-014 | Kotlin, architecture |
| T-016 | Implement `CharacterViewModel` pagination | Add state fields per T-015; implement `loadCharacters()` (initial, page 1) and `loadNextPageIfNeeded()` (guards on `endReached`/`isLoadingNextPage`, increments page, appends results, updates `endReached` from `Page.hasNextPage`, catches failures into `errorMessage` per D5/FR-11) | 3 | P0 | 2-3 hours | T-015 | Kotlin, coroutines/StateFlow |
| T-017 | Implement `LocationViewModel` pagination | Same as T-016 for Location | 3 | P0 | 2-3 hours | T-015 | Kotlin, coroutines/StateFlow |
| T-018 | Implement `EpisodeViewModel` pagination | Same as T-016 for Episode | 3 | P0 | 2-3 hours | T-015 | Kotlin, coroutines/StateFlow |
| T-019 | Wire `CharacterScreen` scroll-to-end detection | Introduce `rememberLazyListState()`, derive "is last item visible" (per decision D3 — literal last item), call `loadNextPageIfNeeded()` via `LaunchedEffect`/`snapshotFlow`; add loading-footer row when `isLoadingNextPage`, and a simple error row/snackbar when `errorMessage != null` | 4 | P0 | 2-3 hours | T-016 | Compose Multiplatform |
| T-020 | Wire `LocationScreen` scroll-to-end detection | Same as T-019 for Location | 4 | P0 | 2-3 hours | T-017 | Compose Multiplatform |
| T-021 | Wire `EpisodeScreen` scroll-to-end detection | Same as T-019 for Episode | 4 | P0 | 2-3 hours | T-018 | Compose Multiplatform |
| T-022 | Manual verification — Desktop run | Run `:composeApp:run`, scroll all 3 lists to the end, confirm continuous loading, correct stop at last page, and error row on a simulated failure (e.g. temporarily disconnect network) | 4 | P1 | 1 hour | T-019, T-020, T-021 | Manual QA |
| T-023 | Manual verification — Android emulator | Run `:androidApp:assembleDebug` + install, repeat T-022 on Android target | 4 | P1 | 1 hour | T-019, T-020, T-021 | Manual QA, Android |

> Note: changing the signatures in T-006/007/008 (ApiService), T-009/010/011 (Repository), and T-012/013/014 (UseCase) will break compilation of `CharacterRepositoryImplTest`, `GetCharactersUseCaseTest`, `CharacterViewModelTest` (and Location/Episode equivalents), since those currently call the old zero-arg signatures. Per D7/Q5 this plan does not fix that — `./gradlew :composeApp:jvmTest` will not compile until the user updates those tests on their own.

---

## 7. Dependency Map

### 7.1 Critical Path

```
T-002 → T-003 → T-006 → T-009 → T-012 → T-015 → T-016 → T-019 → T-022
(0.5-1h)(1-2h)  (1-2h)  (1h)    (0.5-1h) (1-2h)  (2-3h)  (2-3h)  (1h)
```
Total critical path: **≈9.5-15.5 hours** (Character feature only, end-to-end through manual verification; T-001 is already resolved and adds no time). Location and Episode tracks (T-004/T-007/T-010/T-013/T-017/T-020 and T-005/T-008/T-011/T-014/T-018/T-021) are structurally identical and can run in parallel with the Character track once T-002 and T-015 (shared design decisions) land — they don't extend the critical path if resourced in parallel, but on a solo/sequential timeline they add roughly the same 7-9 hours each again.

### 7.2 Parallelizable Work

- **Per-feature tracks (Character / Location / Episode)** — once T-002 (shared `Page<T>` model) and T-015 (shared ViewModel state design) are done, the three features' remaining tasks (T-003/004/005, T-006/007/008, T-009/010/011, T-012/013/014, T-016/017/018, T-019/020/021) are fully independent and can be built in any order or concurrently by different people.
- **T-022 / T-023 (manual verification)** can run in parallel with each other once their shared prerequisites (T-019-021) are done.

### 7.3 External Dependencies

| Dependency | Owner | Status | Impact if Delayed |
|------------|-------|--------|---------------------|
| Rick and Morty public API availability/uptime | Third-party (rickandmortyapi.com), outside team control | Assumed stable (public, well-known API) | Manual verification (T-022/T-023) blocked; low risk given API's maturity |

---

## 8. Risk Assessment

| ID | Risk | Likelihood | Impact | Mitigation | Contingency |
|----|------|------------|--------|------------|-------------|
| R-1 | ~~Generated `kmpgen` client methods don't expose a `page` query parameter~~ | N/A | N/A | **Resolved** — confirmed present via direct code inspection (T-001) | N/A |
| R-2 | Duplicate-request guard (D4) has a subtle race if the user scrolls very fast across the guard-check/state-update boundary | Low | Medium | Manually exercise fast/repeated scrolling during T-022/T-023 | If a race surfaces, add a monotonic "requested page number" check in addition to the boolean flag |
| R-3 | Existing tests fail to compile once signatures change (T-006 through T-014), since they're intentionally left unmodified (D7) | High (expected, accepted) | Low (by design — user explicitly wants to handle this separately) | None — this is an accepted, intentional consequence, not something this plan should mitigate | If it becomes blocking sooner than expected, the user can stub/comment the broken test files temporarily; otherwise handled in the user's own follow-up test-writing work |
| R-4 | `LazyListState` "last item visible" detection (D3) behaves differently across Android vs Desktop (different `LazyColumn` recomposition timing) | Low | Medium | Manual verification on both targets (T-022 AND T-023), not just one | Adjust the visibility threshold (e.g. trigger 1 item early) if Desktop/Android behave inconsistently |
| R-5 | `info.count`/`pages` might not perfectly match `results.size × pages` at API boundaries (e.g. a mid-scroll API change) — edge case at the very last page | Low | Low | Rely on `info.next == null` (D1/business rule) rather than computed page-count math, which self-corrects regardless of `count` accuracy | None needed — design already avoids this failure mode |

---

## 9. Testing Strategy

**Out of scope for this plan.** Per the user's explicit direction (D7/Q5), no unit tests are written or modified as part of this implementation — the user is reserving test-writing (fixtures, MockK, parameterized tests covering the pagination guard matrix: `endReached` × `isLoadingNextPage` × scroll-trigger) as separate, self-directed practice for the `unit-test` branch's stated learning objective, and will request help in a future session if needed.

The only verification performed as part of this plan is **manual**:
- **Desktop** (T-022): run `:composeApp:run`, scroll each of the 3 lists to their true end (e.g. 826 characters), confirm continuous page loading, a visible loading indicator while fetching, a correct stop at the last page, and a simple error message if a fetch is made to fail (e.g. by briefly disconnecting network).
- **Android emulator** (T-023): repeat the same checks via `:androidApp:assembleDebug` + install.

No integration test layer, E2E framework, performance testing, or security testing applies to this change (unauthenticated public read-only API, no new attack surface, no scale/latency requirements beyond "don't block the UI thread" which is verified manually).

---

## 10. Definition of Done

- [ ] `ApiService`/`ApiServiceImpl` for Character, Location, and Episode accept a `page: Int` and return a domain `Page<T>` including `hasNextPage`.
- [ ] `Repository` and `UseCase` layers for all 3 features are page-aware end-to-end.
- [ ] Each ViewModel accumulates results across pages, exposes `endReached`/`isLoadingNextPage`/`errorMessage`, and exposes a next-page-load function guarded against duplicates and end-of-data.
- [ ] Each Compose screen triggers the next-page load when the user scrolls to the last visible item, shows a loading indicator while fetching, and shows a simple error message on failure.
- [ ] Manually verified on both Desktop (T-022) and Android emulator (T-023): scrolling to the bottom of each of the 3 lists loads subsequent pages until the true end of data, with no duplicate/overlapping requests observed, and a visible error message on a simulated failure.
- [ ] No regression in non-paginated behavior (initial page-1 load still works identically to today from the user's perspective, just now continues past 20 items).
- [ ] Explicitly **not** required for this task to be considered done: `./gradlew :composeApp:jvmTest` passing, or any test file being updated — that is separate, user-owned follow-up work (D7).

---

## 11. Open Questions (All Resolved)

All open questions from the initial draft were answered by the user; kept here for record with no action items remaining.

| # | Question | Resolution |
|---|----------|------------|
| Q1 | Error handling on a failed "next page" request? | Show a simple, informative error message. Not critical — personal learning project, no retry/backoff needed. See FR-11, NFR-5, D5. |
| Q2 | Reset pagination state on screen re-entry, or retain across navigation? | Keep it simple — reset to page 1 on every (re)mount, no persistence. See FR-12. |
| Q3 | Does the generated `kmpgen` client already support a `page` param? | Confirmed yes via direct inspection of `Services.kt` — `page: Long? = null` already present on all 3 list methods. See T-001, D6. |
| Q4 | Scroll trigger on literal last item, or N-items-before-end prefetch? | Keep it simple — literal last item. See FR-10, D3. |
| Q5 | Should this task also write/modify unit tests (Kotest/MockK)? | No — explicitly out of scope. The user is handling all test work separately as self-directed learning practice. See D7, §9, and the compilation caveat in the note under §6. |
