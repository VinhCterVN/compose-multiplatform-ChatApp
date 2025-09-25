package com.vincent.chat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.vincent.chat.core.model.MessageType
import com.vincent.chat.core.model.Msg
import com.vincent.chat.theme.MessageStyle
import com.vincent.chat.ui.state.AppState
import composechat.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import java.awt.Desktop
import java.io.File
import javax.imageio.ImageIO

@Composable
fun ChatMessages(
    modifier: Modifier = Modifier
) {

    val appState = koinInject<AppState>()
    val currentUser by appState.currentUser.collectAsState()
    val selectedUser by appState.selectedUser.collectAsState()
    val messages = appState.messages

    Box(
        modifier = Modifier.padding(horizontal = 12.dp).then(modifier)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(50.dp)) }
            itemsIndexed(messages[selectedUser] ?: emptyList()) { index: Int, msg: Msg.Send ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (msg.from == currentUser?.name) Arrangement.End else Arrangement.Start
                ) {
                    if (msg.msgType == MessageType.TEXT)
                        Box(
                            Modifier
                                .wrapContentWidth()
                                .widthIn(max = 0.5f * LocalWindowInfo.current.containerSize.width.dp)
                                .background(MaterialTheme.colorScheme.surfaceDim, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) { Text(msg.text, color = MaterialTheme.colorScheme.onSurface, style = MessageStyle) }
                    else {
                        val extension = File(msg.text).extension
                        if (extension in listOf("png", "jpg", "jpeg")) {
                            Image(
                                painter = BitmapPainter(ImageIO.read(File(msg.text)).toComposeImageBitmap()),
                                contentDescription = "Sent Image",
                                modifier = Modifier.widthIn(max = 400.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                            return@Row
                        }
                        Row(
                            Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .clickable(
                                    onClick = {
                                        try {
                                            Desktop.getDesktop().open(File(msg.text))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                )
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
//							AsyncImage(
//								model = "https://img.icons8.com/?size=200&id=lWFsF2cio5WW&format=png",
//								contentDescription = "File",
//								modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
//								contentScale = ContentScale.Fit
//							)

                            Box(Modifier.padding(vertical = 8.dp)) {
                                Image(
                                    painter = painterResource(getFileIcon(msg.text)),
                                    contentDescription = "File",
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Column(
                                Modifier.padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    "File: ${filterFileName(File(msg.text).name) }}",
                                    style = MessageStyle,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text("Click to open", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(50.dp)) }
        }
    }
}
fun filterFileName(path: String): String {
    val first = path.indexOf('_')
    if (first == -1) return ""
    val second = path.indexOf('_', first + 1)
    if (second == -1) return ""
    return path.substring(second + 1)
}

private fun getFileIcon(path: String): DrawableResource {
    val extension = File(path).extension
    return when (extension) {
        in listOf("rar") -> Res.drawable.rar
        in listOf("zip") -> Res.drawable.zip
        in listOf("mp4", "mov", "avi", "mkv") -> Res.drawable.mov_file
        in listOf("mp3", "wav", "flac", "m4a") -> Res.drawable.music_file
        else -> Res.drawable.file
    }
}
