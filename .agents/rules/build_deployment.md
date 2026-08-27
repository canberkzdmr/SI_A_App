# Build & Deployment

## Gradle
- Use Kotlin DSL (`build.gradle.kts`)
- Enable ProGuard/R8 for release builds (`isMinifyEnabled = true`)
- Use `TYPESAFE_PROJECT_ACCESSORS` for module dependencies

## Build Variants
- `debug` - Development builds
- `release` - Minified production builds

## ProGuard
- Configure rules in `proguard-rules.pro`
- Keep necessary classes for Hilt, Room, serialization
