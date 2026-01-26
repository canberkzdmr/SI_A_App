package com.cbo.memcloud.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.sync.SyncCoreClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SyncDebugUiState(
    val relayUrl: String = "ws://10.0.2.2:5055/ws",
    val vaultId: String = UUID.randomUUID().toString(),
    val deviceId: String = UUID.randomUUID().toString(),
    val joinToken: String = "dev-token-change-me",
    val vaultKey: String = "dev-vault-key-change-me",
    val register: Boolean = true,
    val docId: String = "doc-test",
    val content: String = "",
    val eventLog: List<String> = emptyList(),
    val lastError: String? = null,
)

@HiltViewModel
class SyncDebugViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
) : ViewModel() {

    private val client = SyncCoreClient(appContext)

    private val _uiState = MutableStateFlow(SyncDebugUiState())
    val uiState: StateFlow<SyncDebugUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            client.initStorage()
                .onFailure { e -> appendEvent("initStorage failed: ${e.message}") }
                .onSuccess { appendEvent("initStorage ok") }

            // Poll native events (Android uses polling instead of callbacks).
            while (true) {
                try {
                    val ev = client.pollEventJson()
                    if (ev != null) {
                        appendEvent(ev)
                    } else {
                        delay(50)
                    }
                } catch (t: Throwable) {
                    _uiState.update { it.copy(lastError = t.message ?: "Unknown error") }
                    appendEvent("event pump stopped: ${t.message}")
                    return@launch
                }
            }
        }
    }

    fun updateRelayUrl(v: String) = _uiState.update { it.copy(relayUrl = v) }
    fun updateVaultId(v: String) = _uiState.update { it.copy(vaultId = v) }
    fun updateDeviceId(v: String) = _uiState.update { it.copy(deviceId = v) }
    fun updateJoinToken(v: String) = _uiState.update { it.copy(joinToken = v) }
    fun updateVaultKey(v: String) = _uiState.update { it.copy(vaultKey = v) }
    fun updateRegister(v: Boolean) = _uiState.update { it.copy(register = v) }
    fun updateDocId(v: String) = _uiState.update { it.copy(docId = v) }
    fun updateContent(v: String) = _uiState.update { it.copy(content = v) }

    fun setVaultKey() {
        val key = uiState.value.vaultKey
        client.setVaultKey(key)
            .onFailure { e -> appendEvent("setVaultKey failed: ${e.message}") }
            .onSuccess { appendEvent("setVaultKey ok") }
    }

    fun connect() {
        val s = uiState.value
        client.connectRelay(
            relayUrl = s.relayUrl,
            vaultId = s.vaultId,
            deviceId = s.deviceId,
            joinToken = s.joinToken,
            register = s.register,
        ).onFailure { e ->
            appendEvent("connect failed: ${e.message}")
        }.onSuccess {
            appendEvent("connect ok")
        }
    }

    fun disconnect() {
        client.disconnectRelay()
        appendEvent("disconnect requested")
    }

    fun loadLocalDoc() {
        val s = uiState.value
        val rc = client.loadDocAndEmit(s.vaultId, s.docId)
        appendEvent("docLoadAndEmit rc=$rc")
    }

    fun sendDoc() {
        val s = uiState.value
        val rc = client.setDocContentAndBroadcast(
            vaultId = s.vaultId,
            fromDeviceId = s.deviceId,
            docId = s.docId,
            content = s.content,
        )
        appendEvent("docSetContentAndBroadcast rc=$rc")
    }

    private fun appendEvent(line: String) {
        _uiState.update { st ->
            val next = (st.eventLog + line).takeLast(200)
            st.copy(eventLog = next, lastError = null)
        }
    }
}



