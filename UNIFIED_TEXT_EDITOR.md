# Unified Text Editor - UX Improvement

## Overview
Refactored the rich text editor from a block-based approach to a unified text editor for better user experience. Text and todos now flow naturally in a single editable area, similar to popular note-taking apps.

## Changes Made

### 1. **New UnifiedTextEditor Component**
   - **Location**: `feature/notes/src/main/java/com/cbo/notes/presentation/component/richtext/UnifiedTextEditor.kt`
   - **Purpose**: Single text area for all text and todo items
   - **Features**:
     - Continuous text editing without block boundaries
     - Inline todo items with checkboxes (☐/☑ prefix)
     - Built-in formatting toolbar
     - Automatic conversion between text and block structure

### 2. **Updated RichTextEditor**
   - **Location**: `feature/notes/src/main/java/com/cbo/notes/presentation/component/richtext/RichTextEditor.kt`
   - **Changes**:
     - Simplified toolbar (only "Add Image" button)
     - Uses `UnifiedTextEditor` for text/todos
     - Keeps image blocks separate for visual clarity
     - Cleaner, more intuitive interface

## How It Works

### Text Representation
```
Regular text line 1
Regular text line 2
☐ Unchecked todo item
☑ Checked todo item
More regular text
```

### Block Conversion
- **Text to Blocks**: Parses lines and creates appropriate block types
  - Lines starting with `☐ ` → `TodoBlock(isChecked = false)`
  - Lines starting with `☑ ` → `TodoBlock(isChecked = true)`
  - Other lines → `TextBlock`
- **Blocks to Text**: Converts blocks back to unified text format
  - `TodoBlock` → Adds ☐/☑ prefix
  - `TextBlock` → Plain text line
  - `ImageBlock` → Handled separately

### Formatting Toolbar
Located within the text editor card:
- **Checkbox Icon**: Toggle current line as todo
- **Bold**: Apply bold formatting (coming soon)
- **Italic**: Apply italic formatting (coming soon)
- **Underline**: Apply underline (coming soon)
- **Strikethrough**: Apply strikethrough (coming soon)

## User Experience Improvements

### Before (Block-Based):
- Each paragraph was a separate card
- Todo items were individual blocks
- Adding new blocks required toolbar clicks
- Visual clutter with multiple cards
- Disrupted writing flow

### After (Unified):
- Single continuous text area
- Natural typing experience
- Toggle any line as todo with toolbar button
- Clean, minimal interface
- Better for long-form notes

## Usage

### Writing Text
1. Click in the text area and start typing
2. Press Enter for new lines
3. Text flows naturally like a traditional text editor

### Creating Todos
1. Type your text on any line
2. Click the checkbox icon in the toolbar
3. Line converts to todo with checkbox
4. Click again to toggle it back to regular text

### Adding Images
1. Click the image icon in the top toolbar
2. Select an image from your device
3. Image appears as a separate block below the text
4. Click image to view full screen

### Formatting Text (Planned)
1. Select text you want to format
2. Click formatting buttons in toolbar
3. Styles applied to selected text

## Technical Details

### State Management
- `TextFieldValue` for cursor position and selection
- Automatic sync between text and block structure
- Preserves block IDs for stability

### Performance
- Efficient block conversion
- Only updates when content changes
- Maintains image blocks separately

### Data Structure
- Still uses `NoteContent` with blocks internally
- Backward compatible with existing notes
- Supports migration from block-based notes

## Benefits

1. **Better UX**: More intuitive and natural writing experience
2. **Cleaner UI**: Less visual clutter, more focus on content
3. **Faster Workflow**: No need to switch between block types
4. **Familiar Feel**: Similar to popular note apps (Notion, Bear, etc.)
5. **Backward Compatible**: Existing notes work seamlessly

## Future Enhancements

1. **Text Formatting Implementation**: Complete bold, italic, underline support in unified editor
2. **Markdown Support**: Parse markdown syntax (**, *, _, ~~)
3. **Drag-and-Drop Images**: Inline image placement
4. **Keyboard Shortcuts**: 
   - `Cmd/Ctrl + B` for bold
   - `Cmd/Ctrl + I` for italic
   - `Cmd/Ctrl + K` for link
5. **Smart Lists**: Auto-continue lists when pressing Enter
6. **Code Blocks**: Syntax highlighting for code

## Migration Notes

- Existing notes automatically work with the new editor
- Old block-based structure preserved in database
- Conversion happens on-the-fly during editing
- No data migration required

## Testing

Test the following scenarios:
1. Create a new note with mixed text and todos
2. Edit an existing note
3. Toggle lines between text and todo
4. Add images
5. Save and reload notes
6. Switch between rich text and plain text mode

---

**Last Updated**: October 21, 2025
**Version**: 1.0

