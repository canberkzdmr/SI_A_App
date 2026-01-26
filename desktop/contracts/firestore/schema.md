# Firestore Schema Contract (Mobile + WinUI)

This document defines the **canonical Firestore schema**. Both apps must follow it.

## Guiding rules

- **Identity**: all user-owned data is keyed by `uid` (Firebase Auth UID).
- **Timestamps**: use Firestore `serverTimestamp()` for `createdAt`/`updatedAt` where possible.
- **Soft delete**: do not hard-delete notes by default; set `isDeleted=true` and `deletedAt`.
- **Migration**: every top-level document should include `schemaVersion: number`.

> Note: the current Android codebase uses `Int userId`. With Firebase, prefer `String uid`.

---

## Collections

### 1) `users/{uid}`

**Doc ID**: `{uid}` (Firebase Auth UID)

**Fields**
- `schemaVersion` (number) — start at `1`
- `username` (string)
- `email` (string)
- `avatarUrl` (string|null)
- `isActive` (bool)
- `createdAt` (timestamp)
- `updatedAt` (timestamp)

### 2) `users/{uid}/profile/{profileId}`

Option A (recommended): store as a single doc with fixed ID:
`users/{uid}/profile/main`

**Fields**
- `fullName` (string|null)
- `avatarUrl` (string|null)
- `phoneNumber` (string|null)
- `address` (string|null)
- `bio` (string|null)
- `dateOfBirth` (timestamp|null) *(or store as number epoch millis; pick one and keep consistent)*
- `gender` (string|null) — `"MALE"` | `"FEMALE"`
- `updatedAt` (timestamp)

### 3) `users/{uid}/settings/{settingsId}`

Option A (recommended): single doc: `users/{uid}/settings/main`

**Fields**
- `isFirstLoginDone` (bool)
- `isBiometricsEnabled` (bool)
- `preferredLanguage` (string) — e.g. `"en"`, `"tr"`
- `darkThemeEnabled` (bool)
- `viewMode` (string) — `"LIST"` | `"GRID"` | `"COMPACT"`
- `updatedAt` (timestamp)

### 4) `users/{uid}/notes/{noteId}`

**Doc ID**: `{noteId}` (string, Firestore auto-id recommended)

**Fields**
- `title` (string)
- `content` (string) — store your rich text format consistently (HTML or Markdown)
- `categoryId` (string|null) — references `users/{uid}/categories/{categoryId}`
- `tagIds` (array<string>) — references `users/{uid}/tags/{tagId}`
- `createdAt` (timestamp)
- `updatedAt` (timestamp)
- `isPinned` (bool)
- `isArchived` (bool)
- `isFavorite` (bool)
- `isDeleted` (bool)
- `deletedAt` (timestamp|null)
- `reminderTime` (timestamp|null)

**Indexes (recommended)**
- `isDeleted, updatedAt desc`
- `isPinned desc, updatedAt desc`
- `categoryId, isDeleted, updatedAt desc`
- `isFavorite, isDeleted, updatedAt desc`

### 5) `users/{uid}/categories/{categoryId}`

**Fields**
- `name` (string)
- `color` (string|null) — hex like `"#RRGGBB"`
- `description` (string|null)
- `createdAt` (timestamp)
- `sortOrder` (number)

### 6) `users/{uid}/tags/{tagId}`

**Fields**
- `name` (string)
- `color` (string|null) — hex like `"#RRGGBB"`
- `createdAt` (timestamp)

---

## Conflict avoidance rules (integrity)

- Clients must never write another user’s docs (enforced by Rules).
- Clients should only update:
  - `updatedAt = serverTimestamp()`
  - specific mutable fields
- Consider writing notes through a **Cloud Function** if you want stronger invariants:
  - maximum title length
  - HTML sanitization
  - denormalized `notesCount` on categories




