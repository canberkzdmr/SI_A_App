# Card Components System

This document describes the comprehensive card component system implemented in the UI module.

## Overview

The card system provides a consistent and flexible way to display content in card-like containers throughout the application. It follows Material Design 3 principles and integrates with the app's design system.

## Components

### Core Components

#### AppCard
The main card component with comprehensive customization options.

```kotlin
AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

#### AppCardHorizontal
Horizontal layout card for side-by-side content.

```kotlin
AppCardHorizontal(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
)
```

### Specialized Components

#### HeaderCard
Card for displaying headers with icons, titles, and content.

```kotlin
HeaderCard(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String = "",
    content: String = "",
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    elevation: Dp? = null,
    onClick: (() -> Unit)? = null
)
```

#### ContentCard
Card for structured content with title, subtitle, and content.

```kotlin
ContentCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
)
```

#### GroupView & GroupItem
Component for grouping related UI items (such as settings, form sections, or metadata blocks) inside an `AppCard` container.

```kotlin
GroupView(
    title = "Account Settings",
    subtitle = "Manage your preferences",
    leadingIcon = Icons.Default.Settings,
    showDividerAfterHeader = true,
    variant = CardVariant.DEFAULT
) {
    GroupItem(
        title = "Profile Information",
        subtitle = "Change name, email",
        leadingIcon = Icons.Default.Person,
        showDivider = true,
        onClick = { /* navigate */ }
    )
}
```


#### ActionCard
Card with primary and secondary actions.

```kotlin
ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String? = null,
    icon: ImageVector? = null,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM
)
```

## Variants

### CardVariant
Defines the visual style of the card:

- **DEFAULT**: Standard card with subtle elevation (2dp)
- **ELEVATED**: Card with higher elevation for emphasis (8dp)
- **OUTLINED**: Card with border instead of elevation
- **FILLED**: Card with primary container background
- **TONAL**: Card with secondary container background
- **SURFACE**: Card with surface container background

### CardSize
Defines the padding and spacing:

- **SMALL**: Compact padding (8dp)
- **MEDIUM**: Standard padding (16dp)
- **LARGE**: Generous padding (24dp)

## Usage Examples

### Basic Card
```kotlin
AppCard {
    AppBody(text = "This is a basic card")
}
```

### Elevated Card with Custom Content
```kotlin
AppCard(
    variant = CardVariant.ELEVATED,
    size = CardSize.LARGE
) {
    AppTitle(text = "Important Information")
    Spacer(modifier = Modifier.height(8.dp))
    AppBody(text = "This card has elevated styling")
}
```

### Header Card
```kotlin
HeaderCard(
    icon = Icons.Default.Info,
    title = "Welcome",
    content = "This is a header card with an icon",
    variant = CardVariant.TONAL
)
```

### Action Card
```kotlin
ActionCard(
    title = "Update Available",
    content = "A new version is available",
    icon = Icons.Default.Update,
    primaryActionText = "Update",
    onPrimaryAction = { /* update logic */ },
    secondaryActionText = "Later",
    onSecondaryAction = { /* dismiss logic */ }
)
```

### Horizontal Card
```kotlin
AppCardHorizontal {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppBody(text = "Left Content")
        AppBody(text = "Right Content")
    }
}
```

## Migration Guide

### From Legacy HeaderCard
```kotlin
// Old
HeaderCard(
    iconSelected = Icons.Default.Info,
    title = "Title",
    content = "Content",
    elevation = 4.dp
)

// New
HeaderCard(
    icon = Icons.Default.Info,
    title = "Title",
    content = "Content",
    variant = CardVariant.DEFAULT,
    elevation = 4.dp
)
```

### From Material3 Card
```kotlin
// Old
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // content
    }
}

// New
AppCard(
    modifier = Modifier.fillMaxWidth(),
    variant = CardVariant.DEFAULT
) {
    // content
}
```

## Best Practices

1. **Use appropriate variants**: Choose the variant that best fits the content's importance and context
2. **Consistent sizing**: Use consistent CardSize across similar components
3. **Accessibility**: Ensure cards with actions have proper click targets
4. **Performance**: Avoid nesting cards unnecessarily
5. **Content hierarchy**: Use appropriate text components (AppTitle, AppBody, etc.) for proper hierarchy

## Design System Integration

The card system integrates with:
- Material Design 3 color scheme
- App typography system
- Consistent spacing and elevation
- Theme-aware colors and styling

## Preview Components

Use the preview components in `CardPreviews.kt` to see all variants and examples in action during development.
