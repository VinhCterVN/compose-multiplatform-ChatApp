package com.vincent.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vincent.chat.theme.HeadLineMedium
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.viewModels.AppViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun SettingDialog(
    viewModel: AppViewModel = koinInject<AppViewModel>(),
    appState: AppState = koinInject<AppState>()
) {
    val networkConfigShown = appState.networkConfigShown.collectAsState()
    val networkConfig = appState.networkConfig.collectAsState()
    val isConnected = appState.isConnected.collectAsState()
    var tempHost by remember { mutableStateOf(networkConfig.value.host) }
    var tempPort by remember { mutableStateOf(networkConfig.value.port.toString()) }
    var portError by remember { mutableStateOf(false) }
    var isReconnecting by remember { mutableStateOf(false) }


    LaunchedEffect(networkConfig.value) {
        tempHost = networkConfig.value.host
        tempPort = networkConfig.value.port.toString()
    }


    LaunchedEffect(isConnected.value, isReconnecting) {
        if (isReconnecting && isConnected.value) {
            delay(1000)
            isReconnecting = false
            viewModel.toggleShowConfigDialog(true)
        }
    }

    if (!networkConfigShown.value) return

    Dialog(
        onDismissRequest = { viewModel.toggleShowConfigDialog() }
    ) {
        Column(
            Modifier.fillMaxWidth(1f)
                .heightIn(min = 400.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Config your Network", style = HeadLineMedium)

            TextField(
                value = tempHost,
                onValueChange = { tempHost = it },
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Host") },
                leadingIcon = { Icon(Icons.Default.Language, null) },
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            TextField(
                value = tempPort,
                onValueChange = {
                    tempPort = it

                    portError = try {
                        val port = it.toInt()
                        port <= 0 || port > 65535
                    } catch (e: NumberFormatException) {
                        it.isNotEmpty()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Port") },
                leadingIcon = { Icon(Icons.Default.Cable, null) },
                maxLines = 1,
                isError = portError,
                supportingText = if (portError) {
                    { Text("Port must be a number between 1 and 65535", color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isConnected.value && !isReconnecting) {
                    OutlinedButton(
                        onClick = { viewModel.toggleShowConfigDialog() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                if (isReconnecting) "Reconnecting..." else "Connecting...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ElevatedButton(
                    onClick = {
                        if (tempHost.isNotBlank() && tempPort.isNotBlank() && !portError) {
                            try {
                                val port = tempPort.toInt()
                                if (tempHost != networkConfig.value.host || port != networkConfig.value.port) {
                                    isReconnecting = true
                                    viewModel.setNetworkConfig(tempHost, port)
                                }
                            } catch (e: NumberFormatException) {

                            }
                        }
                    },
                    enabled = tempHost.isNotBlank() && tempPort.isNotBlank() && !portError,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}