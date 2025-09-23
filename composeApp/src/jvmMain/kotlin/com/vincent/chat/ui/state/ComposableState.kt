package com.vincent.chat.ui.state

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.compositionLocalOf

@ExperimentalMaterial3Api
val LocalBottomSheetScaffoldState = compositionLocalOf<BottomSheetScaffoldState> {
	error("No BottomSheet state provided")
}