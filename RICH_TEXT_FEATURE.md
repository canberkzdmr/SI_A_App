# Rich Text Notes Feature

## Overview

This document describes the implementation of the rich text editing feature for the Notes module, allowing users to create styleable notes with text formatting, to-do items, and images.

## Features

### 1. Text Formatting
- **Bold**: Apply bold formatting to text
- **Italic**: Apply italic formatting to text  
- **Underline**: Add underline to text
- **Strikethrough**: Strike through text

### 2. To-Do Items
- Create checklist items within notes
- Check/uncheck to-do items
- Visual distinction for completed items

### 3. Images
- Add images to notes via image picker
- Optional image descriptions
- Image preview in edit and view modes

## Architecture

The implementation follows Clean Architecture principles with clear separation of concerns:

### Domain Layer (`feature/notes/src/main/java/com/cbo/notes/domain/model/`)

#### NoteContent.kt
- **`NoteContent`**: Container for structured note content with multiple blocks
- **`ContentBlock`**: Sealed class representing different content types:
  - `TextBlock`: Rich text with styling
  - `TodoBlock`: Checklist item with checked state
  - `ImageBlock`: Image with URI and optional description
- **`TextStyleRange`**: Defines text style application ranges
- **`TextStyle`**: Enum for BOLD, ITALIC, UNDERLINE, STRIKETHROUGH

#### Note.kt
- Enhanced with `richContent: NoteContent?` field
- Maintains backward compatibility with plain `content: String`
- Helper method `getDisplayContent()` for seamless content access

### Data Layer

#### NoteEntityMapper.kt
- Handles JSON serialization/deserialization of `NoteContent`
- Automatic detection of rich vs plain content
- Graceful fallback for parsing errors

#### Database
- No schema changes required - JSON stored as string in existing `content` field
- Backward compatible with existing notes

### Presentation Layer

#### Components (`feature/notes/src/main/java/com/cbo/notes/presentation/component/richtext/`)

1. **RichTextEditor.kt**
   - Main editor component
   - Toolbar for adding different block types
   - List-based block management

2. **TextBlockEditor.kt**
   - Text block editing with formatting toolbar
   - Toggle buttons for text styles
   - Focus-based UI enhancements

3. **TodoBlockEditor.kt**
   - Checkbox + text input
   - Visual indication of completion

4. **ImageBlockEditor.kt**
   - Image picker integration
   - Image preview with ContentScale.Crop
   - Optional description field

5. **RichContentPreview.kt**
   - Displays rich content in note cards
   - Compact preview with ellipsis
   - Visual indicators for todos and images

#### EditNoteViewModel.kt
- New methods:
  - `updateRichContent(richContent: NoteContent)`
  - `toggleEditorMode()` - Switch between plain and rich text
- Enhanced save logic to handle both content types
- Automatic plain text generation for search

#### EditNoteScreen.kt
- Toggle button in app bar to switch editor modes
- Conditional rendering of RichTextEditor vs plain TextField
- Seamless integration with existing category/tag selection

#### NoteCard.kt
- Enhanced to display rich content preview
- Maintains backward compatibility with plain text notes

## Usage

### Creating a Rich Text Note

1. Open the note editor
2. The rich text editor is enabled by default for new notes
3. Use the toolbar to add:
   - Text blocks (T icon)
   - To-do items (Checkbox icon)
   - Images (Image icon)

### Formatting Text

1. Focus on a text block
2. Tap the formatting icon to show the toolbar
3. Select desired formatting (Bold/Italic/Underline/Strikethrough)
4. Currently applies to entire block (selection support can be added later)

### Adding To-Do Items

1. Tap the checkbox icon in the toolbar
2. Type the to-do text
3. Check/uncheck to mark completion

### Adding Images

1. Tap the image icon in the toolbar
2. Select an image from the device
3. Optionally add a description

### Switching Editor Modes

- Tap the editor mode toggle button (T/Bold icon) in the app bar
- Plain text mode: Simple text field
- Rich text mode: Block-based editor with formatting

## Technical Details

### Serialization

Rich content is serialized to JSON using Kotlinx Serialization:

```json
{
  "blocks": [
    {
      "type": "com.cbo.notes.domain.model.ContentBlock.TextBlock",
      "id": "1234567890",
      "text": "Hello World",
      "styles": [
        {
          "start": 0,
          "end": 5,
          "style": "BOLD"
        }
      ]
    },
    {
      "type": "com.cbo.notes.domain.model.ContentBlock.TodoBlock",
      "id": "1234567891",
      "text": "Buy groceries",
      "isChecked": false,
      "styles": []
    }
  ]
}
```

### Backward Compatibility

- Existing plain text notes continue to work
- Rich content is optional (`richContent: NoteContent?`)
- Automatic conversion when switching editor modes
- Plain text maintained in `content` field for search

### Dependencies Added

```kotlin
// build.gradle.kts (feature/notes and core-database)
plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

// libs.versions.toml
[versions]
kotlinx-serialization-json = "1.6.0"

[libraries]
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization-json" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

## Future Enhancements

1. **Text Selection Support**: Apply formatting to selected text ranges
2. **More Block Types**: 
   - Code blocks with syntax highlighting
   - Quote blocks
   - Headers (H1, H2, H3)
   - Bullet/numbered lists
3. **Rich Text Search**: Search within formatted content
4. **Export**: Export to Markdown, HTML, or PDF
5. **Collaborative Editing**: Real-time collaboration support
6. **Voice-to-Text**: Add voice input for text blocks
7. **Link Support**: Embed clickable links
8. **Table Support**: Add table blocks

## Testing Recommendations

### Unit Tests
- Test NoteContent serialization/deserialization
- Test NoteEntityMapper conversion logic
- Test backward compatibility with plain text notes

### Integration Tests
- Test note creation with rich content
- Test editor mode switching
- Test note updates preserving rich content

### UI Tests
- Test RichTextEditor interactions
- Test formatting toolbar
- Test image picker flow
- Test todo item checking

## Security Considerations

1. **Image Storage**: Images are stored by URI reference only
2. **XSS Prevention**: No HTML rendering, all content is structured data
3. **Input Validation**: Text length limits should be enforced
4. **File Access**: Image picker respects Android permissions

## Performance Considerations

1. **Lazy Loading**: LazyColumn for efficient block rendering
2. **Image Loading**: Coil library for optimized image loading
3. **JSON Size**: Keep block count reasonable (recommend < 100 blocks per note)
4. **Database**: Content stored as TEXT, indexed for search

## Accessibility

- All formatting buttons have content descriptions
- Semantic structure maintained for screen readers
- High contrast support for styled text
- Todo items support keyboard navigation

## Conclusion

The rich text notes feature has been successfully implemented following Clean Architecture principles, maintaining backward compatibility, and providing a solid foundation for future enhancements. The modular design allows for easy addition of new block types and formatting options.

