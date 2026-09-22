# Rick and Morty KMP Encyclopedia

A Kotlin Multiplatform (KMP) encyclopedia app for the Rick and Morty universe, targeting **Android**, **Desktop (JVM for Linux, macOS, and Windows)**, and **iOS** from a single shared codebase. It fetches data from the public [Rick and Morty API](https://rickandmortyapi.com/) and displays Characters, Locations, and Episodes.

This is a personal learning/practice project used to demonstrate a set of KMP engineering objectives — networking, multiplatform build configuration, testing, and more.

## Project Overview

The core goals of this project are to showcase:

- **Shared Logic:** business logic (use cases, repositories) and presentation logic (ViewModels) live in `commonMain` and are shared across Android, Desktop, and iOS.
- **Clean Architecture:** a layered architecture (`data` / `domain` / `presentation`) + MVVM, with data flowing `ApiService → Repository → UseCase → ViewModel → Compose Screen` for each feature (Characters, Locations, Episodes).
- **Generated networking:** the HTTP layer is generated from an OpenAPI spec via the `com.kroegerama.openapi-kmp-gen` Gradle plugin, producing Ktor-based clients/DTOs. Each platform supplies its own Ktor engine (`ktor-client-okhttp` on Android, `ktor-client-cio` on JVM/Desktop, `ktor-client-darwin` on iOS).
- **Platform-Specific Implementations:** `expect`/`actual` declarations where platform code is unavoidable — most notably a small `Logger` abstraction (`android.util.Log` on Android, SLF4J on JVM, `NSLog` on iOS).
- **Declarative UI:** the entire UI is built with **Jetpack Compose Multiplatform**, shared across all three platforms.

## Features & API Use Cases

The application consumes the public [Rick and Morty API](https://rickandmortyapi.com/api) and implements the following use cases:

- **Characters (`/character` endpoint):** fetches and displays a paginated list of all characters.
- **Locations (`/location` endpoint):** fetches and displays a paginated list of all locations.
- **Episodes (`/episode` endpoint):** fetches and displays a paginated list of all episodes.

The UI consists of a main screen with three buttons to navigate to each respective section. Each section screen displays the data in a list with incremental "load next page" pagination, and includes a top app bar with a title and a functional back button.

## Module Structure

- **`composeApp`** — a Kotlin Multiplatform *library* module (`android()`, `jvm()`, `iosArm64()` targets) containing essentially all app code: shared business logic + Compose UI (`commonMain`), and platform code (`androidMain`, `jvmMain`, `iosMain`, `jvmTest`). It also publishes `jvm`/`android`/`iosArm64` artifacts to Maven Local and assembles a local `.xcframework` for iOS — see "Publishing composeApp" below.
- **`androidApp`** — a thin `com.android.application` module that produces the installable Android APK. This is the module to target for Android builds.
- **`iosApp`** — an Xcode project that hosts the shared Compose UI from Swift. It consumes `composeApp` via a local Swift Package (`ComposeAppPackage/`) wrapping a locally-built `.xcframework`, rather than a direct Gradle build-phase embed. Building and running it requires a macOS host with Xcode.
- Desktop distribution (`compose.desktop`) is configured directly inside `composeApp/build.gradle.kts`, since JVM is a target of that same module.

## Unit Testing

Unit tests for the shared business logic live in `composeApp/src/jvmTest` (JVM-executed via JUnit5, not `commonTest` — Kotlin/Native targets can't resolve the JUnit5/MockK test dependencies used here). The tests cover:

- **Use Cases:** verifying correct interaction with their repositories.
- **Repositories:** ensuring correct calls into `ApiService`.
- **ViewModels:** confirming state updates correctly after data is loaded, including pagination and loading-state timing.

To run all unit tests:

```shell
./gradlew :composeApp:jvmTest
```

To run a single test class or method:

```shell
./gradlew :composeApp:jvmTest --tests "com.example.rickandmorty.presentation.character.CharacterViewModelTest"
```

## Build and Run Android Application

Build and install the debug APK via the `androidApp` module:

- On macOS/Linux:
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- On Windows:
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

## Build and Run Desktop (JVM) Application

- On macOS/Linux:
  ```shell
  ./gradlew :composeApp:run
  ```
- On Windows:
  ```shell
  .\gradlew.bat :composeApp:run
  ```

## Build and Run iOS Application

The shared module's `iosArm64` (physical device) target compiles on any host:

```shell
./gradlew :composeApp:compileKotlinIosArm64
```

Assembling the local `.xcframework` that `iosApp` consumes requires a macOS host with Xcode, since the final link step needs Apple's `ld64` linker — this is unconditionally skipped on Linux/Windows:

```shell
./gradlew :composeApp:assembleComposeAppDebugXCFramework
```

From there, open `iosApp/iosApp.xcodeproj` in Xcode, select a physical device, and run — Xcode's build phase re-runs the command above automatically before each build, and `iosApp`'s local Swift Package (`ComposeAppPackage/`) picks up the resulting `.xcframework`. Only the `iosArm64` (physical device) target is configured — there is no simulator target in this project.

## Publishing `composeApp` (Maven Local)

`composeApp` also publishes its `jvm`/`android`/`iosArm64` artifacts to your local Maven repository (`~/.m2`):

```shell
./gradlew publishToMavenLocal
```

This isn't required for any of the run commands above — it exists to demonstrate artifact-based publishing for a Kotlin Multiplatform library.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
