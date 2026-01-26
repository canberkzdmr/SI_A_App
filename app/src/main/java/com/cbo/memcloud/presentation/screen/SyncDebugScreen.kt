package com.cbo.memcloud.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.memcloud.presentation.viewmodel.SyncDebugViewModel
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDebugScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncDebugViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenWithTopBarAndInsets(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                title = { Text("Sync Debug", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    Button(onClick = onNavigateBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.relayUrl,
                onValueChange = viewModel::updateRelayUrl,
                label = { Text("Relay URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.vaultId,
                onValueChange = viewModel::updateVaultId,
                label = { Text("Vault ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.deviceId,
                onValueChange = viewModel::updateDeviceId,
                label = { Text("Device ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.joinToken,
                onValueChange = viewModel::updateJoinToken,
                label = { Text("Join Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.vaultKey,
                onValueChange = viewModel::updateVaultKey,
                label = { Text("Vault Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.register, onCheckedChange = viewModel::updateRegister)
                Text("Register vault (first device only)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = viewModel::setVaultKey, modifier = Modifier.weight(1f)) {
                    Text("Set Key")
                }
                Button(onClick = viewModel::connect, modifier = Modifier.weight(1f)) {
                    Text("Connect")
                }
                Button(onClick = viewModel::disconnect, modifier = Modifier.weight(1f)) {
                    Text("Disconnect")
                }
            }

            OutlinedTextField(
                value = uiState.docId,
                onValueChange = viewModel::updateDocId,
                label = { Text("Doc ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::updateContent,
                label = { Text("Doc content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = viewModel::loadLocalDoc, modifier = Modifier.weight(1f)) {
                    Text("Load local doc")
                }
                Button(onClick = viewModel::sendDoc, modifier = Modifier.weight(1f)) {
                    Text("Send doc")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Events", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(uiState.eventLog) { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}



