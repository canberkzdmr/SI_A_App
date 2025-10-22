# Selection-Based Text Formatting Guide

## Overview
You can now apply text styles to specific words or phrases, not just entire blocks. Select any portion of text and apply Bold, Italic, Underline, or Strikethrough.

## How to Use

### **Method 1: Select Text and Format**
1. **Type some text**: "Hello beautiful world"
2. **Select a word**: Long-press or double-tap "beautiful"
3. **Open formatting toolbar**: Click "Format" button
4. **Click Bold**: The word "beautiful" becomes **bold**
5. **Result**: "Hello **beautiful** world"

### **Method 2: Format Different Parts**
1. **Type**: "This is amazing"
2. **Select** "This" → Click **Bold** → "**This** is amazing"
3. **Select** "amazing" → Click **Italic** → "**This** is *amazing*"
4. **Result**: Multiple styles in one block!

### **Method 3: No Selection = Whole Block**
1. **Type**: "Make everything bold"
2. **Don't select anything** (just click in the text)
3. **Click Bold**: Entire text becomes bold
4. **Result**: "**Make everything bold**"

### **Method 4: Combine Multiple Styles**
1. **Type**: "Important note"
2. **Select** "Important"
3. **Click Bold** → "**Important** note"
4. **Keep "Important" selected** 
5. **Click Underline** → "<u>**Important**</u> note"
6. **Result**: Same text can be both bold AND underlined!

## Features

### ✨ **Visual Feedback**
- **Selected text** shows standard Android selection handles
- **Active buttons** highlight in blue
- **Styled text** displays in real-time as you type
- **Multiple selections** supported (one at a time)

### 🎨 **Supported Styles**
- **Bold** (Weight: Bold)
- *Italic* (Slant: Italic)
- <u>Underline</u> (Decoration: Underline)
- ~~Strikethrough~~ (Decoration: Line-through)

### 🔄 **Style Combinations**
You can apply multiple styles to the same text:
- **Bold + Italic** = **_Bold Italic_**
- **Bold + Underline** = **<u>Bold Underlined</u>**
- **Italic + Strikethrough** = *~~Italic Strikethrough~~*
- **All four** = **<u>*~~All styles~~*</u>**

### 📍 **Smart Button States**
The formatting buttons show different states:
- **Active** (Blue background) = Style is applied to selected text
- **Inactive** (Gray) = Style is not applied
- **Mixed** = If you select text with and without the style, button shows inactive

## Examples

### Example 1: Emphasize Key Words
```
Original: "The project deadline is Friday"
Action:   Select "deadline" → Bold
Result:   "The project **deadline** is Friday"
```

### Example 2: Multiple Highlights
```
Original: "Red apples and green bananas"
Action 1: Select "Red" → Bold
Action 2: Select "green" → Italic
Result:   "**Red** apples and *green* bananas"
```

### Example 3: Nested Styles
```
Original: "This is very important"
Action 1: Select "very important" → Bold
Action 2: Keep selection → Underline  
Result:   "This is <u>**very important**</u>"
```

### Example 4: Partial Styling
```
Original: "Meeting at 2pm in Room 301"
Action 1: Select "2pm" → Bold
Action 2: Select "Room 301" → Bold + Underline
Result:   "Meeting at **2pm** in <u>**Room 301**</u>"
```

## Tips & Tricks

### 💡 **Pro Tips**

1. **Double-tap** to select a word quickly
2. **Triple-tap** to select entire line (on some devices)
3. **Long-press** and drag to select multiple words
4. **No selection** = formats entire block
5. **Click button again** to remove that style from selection

### ⚡ **Quick Workflows**

**For Titles:**
```
1. Type title
2. Select all (long-press, drag to end)
3. Click Bold
4. Click Underline
→ <u>**Your Bold Underlined Title**</u>
```

**For Emphasis:**
```
1. Type sentence
2. Select key phrase
3. Click Italic
→ Subtle emphasis without being too bold
```

**For Strikethrough (Completed Items):**
```
1. Type task
2. Complete it
3. Select task text
4. Click Strikethrough
→ ~~Completed task~~
```

## Technical Details

### How It Works
1. **Selection Tracking**: Editor monitors your text selection (start/end positions)
2. **Style Application**: Clicking a format button creates a `TextStyleRange` for that selection
3. **Live Rendering**: Styled text is rendered using `AnnotatedString` with `SpanStyle`
4. **Persistence**: Styles are saved as ranges (start, end, style) in the database

### Style Data Structure
```kotlin
TextStyleRange(
    start = 5,    // Start position in text
    end = 12,     // End position in text
    style = BOLD  // Style to apply
)
```

### Multiple Style Ranges
A single block can have many style ranges:
```kotlin
ContentBlock.TextBlock(
    text = "Hello beautiful world",
    styles = listOf(
        TextStyleRange(0, 5, BOLD),      // "Hello"
        TextStyleRange(6, 15, ITALIC),   // "beautiful"
        TextStyleRange(6, 15, UNDERLINE) // "beautiful" also underlined
    )
)
```

## Keyboard Shortcuts (Future)

### Planned Shortcuts:
- `Ctrl/Cmd + B` = Bold
- `Ctrl/Cmd + I` = Italic
- `Ctrl/Cmd + U` = Underline
- `Ctrl/Cmd + Shift + X` = Strikethrough

*Note: These are not yet implemented but are on the roadmap*

## Troubleshooting

### "Button not highlighting when I select text"
- Make sure you have text selected (handles visible)
- Try clicking the Format button to show the toolbar
- Check that you're in a text block (not a todo)

### "Style not appearing while typing"
- Styles show immediately for existing text
- New text after selection follows last applied style
- If unclear, select the text and reapply the style

### "Can't remove a style"
- Select the exact same text again
- Click the same button (it should be highlighted)
- The style will be removed from that selection

### "Multiple styles acting weird"
- This is expected - overlapping styles combine
- To remove one style, select text and toggle that specific button
- To start fresh, you may need to remove all styles and reapply

## Best Practices

### ✅ **Do:**
- Use Bold for important words/phrases
- Use Italic for emphasis or foreign words
- Use Underline sparingly (can look like links)
- Use Strikethrough for completed items
- Combine styles for extra emphasis

### ❌ **Don't:**
- Don't overuse formatting (reduces impact)
- Don't underline everything (confusing)
- Don't mix too many styles in one word
- Don't format every word (hard to read)

## Examples in Context

### Meeting Notes:
```
**Project Update** - October 21, 2025

Discussed the following topics:
- **Budget**: Need to review by *Friday*
- **Timeline**: ~~Original deadline~~ Extended to December
- **Team**: New member joins <u>**next Monday**</u>

**Action Items:**
1. Prepare presentation
2. Review documents
3. Schedule follow-up
```

### Study Notes:
```
**Chapter 5: Photosynthesis**

*Photosynthesis* is the process by which plants convert 
**light energy** into **chemical energy**.

Key equation:
6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂

Important: <u>**Chlorophyll**</u> is the pigment that makes
this process possible.
```

### Shopping List:
```
**Grocery List** - Weekend

Produce:
- ~~Apples~~ (bought)
- **Bananas** (urgent - running out!)
- *Organic* lettuce

Dairy:
- Milk
- <u>**Cheese**</u> (don't forget!)
```

---

**Last Updated**: October 21, 2025
**Version**: 1.0
**Feature Status**: ✅ Active

