package com.vincent.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import org.koin.compose.koinInject

@Composable
fun LoginDialog(
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val currentUser = koinInject<AppState>().currentUser.collectAsState()
	var username by remember { mutableStateOf("") }

	if (currentUser.value == null) {
		Dialog(onDismissRequest = {}) {
			Column(
				Modifier.fillMaxWidth(1f)
					.heightIn(min = 400.dp)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
					.padding(12.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceEvenly
			) {
				Text("Create an Account to Chat", style = HeadLineMedium)

				TextField(
					value = username,
					onValueChange = { username = it },
					shape = RoundedCornerShape(12.dp),
					placeholder = { Text("Username") },
					leadingIcon = { Icon(Icons.Default.Person, null) },
					maxLines = 1,
					keyboardActions = KeyboardActions(
						onDone = { viewModel.login(username) }
					),
					colors = TextFieldDefaults.colors(
						focusedIndicatorColor = Color.Transparent,
						disabledIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent,
						errorIndicatorColor = Color.Transparent,
						focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
					)
				)

				ElevatedButton(
					onClick = { viewModel.login(username) },
					shape = RoundedCornerShape(8.dp),
					modifier = Modifier.fillMaxWidth(0.75f),
					elevation = ButtonDefaults.buttonElevation(8.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.onSecondaryContainer
					)
				) {
					Text("Login")
				}
			}
		}
	}

}