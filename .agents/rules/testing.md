# Testing Guidelines

## Minimum Test Gereksinimleri
Her yeni feature modülünde en az şu testler yazılmalıdır:
- **ViewModel testleri** — UI state geçişleri, hata durumları
- **UseCase testleri** — İş mantığı doğrulaması
- **Repository testleri** — Veri katmanı doğrulaması (opsiyonel ama önerilir)

## Unit Tests
- Use JUnit 4, Mockito for mocks
- Use `kotlinx-coroutines-test` for async testing
- Use `InstantTaskExecutorRule` for LiveData
- Test both success and failure paths

## UI Tests
- Use Espresso for instrumentation tests
- Use Compose testing utilities
- Test critical user flows (login, note creation, navigation)

## Integration Tests
- Test repository flows with in-memory Room databases
- Verify all database migrations with `MigrationTest`

## Mocking
- Mock external dependencies to isolate tests
- Use `@Mock` annotation with Mockito
- Use `mockito-kotlin` for idiomatic Kotlin mocking

## Test Dosya Konumu
```
feature/<name>/
├── src/test/java/...           ← Unit tests
└── src/androidTest/java/...    ← Instrumentation tests
```
