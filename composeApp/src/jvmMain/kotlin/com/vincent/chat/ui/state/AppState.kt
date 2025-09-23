package com.vincent.chat.ui.state

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.vincent.chat.core.model.Msg
import com.vincent.chat.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow

class AppState {
	val currentUser = MutableStateFlow<User?>(null)
	val selectedUser = MutableStateFlow<String?>(null)
	val listUsers = MutableStateFlow<List<String>>(emptyList())
	val messages = mutableStateMapOf<String, SnapshotStateList<Msg.Send>>()
	val isConnected = MutableStateFlow(false)
}