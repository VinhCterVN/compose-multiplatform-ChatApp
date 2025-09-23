package com.vincent.chat.core.module

import com.vincent.chat.ui.state.AppState
import com.vincent.chat.ui.viewModels.AppViewModel
import org.koin.dsl.module

val appModule = module {
	single { AppViewModel(get()) }
	single { AppState() }
}