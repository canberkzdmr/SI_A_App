# Feature Development Rules

## Notes Management

**Entities:**
- `NoteEntity` - Main note with timestamps, soft delete support
- `CategoryEntity` - Note categories
- `TagEntity` - Note tags
- `NoteTagCrossRef` - Many-to-many relationship
- `NoteWithDetails` - Relation query result

**Features:**
- CRUD operations via repositories
- Soft delete with 7-day retention period
- Automatic cleanup via WorkManager
- Search/filtering via Room queries
- Sorting (by date, title) with pinned notes priority
- Rich text content support (HTML)
- Category and tag filtering
- View modes: List, Grid, Compact

**Patterns:**
```kotlin
// Use case pattern
class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}

// ViewModel state pattern
data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val errorMessage: String? = null
)
```

## User Management

**Authentication:**
- Use `:feature:login` patterns
- Validate passwords securely
- Support biometric authentication

**Profiles:**
- Store avatars as URIs or byte arrays in `UserDetail`
- Gender enum support
- User settings per user (view mode, language, biometrics)

**Sessions:**
- Manage via `:core-session`
- `SessionManager` uses DataStore for persistence
- `UserSession` provides reactive session state
- Invalidate on logout

## Database Migrations

**Pattern:**
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Migration SQL statements
    }
}
```

- Always provide migrations for schema changes
- Register in `ALL_MIGRATIONS` array
- Test migrations with in-memory databases
- Use indices for frequently queried columns

## WorkManager Background Tasks

**Pattern:**
```kotlin
@HiltWorker
class MyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val useCase: MyUseCase
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        // Work implementation
    }
}
```

## Navigation

**Pattern:** Sealed class destinations in `:core-navigation`
```kotlin
sealed class AppDestination(val route: String) {
    object Login : AppDestination("login?username={username}")
    object Profile : AppDestination("profile")
    // ...
}
```
