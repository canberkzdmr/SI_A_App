# Dependency Management

## Version Catalogs
All dependencies are declared in `gradle/libs.versions.toml`:
- Do NOT hardcode versions in `build.gradle.kts` files
- Reference using `libs.` prefix (e.g., `libs.hilt.android`)
- When adding new dependencies, always add them to `libs.versions.toml` first
- **Always check `gradle/libs.versions.toml` for current versions** before referencing any library version in code or documentation

## Core Tech Stack
Exact versions are defined in `gradle/libs.versions.toml`. Key technologies:

- **Kotlin** + **KSP** for compilation and annotation processing
- **Jetpack Compose** (BOM-managed) + **Material 3** for UI
- **Hilt** for dependency injection
- **Room** for local database
- **DataStore Preferences** for key-value storage
- **Coroutines** + **Flow** for async/reactive patterns
- **Navigation Compose** for navigation
- **WorkManager** for background processing
- **Biometric** for authentication

## Firebase & Google Services
- **Firebase BOM** — Manages Firebase library versions
- **Firebase Crashlytics** — Crash reporting
- **Firebase Analytics** — Event tracking
- **Firebase Remote Config** — Feature flags and remote configuration
- Firebase event naming convention: `snake_case` (e.g., `note_created`, `category_filtered`)

## Maps & Location
- **Google Maps Compose** — Map rendering in Compose
- **Play Services Location** — Location services
- **Google Places** — Place search and details

## Charts & Visualization
- **Vico** (`compose` + `compose-m3`) — Cartesian charts (column, line)
- Custom chart components (DonutChart, HeatmapChart) in feature modules

## Rich Text
- **RichEditor Compose** (`com.mohamedrejeb.richeditor`) — Rich text editing
- **Jsoup** — HTML parsing
- **Gson** — JSON serialization

## Calendar
- **Kizitonwose Calendar Compose** — Custom calendar views

## Testing
- **JUnit 4** for unit tests
- **Mockito** + **Mockito Kotlin** for mocking
- **Coroutines Test** for async testing
- **Core Testing** for Architecture Components
- **Espresso** for UI tests
- **Compose UI Test** for Compose instrumentation tests

## Dependency Injection
- Use Hilt for all DI
- Annotate ViewModels with `@HiltViewModel`
- Use `@Module` and `@InstallIn` for providers
- Workers use `@HiltWorker` with `@AssistedInject`
