package com.vincent.chat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.vincent.chat.theme.AppTheme
import com.vincent.chat.theme.HeadLineLarge
import com.vincent.chat.ui.component.ChatArea
import com.vincent.chat.ui.component.ListUsers
import com.vincent.chat.ui.component.LoginDialog
import com.vincent.chat.ui.component.SettingDialog
import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.state.LocalBottomSheetScaffoldState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.jetbrains.skiko.Cursor
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun App(
) {
	val currentUser = koinInject<AppState>().currentUser.collectAsState()
	val scaffoldState = LocalBottomSheetScaffoldState.current
	val splitterState = rememberSplitPaneState()

	var isHovered by remember { mutableStateOf(false) }
	val bgColor by animateColorAsState(
		targetValue = if (isHovered) Color(0xFFAAAAAA) else Color(0x00000000),
		animationSpec = tween(
			durationMillis = 300,
			easing = EaseOut
		),
		label = "hoverColor"
	)

	AppTheme(darkTheme = false) {
		BottomSheetScaffold(
			scaffoldState = scaffoldState,
			sheetPeekHeight = 0.dp,
			sheetContent = @Composable {
				Column(
					Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text("Sheet Content", style = HeadLineLarge)
				}
			},
			modifier = Modifier.blur(if (currentUser.value == null) 12.dp else 0.dp)
		) {
			HorizontalSplitPane(
				splitPaneState = splitterState
			) {
				first(250.dp) {
					Box(
						Modifier
							.background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
							.fillMaxSize()
							.padding(horizontal = 2.dp)
					) {
						ListUsers()
					}
				}
				second(400.dp) {
					Box(
						Modifier.fillMaxSize(),
					) {
						ChatArea()
					}
				}
				splitter {
					visiblePart {
						Box(
							Modifier
								.width(1.dp)
								.fillMaxHeight()
								.background(
									MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
								)
						)
					}
					handle {
						Box(
							Modifier
								.fillMaxHeight()
								.width(4.dp)
								.padding(vertical = 12.dp)
								.background(bgColor, CircleShape)
								.onPointerEvent(PointerEventType.Enter) { isHovered = true }
								.onPointerEvent(PointerEventType.Exit) { isHovered = false }
								.pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
								.markAsHandle()
						)
					}
				}
			}
		}
		LoginDialog()
		SettingDialog()
	}
}