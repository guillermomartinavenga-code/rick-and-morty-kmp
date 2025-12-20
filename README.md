# Rick and Morty KMP Encyclopedia

This is a Kotlin Multiplatform (KMP) project that demonstrates how to build an application targeting **Android** and **Desktop (JVM for Linux, macOS, and Windows)** with a shared codebase.

The project serves as a practical example of KMP principles, including code sharing, dependency injection, and platform-specific implementations using `expect` and `actual` declarations.

## Project Overview

The application is a simple encyclopedia for the Rick and Morty universe. It fetches data from the public [Rick and Morty API](https://rickandmortyapi.com/) and displays it in three main sections: Characters, Locations, and Episodes.

The core goals of this project are to showcase:

- **Shared Logic:** The majority of the code, including business logic (use cases, repositories) and presentation logic (ViewModels), resides in the `commonMain` source set and is shared across Android and Desktop.
- **Platform-Specific Implementations:** The use of `expect` and `actual` declarations for the `ApiService`. The `expect` keyword in `commonMain` defines a contract, while `actual` in `androidMain` and `jvmMain` provides the concrete implementation (using Retrofit for Android and Ktor for Desktop).
- **Clean Architecture:** The project follows a modern, layered architecture (`data`, `domain`, `presentation`) for better separation of concerns and testability.
- **Declarative UI:** The entire user interface is built with **Jetpack Compose Multiplatform**, allowing the UI to be shared across platforms with minimal platform-specific adjustments.

## Features & API Use Cases

The application consumes the public [Rick and Morty API](https://rickandmortyapi.com/api) and implements the following use cases:

- **Characters (`/character` endpoint):**
  - **UC-C1:** Fetches and displays a list of all characters.
- **Locations (`/location` endpoint):**
  - **UC-L1:** Fetches and displays a list of all locations.
- **Episodes (`/episode` endpoint):**
  - **UC-E1:** Fetches and displays a list of all episodes.

The UI consists of a main screen with three buttons to navigate to each respective section. Each section screen displays the data in a list and includes a top app bar with a title and a functional back button.

## Unit Testing

The project includes unit tests for the shared business logic located in the `commonTest` source set. The tests cover:

- **Use Cases:** Verifying that the use cases correctly interact with their repositories.
- **Repositories:** Ensuring the repositories correctly call the `ApiService`.
- **ViewModels:** Confirming that the ViewModels update their state correctly after data is loaded.

To run all unit tests, execute the following Gradle task from the terminal:

```shell
./gradlew :composeApp:jvmTest
```

## Build and Run Android Application

To build and run the development version of the Android app, use the `composeApp` run configuration from the run widget in your IDE’s toolbar or build it directly from the terminal:

- On macOS/Linux:
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- On Windows:
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

## Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the `composeApp` run configuration from the run widget in your IDE’s toolbar or run it directly from the terminal:

- On macOS/Linux:
  ```shell
  ./gradlew :composeApp:run
  ```
- On Windows:
  ```shell
  .\gradlew.bat :composeApp:run
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
