# Kotlin Conventions & Code Style

## Kotlin-First Development
- Write all new code in Kotlin (version defined in `gradle/libs.versions.toml`)
- Use coroutines for async operations (`viewModelScope.launch`)
- Use `Flow` for reactive data streams
- Leverage sealed classes for navigation and error handling
- Use extension functions and data classes for concise, type-safe code

## Naming
- **Variables/Functions**: `camelCase` (e.g., `noteRepository`, `getNotesUseCase`)
- **Classes**: `PascalCase` (e.g., `NotesViewModel`, `NoteEntity`)
- **Constants**: `SCREAMING_SNAKE_CASE` (e.g., `MAX_RETRY_ATTEMPTS`)
- Use descriptive names that indicate purpose

## File Organization
- Group related classes in packages:
  - `data/repository/` - Repository implementations
  - `data/mapper/` - Entity mappers
  - `domain/model/` - Domain models
  - `domain/repository/` - Repository interfaces
  - `domain/usecase/` - Use case classes
  - `presentation/viewmodel/` - ViewModels
  - `presentation/screen/` - Compose screens
  - `presentation/component/` - Reusable UI components
  - `presentation/navigation/` - Navigation graphs
  - `di/` - Hilt modules
  - `worker/` - WorkManager workers

## Formatting
- 4-space indentation
- Use Kotlin's standard formatting
- Organize imports alphabetically
- Avoid wildcard imports except for Compose or testing utilities

## Comments
- Use KDoc for public APIs
- Comment complex business logic
- Avoid redundant comments
