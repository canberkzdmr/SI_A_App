package com.cbo.notes.presentation

/**
 * View mode for displaying notes in different layouts
 */
enum class ViewMode {
    LIST, GRID
}

/**
 * Sort order options for notes
 */
enum class SortOrder {
    UPDATED_DESC, UPDATED_ASC,
    CREATED_DESC, CREATED_ASC,
    TITLE_ASC, TITLE_DESC
}
