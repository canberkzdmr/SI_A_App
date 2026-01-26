package com.cbo.core.sync

/**
 * JNI surface for the Rust sync-core.
 *
 * Android will ship `libmemcloud_sync_core.so` under `src/main/jniLibs/<abi>/`.
 */
internal object SyncCoreNative {

    private const val LIB_NAME = "memcloud_sync_core"

    @Volatile
    private var loaded: Boolean = false

    fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            System.loadLibrary(LIB_NAME)
            loaded = true
        }
    }

    @JvmStatic external fun protocolVersion(): Int

    @JvmStatic external fun abiVersion(): Int

    @JvmStatic external fun setStorageDir(storageDirUtf8: String): Int

    @JvmStatic external fun setActiveVaultKey(vaultKeyUtf8: String): Int

    @JvmStatic external fun relayConnectAndJoin(
        relayUrlUtf8: String,
        vaultIdUtf8: String,
        deviceIdUtf8: String,
        joinTokenUtf8: String,
        register: Boolean,
    ): Int

    @JvmStatic external fun relayDisconnect()

    @JvmStatic external fun docSetContentAndBroadcast(
        vaultIdUtf8: String,
        fromDeviceIdUtf8: String,
        docIdUtf8: String,
        contentUtf8: String,
    ): Int

    @JvmStatic external fun docLoadAndEmit(
        vaultIdUtf8: String,
        docIdUtf8: String,
    ): Int

    /**
     * Polls the next sync-core event JSON, or returns null if no events are queued.
     *
     * Android uses polling (instead of callbacks) to keep JNI simple and safe.
     */
    @JvmStatic external fun pollEventJson(): String?
}


