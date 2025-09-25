package com.vincent.chat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vincent.chat.theme.HeadLineMedium
import com.vincent.chat.theme.TitleLineBig
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.state.LocalBottomSheetScaffoldState
import composechat.composeapp.generated.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatArea(
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val appState = koinInject<AppState>()
    val selectedUser by appState.selectedUser.collectAsState()
    val isConnected by appState.isConnected.collectAsState()
    val localSheet = LocalBottomSheetScaffoldState.current

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/empty.json").decodeToString()
        )
    }

    val disconnectComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/error-with-cat.json").decodeToString()
        )
    }

    if (selectedUser.isNullOrEmpty()) {
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceBright),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isConnected)
                Image(
                    painter = rememberLottiePainter(
                        composition = composition,
                        iterations = Compottie.IterateForever
                    ), contentDescription = "Lottie animation", modifier = Modifier.fillMaxWidth(0.6f)
                )
            else
                Image(
                    painter = rememberLottiePainter(
                        composition = disconnectComposition,
                        iterations = Compottie.IterateForever
                    ), contentDescription = "Lottie animation", modifier = Modifier.fillMaxWidth(0.6f)
                )
//            Text("Select a chat to start messaging", style = TitleLineBig)
            Text(if (isConnected) "Select a chat to start messaging" else "You are Disconnected", style = TitleLineBig)
//            Text("Window State: ${windowState.size.width} - ${windowState.size.height}", style = TitleLineBig)
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            AsyncImage(
                                model = "https://i.pravatar.cc/150?u=$selectedUser",
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(54.dp)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(3.dp).clip(CircleShape).aspectRatio(1f)
                            )
                            Text("$selectedUser", style = HeadLineMedium)
                        }
                    }, actions = {
                        IconButton(
                            onClick = { scope.launch { localSheet.bottomSheetState.expand() } }) {
                            Icon(Icons.Default.Settings, null)
                        }
                        IconButton(
                            onClick = {}) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                    }, navigationIcon = {
                        IconButton(
                            onClick = { appState.selectedUser.value = null; onBack() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
//					containerColor = Color.Transparent,

                    )
                )
            },
        ) {
            Column(
                Modifier.fillMaxSize().background(
                    MaterialTheme.colorScheme.surfaceBright, RoundedCornerShape(12.dp)
                ).padding(top = it.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChatMessages(
                    modifier = Modifier.weight(1f)
                )

                ChatBar()
            }
        }
    }
}