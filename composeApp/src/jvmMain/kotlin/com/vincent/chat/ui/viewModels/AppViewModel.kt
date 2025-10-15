package com.vincent.chat.ui.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vincent.chat.core.model.*
import com.vincent.chat.ui.state.AppState
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.ConnectException

class AppViewModel(
    private val appState: AppState
) : ViewModel() {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val root: String = "C:${File.separator}ComposeChat${File.separator}cache${File.separator}".also {
        File(it).apply { if (!exists()) mkdirs() }
    }
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var socket: Socket? = null
    private var receiveChannel: ByteReadChannel? = null
    private var sendChannel: ByteWriteChannel? = null
    private val _connected = appState.isConnected
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10
    private val baseReconnectDelayMs = 2000L

    val usersList = appState.listUsers
    val messages = appState.messages
    val networkConfig = appState.networkConfig

    init {
        viewModelScope.launch {
            appState.networkConfig
                .collect { newConfig ->
                    println("Network Config updated: $newConfig")
                    reconnectWithNewConfig()
                }
        }
        viewModelScope.launch {
            appState.listUsers.collect { newList ->
                if (appState.selectedUser.value != null && !newList.contains(appState.selectedUser.value)) {
                    appState.selectedUser.value = null
                    println("Selected user was removed from the list, resetting selection.")
                }
            }
        }
    }

    private suspend fun reconnectWithNewConfig() {
        val currentUser = appState.currentUser.value

        disconnect(closeSelector = false)
        connect()

        currentUser?.let { user ->
            delay(500)
            login(user.name)
            println("Auto-login after config change: ${user.name}")
        }
    }

    private fun connect() = viewModelScope.launch {
        try {
            reconnectJob?.cancel()

            disconnect(false)

            val config = networkConfig.value
            println("\nConnecting to ${config.host}:${config.port}")

            socket = aSocket(selectorManager).tcp().connect(config.host, config.port)
            receiveChannel = socket?.openReadChannel()
            sendChannel = socket?.openWriteChannel(autoFlush = true)

            if (receiveChannel != null) {
                println("Connected successfully to ${config.host}:${config.port}")
            }

            _connected.value = true
            reconnectAttempts = 0

            launch {
                try {
                    var line: String?
                    while (receiveChannel?.readUTF8Line().also { line = it } != null) {
                        val text = line!!.trim().takeIf { it.isNotEmpty() } ?: continue
                        handleIncoming(text)
                    }
                } catch (e: Exception) {
                    println("Connection lost: ${e.message} : ${e.cause}")
                    _connected.value = false


                    if (_connected.value) {
                        scheduleReconnect()
                    }
                }
            }
        } catch (e: ConnectException) {
            println("Cannot connect to server: ${e.message}")
            _connected.value = false
            scheduleReconnect()
        } catch (e: Exception) {
            println("Connection error: ${e.message}")
            _connected.value = false
            scheduleReconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            disconnect(true)
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        if (reconnectAttempts < maxReconnectAttempts) {
            val delayMs = baseReconnectDelayMs * (1 shl reconnectAttempts)
            reconnectAttempts++

            println("Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms")
            reconnectJob = viewModelScope.launch {
                delay(delayMs)
                connect()
                appState.currentUser.value?.let { user ->
                    delay(500)
                    login(user.name)
                }
            }
        } else {
            println("Max reconnect attempts reached")
        }
    }

    private suspend fun disconnect(closeSelector: Boolean = true) = viewModelScope.async {
        try {

            if (_connected.value && appState.currentUser.value != null) {
                logout()
                delay(100)
            }

            sendChannel?.flushAndClose()
            receiveChannel?.cancel()
            socket?.close()
            _connected.value = false

            println("Disconnected (closeSelector: $closeSelector)")
        } catch (e: Exception) {
            println("Error during disconnect: ${e.message}")
        } finally {
            if (closeSelector) {
                selectorManager.close()
            }
        }
    }.await()


    fun handleIncoming(jsonLine: String) {
        val el = json.parseToJsonElement(jsonLine)
        val type = el.jsonObject["type"]?.jsonPrimitive?.content
        when (type) {
            "users" -> {
                val list = el.jsonObject["list"]!!.jsonArray.map { it.jsonPrimitive.content }
                usersList.value = list.stream().filter { it != appState.currentUser.value?.name }.toList()
                appState.selectedUser.value =
                    usersList.value.contains(appState.selectedUser.value).let { appState.selectedUser.value }
                println("Updating users list: $list")
            }

            "deliver" -> {
                val from = el.jsonObject["from"]!!.jsonPrimitive.content
                val text = el.jsonObject["text"]!!.jsonPrimitive.content
                println("Message from $from: $text")

                val list = messages.getOrPut(from) { mutableStateListOf() }
                list.add(
                    Msg.Send(
                        from = from,
                        to = appState.currentUser.value?.name ?: "Message",
                        text = text,
                    ),
                )
            }

            "file" -> {
                val f = json.decodeFromString<FileDeliver>(jsonLine)

                println("Receiving file ${f.fileName} of size ${f.fileSize} bytes from ${f.from}")

                val userDir = File(root + appState.currentUser.value!!.name).apply { if (!exists()) mkdirs() }
                val file =
                    File(userDir, "${System.currentTimeMillis()}_${f.from}_${f.fileName}")
                file.writeBytes(f.bytes)
                println("File ${f.fileName} saved to ${file.absolutePath}")

                val list = messages.getOrPut(f.from) { mutableStateListOf() }
                list.add(
                    Msg.Send(
                        from = f.from,
                        to = appState.currentUser.value?.name ?: "File",
                        text = file.absolutePath,
                        msgType = MessageType.FILE
                    )
                )
            }
        }
    }

    fun sendMessage(from: String, to: String, content: String) = viewModelScope.launch {
        if (_connected.value) {
            val text = content.trim()
            if (text.isEmpty()) return@launch
            sendChannel?.writeStringUtf8(json.encodeToString(Msg.Send(from = from, to = to, text = text)) + "\n")
            messages.getOrPut(to) {
                mutableStateListOf()
            }.add(Msg.Send(from = from, to = to, text = text))
        } else {
            println("Cannot send message: not connected.")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sendFile(from: String, to: String, file: String) = viewModelScope.launch {
        if (_connected.value) {
            sendChannel?.writeStringUtf8(
                json.encodeToString(
                    FilePacket(
                        from = from,
                        to = to,
                        fileName = getFileName(file),
                        fileSize = File(file).length(),
                        bytes = File(file).readBytes()
                    )
                ) + "\n"
            )

            messages.getOrPut(to) { mutableStateListOf() }
                .add(Msg.Send(from = from, to = to, text = file, msgType = MessageType.FILE))
        } else
            println("Cannot send file: not connected.")
    }

    private fun getFileName(path: String): String {
        return path.substringAfterLast(File.separator)
    }

    fun login(username: String) = viewModelScope.launch {
        if (!_connected.value) return@launch
        try {
            sendChannel?.writeStringUtf8(json.encodeToString(Login(from = username)) + "\n")
            val oldUser = appState.currentUser.value
            appState.currentUser.value = oldUser?.copy(name = username) ?: User(name = username)
            println("Login sent for user: $username")
        } catch (e: Exception) {
            println("Error during login: ${e.message}")
        }
    }

    fun logout() = viewModelScope.launch {
        if (_connected.value) {
            val username = appState.currentUser.value?.name ?: return@launch
            try {
                sendChannel?.writeStringUtf8(json.encodeToString(Msg.Logout(from = username)) + "\n")
                println("Logout sent for user: $username")
            } catch (e: Exception) {
                println("Error during logout: ${e.message}")
            }
        }
        appState.currentUser.value = null
    }

    fun setNetworkConfig(host: String, port: Int) {
        val newConfig = NetworkConfig(host, port)
        println("Setting new network config: $newConfig")
        appState.networkConfig.value = newConfig
    }

    fun toggleShowConfigDialog() {
        appState.networkConfigShown.value = !appState.networkConfigShown.value
    }

    fun toggleShowConfigDialog(value: Boolean) {
        appState.networkConfigShown.value = true
    }
}