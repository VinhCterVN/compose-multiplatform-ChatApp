package com.vincent.chat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vincent.chat.theme.Roboto
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.viewModels.AppViewModel
import org.koin.compose.koinInject
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

@Composable
fun ChatBar(
	viewModel: AppViewModel = koinInject<AppViewModel>()
) {
	val appState = koinInject<AppState>()
	val currentUser by appState.currentUser.collectAsState()
	val selectedUser by appState.selectedUser.collectAsState()

	val message = remember { mutableStateOf("") }
	val selectedFile = remember { mutableStateOf("") }

	LaunchedEffect(selectedFile.value) {
		if (selectedFile.value.isNotEmpty()) {
			val bitmap = ImageIO.read(File(selectedFile.value))
			if (bitmap == null) {
				println("Failed to load image: ${selectedFile.value}")
				return@LaunchedEffect
			}
		}
	}

	Row(
		modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		IconButton(onClick = {
			val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
			dialog.isVisible = true
			dialog.file?.let { selectedFile.value = dialog.directory + it }
		}) {
			Icon(Icons.Outlined.AttachFile, null)
		}
		Column(
			Modifier.weight(1f),
		) {
			CustomTextField(
				message = message,
				selectedFile = selectedFile,
				modifier = Modifier.fillMaxWidth(),
				leadingIcon = { Icon(Icons.Default.FileUpload, null) },
				placeholderText = "Type a message..."
			) { msg ->
				currentUser?.let { viewModel.sendMessage(it.name, selectedUser!!, msg) }
				message.value = ""
			}

			if (selectedFile.value.isNotEmpty()) {
				val extension = File(selectedFile.value).extension
				if (extension !in listOf("png", "jpg", "jpeg")) {
					Text(
						text = "Unsupported image format: .$extension",
						color = Color.Red,
						fontFamily = Roboto,
						letterSpacing = (-0.2).sp,
						modifier = Modifier.padding(start = 8.dp)
					)
					return@Column
				}

				Spacer(Modifier.height(12.dp))
				Image(
					painter = BitmapPainter(ImageIO.read(File(selectedFile.value)).toComposeImageBitmap()),
					contentDescription = "File",
					contentScale = ContentScale.Fit,
					modifier = Modifier.widthIn(max = 200.dp).clip(RoundedCornerShape(12.dp))
						.clickable(onClick = { selectedFile.value = "" })
				)

			}
		}

		IconButton(onClick = {
			currentUser?.let {
				if (selectedFile.value.isNotEmpty()) {
					viewModel.sendFile(it.name, selectedUser!!, selectedFile.value)
					selectedFile.value = ""
				} else {
					viewModel.sendMessage(it.name, selectedUser!!, message.value)
					message.value = ""
				}
			}
		}) {
			Icon(Icons.AutoMirrored.Default.Send, null)
		}
	}

}