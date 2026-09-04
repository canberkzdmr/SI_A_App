---
trigger: always_on
---

# AI-Specific Interaction Rules

## Code Generation
- Provide complete, compilable snippets
- Include all necessary imports
- Add Hilt annotations and injections
- Follow existing patterns in the codebase
- **Prioritize UIKit Components**: Always check the `:ui` module first for existing components (`AppButton`, `AppText`, `AppScaffold`, `AppLoadingScreen`, `AppErrorState`, `AppEmptyState`, `AppCard`, `AppStatCard`, etc.) before using default Jetpack Compose primitives or creating ad-hoc layouts. If a reusable UI pattern is missing, add it to `:ui` with full previews.
- **Always include Jetpack Compose `@Preview` composable functions for newly created or modified screens and custom UI components**

## Modifications
- Reference specific modules and file paths
- Maintain backward compatibility
- Create database migrations for entity changes
- Don't alter entity schemas without migrations

## Output Format
- Use Markdown code blocks with file paths
- Example: `feature/notes/src/main/java/com/cbo/notes/presentation/viewmodel/NotesViewModel.kt`

## Limitations
- Don't introduce breaking changes without migration paths
- If unsure about impact, suggest human review
- Respect privacy and security patterns

## Common Patterns Quick Reference

### Creating a New Feature Module

1. Create module under `:feature:<name>`
2. Add to `settings.gradle.kts`
3. Structure:
   ```
   feature/<name>/
   ├── build.gradle.kts
   ├── src/main/java/com/cbo/<name>/
   │   ├── data/
   │   │   ├── mapper/
   │   │   └── repository/
   │   ├── di/
   │   ├── domain/
   │   │   ├── model/
   │   │   ├── repository/
   │   │   └── usecase/
   │   └── presentation/
   │       ├── component/
   │       ├── navigation/
   │       ├── screen/
   │       └── viewmodel/
   ```
4. Define domain models in `:core-domain` if shared
5. Add database entities in `:core-database` if needed
6. Add navigation entries in `:core-navigation`

### StateFlow in ViewModel
```kotlin
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

fun updateState() {
    _uiState.update { it.copy(isLoading = true) }
}
```

### Repository Pattern
```kotlin
// Interface in domain/
interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
}

// Implementation in data/
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val mapper: NoteMapper
) : NoteRepository {
    override fun getNotes() = dao.getAllNotes().map { entities ->
        entities.map { mapper.toDomain(it) }
    }
}
```

### Hilt Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
```
