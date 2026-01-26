## MemCloud sync-core (Rust)

This crate is the **shared sync engine** for MemCloud (WinUI + Android):

- Relay protocol client (WebSocket)
- End-to-end encryption (E2EE)
- CRDT document replication (Automerge)
- Local persistence (currently file-based; SQLite later)

### Android build (creates `.so` files)

Prereqs:
- Rust (`cargo`)
- `cargo-ndk` (`cargo install cargo-ndk`)
- Android NDK installed
- Set `ANDROID_NDK_HOME` to your NDK folder

From repo root:

```powershell
.\native\build-android-sync-core.ps1 -Configuration Release
```

Outputs are copied to:
- `core-sync/src/main/jniLibs/<abi>/libmemcloud_sync_core.so`

### Android usage

The Android wrapper lives in:
- `core-sync/src/main/java/com/cbo/core/sync/`

It calls into JNI exports implemented inside this crate (see `src/lib.rs`).




