package com.vincent.chat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.vincent.chat.theme.*
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.viewModels.AppViewModel
import composechat.composeapp.generated.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListUsers(
    viewModel: AppViewModel = koinInject<AppViewModel>()
) {
    val appState = koinInject<AppState>()
    val currentUser by appState.currentUser.collectAsState()
    val messages = appState.messages
    val usersList by viewModel.usersList.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chats", style = HeadLineLarge)

        Box(
            modifier = Modifier.zIndex(1f).fillMaxWidth().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                textStyle = MessageStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            .padding(vertical = 0.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null)
                        Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search...", style = LocalTextStyle.current.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.5f),
                                    )
                                )
                            }
                            innerTextField()
                        }
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                        else IconButton(onClick = { }) {}
                    }
                })
        }

        if (usersList.isEmpty()) {
            val composition by rememberLottieComposition {
                LottieCompositionSpec.JsonString(
                    Res.readBytes("files/empty_state.json").decodeToString()
                )
            }
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = rememberLottiePainter(
                        composition = composition, iterations = Compottie.IterateForever
                    ), contentDescription = "Lottie animation", modifier = Modifier.fillMaxWidth(0.6f)
                )

                Text("There is no one to chat with", style = TitleLineBig.copy(fontSize = 16.sp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(usersList) { index, user ->
                    ListItem(
                        leadingContent = {
                            AsyncImage(
                                model = "https://i.pravatar.cc/150?u=$user",
                                contentDescription = "Avatar$index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(3.dp)
                                    .clip(CircleShape).background(
                                        MaterialTheme.colorScheme.onPrimary.copy(0.2f), CircleShape
                                    ),
                            )
                        },
                        headlineContent = { Text(user, style = TitleLineLarge) },
                        supportingContent = { Text(messages[user]?.last()?.text ?: "No Messages") },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp).clip(RoundedCornerShape(8.dp)).clickable(
                            onClick = { appState.selectedUser.value = user })
                    )

                }
            }
        }

        // end row
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${currentUser?.name}", style = HeadLineMedium.copy(
                    fontSize = 20.sp
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.AutoMirrored.Default.ExitToApp, null, tint = Color.Unspecified)
                }
                IconButton(onClick = { viewModel.toggleShowConfigDialog() }) {
                    Icon(Icons.Default.Settings, null, tint = Color.Unspecified)
                }
            }
        }
    }
}