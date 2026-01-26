# Desktop + Mobile Integrity Contracts

This folder contains **platform-agnostic contracts** to keep the Android (Kotlin) app and the WinUI (.NET) desktop app consistent while both talk to Firebase.

Because Firestore is **schemaless**, integrity is achieved by:
- a **document schema contract** (what collections exist, what fields exist, and their types)
- **Security Rules** (what is allowed / denied, server-enforced)
- optional **Cloud Functions** (stronger validation, invariants, migrations)
- **design tokens** (colors/typography tokens that both UI stacks can consume)

## Contents

- `firestore/schema.md`  
  Canonical Firestore schema (collections + document IDs + fields + types).

- `firestore/firestore.rules`  
  Baseline security rules enforcing ownership and basic validation.

- `theme/tokens.json`  
  Minimal design tokens (colors + typography) that WinUI and Android can both consume.

## Recommended approach

- **Use Firebase Auth** and make all user-owned documents keyed by the user’s auth UID: `users/{uid}`.
- Treat Firestore as the **single source of truth**.
- Add a `schemaVersion` field to key documents so you can migrate safely.
- Put important business rules in **Security Rules** and/or **Cloud Functions** (not only in clients).




