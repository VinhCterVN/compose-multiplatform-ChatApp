package com.vincent.chat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.vincent.chat.theme.MessageStyle


@Composable
fun CustomTextField(
	message: MutableState<String>,
	selectedFile: MutableState<String>,
	modifier: Modifier = Modifier,
	placeholderText: String = "Placeholder",
	shape: Shape = RoundedCornerShape(12.dp),
	leadingIcon: (@Composable () -> Unit)? = null,
	trailingIcon: (@Composable () -> Unit)? = null,
	fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
	onSend: (String) -> Unit = {}
) {

	if (selectedFile.value.isNotEmpty()) {

	}

	BasicTextField(
		modifier = Modifier.fillMaxWidth()
			.onKeyEvent { event ->
				if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
					onSend(message.value)
					true
				} else false
			}
			.then(modifier),
		value = message.value,
		onValueChange = {
			message.value = it
		},
		singleLine = false,
		maxLines = 5,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
		textStyle = MessageStyle,
		decorationBox = { innerTextField ->
			Row(
				modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape)
					.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically
			) {
				if (leadingIcon != null) leadingIcon()
				Box(Modifier.weight(1f).padding(horizontal = 8.dp)) {
					if (message.value.isEmpty()) {
						Text(
							text = placeholderText, style = LocalTextStyle.current.copy(
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = fontSize
							)
						)
					}
					innerTextField()
				}
				if (trailingIcon != null) trailingIcon()
			}
		})
}