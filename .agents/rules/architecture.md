# Architecture & Design Patterns

## Clean Architecture
- Separate concerns into layers: **Presentation**, **Domain**, **Data**
- Use cases in the domain layer must be independent of UI or data implementations
- Repositories abstract data sources and follow the repository pattern
- Each feature module follows the structure: `data/`, `domain/`, `presentation/`, `di/`

## MVVM Pattern
- All UI-related logic must use ViewModels for state management
- Use Jetpack Compose for all views
- State is managed via `StateFlow` and `MutableStateFlow`
- UI State is represented as data classes (e.g., `NotesUiState`)
- Avoid direct UI updates from repositories or use cases
- **Mandatory Previews**: Every newly added screen and UI component MUST include Jetpack Compose `@Preview` composable functions

## Modularity
Project modules structure:
```
:app                    - Main application module
:core                   - Core utilities
:core-common            - Common utilities and extensions
:core-data              - Data layer implementations
:core-database          - Room database, entities, DAOs
:core-domain            - Domain models, use cases, repositories
:core-navigation        - Navigation destinations (sealed class pattern)
:core-session           - Session management, user authentication state
:ui                     - Shared UI components, theme, snackbar
:feature:login          - Login/Register functionality
:feature:user           - User profile management
:feature:splash         - Splash screen
:feature:notes          - Notes CRUD, categories, tags
:feature:statistics     - Note statistics, charts, analytics
```

## Error Handling
- Use Kotlin's `Result` type for operations that can fail
- Use `fold()` pattern for handling success/failure:
```kotlin
result.fold(
    onSuccess = { /* handle success */ },
    onFailure = { /* handle error */ }
)
```
- Use sealed classes for domain-specific exceptions (`LoginException`, `RegistrationException`)
- Show user-friendly messages via `SnackbarManager`

## Performance
- Use coroutines for all async tasks
- Avoid blocking calls on the main thread
- Use `combine()` for multiple Flow operations
- Implement proper indexing on Room entities

## Security
- Never store passwords in plain text
- Use secure session management via `:core-session`
- Support biometric authentication (`androidx.biometric`)
- Invalidate sessions on logout
