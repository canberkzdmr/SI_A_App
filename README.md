# SI_A_App (MemCloud)

An Android app built with Clean Architecture and modularized layers, using Kotlin 2.0, Jetpack Compose, and MVVM. The project follows a clear separation of concerns across Presentation, Domain, and Data layers, with reusable core modules and feature modules for vertical slices.

![Logo](memcloud_logo.png)

## Highlights

- **Architecture**: Clean Architecture, MVVM, coroutines/Flow, repository pattern
- **UI**: Jetpack Compose + Material 3, shared UI components in `:ui`
- **Modularity**: `:core-*` libraries and `:feature:*` vertical features
- **Data**: Room-based persistence in `:core-database`, repositories in `:core-data`
- **Domain**: Use cases and models in `:core-domain`
- **Navigation**: Centralized routes in `:core-navigation`
- **Session**: Secure session helpers in `:core-session`
- **Testing**: Unit and instrumented tests with JUnit/Espresso

## Module Structure

- `:app` — Android app entry; DI wiring, `MainActivity`, navigation host
- `:ui` — Design system and reusable Compose components
- `:core-common` — Shared utilities and base types
- `:core-domain` — Domain models and use cases (UI/data-agnostic)
- `:core-data` — Repository implementations; orchestrates data sources
- `:core-database` — Room entities, DAOs, database
- `:core-navigation` — Route definitions and navigation helpers
- `:core-session` — Session management utilities
- `:feature:splash` — Splash screen flow
- `:feature:login` — Authentication UI/logic following secure validation patterns
- `:feature:notes` — Notes CRUD, categories/tags, search
- `:feature:user` — User profile and related UI

> UI component docs: see `ui/src/main/java/com/cbo/ui/components/cards/README.md` for the card system.

## Tech Stack

- Kotlin 2.0.0, Coroutines, Flow
- Jetpack Compose, Material 3, Coil
- Hilt (DI)
- Room, DataStore
- JUnit, Mockito, Espresso

## Architecture Overview

- **Presentation (Compose + ViewModels)**: State hoisted through ViewModels annotated with `@HiltViewModel`.
- **Domain (Use Cases)**: Pure Kotlin; no Android dependencies; exposes business logic via use cases.
- **Data (Repositories)**: Repositories abstract Room/DataStore or other sources; exposed as Flows/Results.

This structure enables independent evolution of features, improved testability, and clear ownership boundaries.

## Getting Started

### Prerequisites

- Android Studio Iguana or newer
- JDK 17
- Android SDK (minSdk 26, targetSdk 34)

### Setup

1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync and index the project
4. Select the `app` run configuration and run on a device/emulator

### Build

```bash
./gradlew clean assembleDebug
```

### Run Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Conventions

- Follow Kotlin coding conventions; 4-space indentation; descriptive names
- Organize imports, avoid wildcard imports except for Compose
- Public APIs should include KDoc; comment only non-obvious logic
- Avoid blocking calls on main thread; prefer coroutines/Flow

## Feature Notes

- **Notes**: Entities include timestamps and metadata (pinned/archived/favorite). Search and filters are implemented via Room/Flow. UI uses Compose screens with state hoisting.
- **User Management**: Authentication aligns with `:feature:login`. Sessions are managed via `:core-session` and invalidated on logout. Avatars stored as URIs/byte arrays.
- **Navigation**: All destinations registered through `:core-navigation`; `MainNavHost` composes feature graphs.

## Development Tips

- Prefer adding new capabilities as a `:feature:<name>` module; reuse `:ui` and `:core-*` as needed
- Keep domain code free of Android dependencies
- Use repositories for all data access; do not touch DAOs from UI
- Handle errors using `Result` or sealed types; map to user-friendly messages in the UI

## License

TBD

