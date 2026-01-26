package com.cbo.core.sync

import android.content.Context

/**
 * Minimal Android-side wrapper around the sync-core JNI surface.
 *
 * This will be expanded into proper repository integration (Room <-> CRDT) once Android can
 * build/package the native .so and we add event polling.
 */
class SyncCoreClient(
    private val appContext: Context,
) {
    fun initStorage(): Result<Unit> = runCatching {
        SyncCoreNative.ensureLoaded()
        val dir = appContext.filesDir.resolve("sync-core").absolutePath
        val rc = SyncCoreNative.setStorageDir(dir)
        check(rc == 0) { "setStorageDir failed rc=$rc" }
    }

    fun setVaultKey(vaultKey: String): Result<Unit> = runCatching {
        SyncCoreNative.ensureLoaded()
        val rc = SyncCoreNative.setActiveVaultKey(vaultKey)
        check(rc == 0) { "setActiveVaultKey failed rc=$rc" }
    }

    fun connectRelay(
        relayUrl: String,
        vaultId: String,
        deviceId: String,
        joinToken: String,
        register: Boolean,
    ): Result<Unit> = runCatching {
        SyncCoreNative.ensureLoaded()
        val rc = SyncCoreNative.relayConnectAndJoin(
            relayUrlUtf8 = relayUrl,
            vaultIdUtf8 = vaultId,
            deviceIdUtf8 = deviceId,
            joinTokenUtf8 = joinToken,
            register = register,
        )
        check(rc == 0) { "relayConnectAndJoin failed rc=$rc" }
    }

    fun disconnectRelay() {
        runCatching {
            SyncCoreNative.ensureLoaded()
            SyncCoreNative.relayDisconnect()
        }
    }

    fun pollEventJson(): String? {
        SyncCoreNative.ensureLoaded()
        return SyncCoreNative.pollEventJson()
    }

    fun loadDocAndEmit(vaultId: String, docId: String): Int {
        SyncCoreNative.ensureLoaded()
        return SyncCoreNative.docLoadAndEmit(vaultIdUtf8 = vaultId, docIdUtf8 = docId)
    }

    fun setDocContentAndBroadcast(
        vaultId: String,
        fromDeviceId: String,
        docId: String,
        content: String,
    ): Int {
        SyncCoreNative.ensureLoaded()
        return SyncCoreNative.docSetContentAndBroadcast(
            vaultIdUtf8 = vaultId,
            fromDeviceIdUtf8 = fromDeviceId,
            docIdUtf8 = docId,
            contentUtf8 = content,
        )
    }
}



