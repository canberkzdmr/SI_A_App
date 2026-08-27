# UIKit Components & Compose Previews

## Shared Components in `:ui` Module (`com.cbo.ui.components.*`)

### Root-Level Components
- `AppButton` — `PrimaryButton`, `SecondaryButton`, `TertiaryButton`, `DestructiveButton`, `AppIconButton`
- `AppText` — `AppHeadline`, `AppTitle`, `AppTitleMedium`, `AppBody`, `AppCaption`, `AppLabel`, `SectionHeader`
- `AppScaffold`, `AppScaffoldWithInsets`, `ScreenWithTopBar`
- `AppTabs` — Tab bar components
- `Chips` — `StatChip`, `ColorDot`
- `Dialog` — Base dialog components
- `TextField` — Custom text fields
- `SearchField` — Search input
- `SelectionButton` — Selection controls
- `Switch` — Custom switch toggle
- `ColorPicker` — Color selection component
- `DatePicker` — Date selection component
- `PhoneNumber` — Phone number input
- `ShimmerBox` — Shimmer loading effect
- `Time` — Time display
- `BottomNavigation` — `AppBottomNavigation`, `BottomNavigationOverlay`, `CenterButton`
- `EdgeToEdgeWrapper` — Edge-to-edge display wrapper

### `components/cards/`
- `AppCard`, `HeaderCard`, `ContentCard`, `ActionCard`, `GroupView`

### `components/display/`
- `AppStatCard`, `AppStatGrid`, `AppMetricCard`, `AppKpiCard`, `AppCompactStatItem`
- `AppProgressCard`, `AppCircularProgressCard`, `AppStepProgress`
- `AppTimeline`, `AppActivityFeed`
- `AppSectionCard`, `AppStreakCard`, `AppInsightBanner`

### `components/states/`
- `AppLoadingScreen`, `AppLoadingOverlay`, `AppLoadingButton`
- `AppErrorState`, `AppEmptyState`, `AppSuccessState`, `AppOfflineState`
- `SkeletonComponents`

### `components/expandable/`
- `Accordion`, `ExpandableCard`, `ExpandableListItem`, `ExpandableText`

### `components/filter/`
- `FilterDropdown`, `FilterExpandable`

### `components/forms/`
- `FieldGroups`, `FormComponents`

### `components/dialogs/`
- `ConfirmationDialogs`, `FormDialogs`

### `components/richtext/`
- `RichTextEditorField`, `RichTextViewer`, `RichTextStyleButton`

### Other
- `snackbar/` — `SnackbarManager`, `SnackbarMessage`, `SnackbarHostProvider`
- `theme/` — Colors, Typography, Theme (`MemCloudApplicationTheme`, `Dimens`)

---

## UIKit Priority Rule (Mandatory)
- **Always check `:ui` first**: Before using default Jetpack Compose / Material 3 primitives (e.g., `Text`, `Button`, `Card`, `Scaffold`, `CircularProgressIndicator`), developers and AI agents MUST check if an existing UIKit component in `:ui` satisfies the use case.
- **Component Mapping Standard**:
  - Raw `Text` ➔ Use `AppHeadline`, `AppTitle`, `AppTitleMedium`, `AppBody`, `AppCaption`, `AppLabel`, `SectionHeader`
  - Raw `Button` / `IconButton` ➔ Use `PrimaryButton`, `SecondaryButton`, `TertiaryButton`, `DestructiveButton`, `AppIconButton`
  - Raw `Scaffold` ➔ Use `AppScaffold`, `AppScaffoldWithInsets`, or `ScreenWithTopBar`
  - Raw `CircularProgressIndicator` (full-screen loading) ➔ Use `AppLoadingScreen` or `AppLoadingOverlay`
  - Custom Error / Empty layouts ➔ Use `AppErrorState`, `AppEmptyState`, `AppOfflineState`
  - Custom stat / metric / info cards ➔ Use `AppStatCard`, `AppMetricCard`, `AppKpiCard`, `AppCard`, `AppSectionCard`
  - Custom shimmer/skeleton ➔ Use `ShimmerBox`, `SkeletonComponents`
  - Custom expandable content ➔ Use `Accordion`, `ExpandableCard`, `ExpandableListItem`

## Hardcoded Renk Kullanımı Kuralı
- Feature modülleri içinde doğrudan `Color(0xFF...)` kullanılmamalıdır
- Tema renkleri `MaterialTheme.colorScheme.*` üzerinden kullanılmalıdır
- Tema dışı semantik renkler gerektiğinde (chart renkleri, KPI renkleri vb.) bunlar `:ui/theme` içinde tanımlanmalıdır:
  ```kotlin
  // ui/theme/AppColors.kt
  object AppColors {
      val chartIndigo = Color(0xFF6366F1)
      val chartCyan = Color(0xFF22D3EE)
      val chartAmber = Color(0xFFF59E0B)
      val chartEmerald = Color(0xFF10B981)
      val chartRose = Color(0xFFF43F5E)
      val chartViolet = Color(0xFF8B5CF6)
  }
  ```
- Feature modülleri bu renkleri `AppColors.chartIndigo` şeklinde referans almalıdır

## Extending UIKit for Reusability
- When building features, if a UI element represents a generic, reusable pattern across the application (e.g. KPI badges, insight/alert banners, chart containers, metric cards), it **must be added to `:ui`** (`ui/src/main/java/com/cbo/ui/components/`) rather than duplicated or kept private inside a single feature module.
- Any newly added UIKit component in `:ui` MUST include `@Preview` composable functions with light/dark theme support.

## Snackbar Pattern
```kotlin
// In ViewModel
snackbarManager.showMessage(
    SnackbarMessage.Success(messageRes = R.string.success_message)
)
```

## Compose Previews (Mandatory for All New Screens & Components)

### Stateless/Stateful Separation
Every screen must decouple the stateful container (`Screen(viewModel: ViewModel)`) from the stateless content (`ScreenContent(uiState: UiState, onAction: (Action) -> Unit)`).

### Mandatory Previews for New Screens
Every newly added screen MUST have `@Preview` composables defined for visual verification in Android Studio.

### Multiple State Previews
Provide previews for essential UI states:
- Default / Content with mock data
- Loading state (`isLoading = true`)
- Empty state (no data available)
- Error or filtered states where applicable
- Light & Dark theme variants (`@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)`)

### Realistic Sample Data
Supply preview helper functions or sample models (e.g., `previewUiState()`, `sampleNotes()`) to make previews realistic and standalone.

### Screen & Compose Preview Pattern (Mandatory)
```kotlin
// 1. Stateful Screen (Entry point with ViewModel)
@Composable
fun FeatureScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

// 2. Stateless Content (Reusable & Previewable)
@Composable
fun FeatureScreenContent(
    uiState: FeatureUiState,
    onAction: (FeatureAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    // UI Layout implementation
}

// 3. Previews (Default, Loading, Dark Mode, etc.)
@Preview(showBackground = true, showSystemUi = true, name = "Feature • Content")
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Feature • Dark")
@Composable
private fun FeatureScreenPreview() {
    MemCloudTheme {
        FeatureScreenContent(
            uiState = FeatureUiState(items = sampleItems()),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
```
