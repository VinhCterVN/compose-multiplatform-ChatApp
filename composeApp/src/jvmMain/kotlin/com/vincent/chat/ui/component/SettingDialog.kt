package com.vincent.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vincent.chat.theme.HeadLineMedium
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.viewModels.AppViewModel
import org.koin.compose.koinInject

@Composable
fun SettingDialog(
    viewModel: AppViewModel = koinInject<AppViewModel>(),
    appState: AppState = koinInject<AppState>()
) {
    val networkConfigShown = appState.networkConfigShown.collectAsState()
    val networkConfig = appState.networkConfig.collectAsState()
    var tempHost by remember { mutableStateOf(networkConfig.value.host) }
    var tempPort by remember { mutableStateOf(networkConfig.value.port) }

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

            // 2 text fields for temp...

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
                value = tempPort.toString(),
                onValueChange = { tempPort = it.toInt() },
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Port") },
                leadingIcon = { Icon(Icons.Default.Cable, null) },
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

            ElevatedButton(
                onClick = { viewModel.setNetworkConfig(tempHost, tempPort) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.75f),
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