---
description: Kod içerisinde navigation route'larının hardcoded (sabit) stringler yerine Navigation katmanında tanımlı sabitler (constants) üzerinden kullanılmasını sağlayan kural.
---

# Navigation Route Constants Rule

When working with navigation in Jetpack Compose or any Android Navigation component:

- **NEVER** use hardcoded string literals (e.g., `"calendar"`, `"map"`, `"notes"`) for navigation routes or arguments inside UI components, ViewModels, or Navigation Hosts.
- **ALWAYS** define navigation route names as `const val` inside the respective feature's Navigation file (e.g., `NotesNavigation.kt` -> `const val CALENDAR_ROUTE = "calendar"`).
- **ALWAYS** import and use these constants across the application (e.g., `navController.navigate(CALENDAR_ROUTE)` instead of `navController.navigate("calendar")`).

This ensures type safety, enables easy refactoring, and avoids hidden bugs related to typos in route definitions.
