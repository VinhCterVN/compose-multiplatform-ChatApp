package com.vincent.chat

import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vincent.chat.core.module.appModule
import com.vincent.chat.core.server.Server
import com.vincent.chat.ui.App
import com.vincent.chat.ui.state.LocalBottomSheetScaffoldState
import com.vincent.chat.ui.viewModels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3Api::class)
fun main() = application {
    CoroutineScope(Dispatchers.IO).launch {
        Server().run()
    }

    val state = rememberWindowState()

    KoinApplication(application = {
        modules(appModule.plus(module { single { state } }))
    }) {
        val model = koinInject<AppViewModel>()
        Window(
            onCloseRequest = {
                model.logout()
                exitApplication()
            },
            title = "ComposeChat",
            state = state
        ) {
            val scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false
                ),
                snackbarHostState = remember { SnackbarHostState() }
            )

            LaunchedEffect(state) {
                snapshotFlow { state.size }
                    .onEach(::onWindowResize)
                    .launchIn(this)
            }

            CompositionLocalProvider(LocalBottomSheetScaffoldState provides scaffoldState) { App() }
        }
    }
}

private fun onWindowResize(size: DpSize) {
    println("onWindowResize $size")
}