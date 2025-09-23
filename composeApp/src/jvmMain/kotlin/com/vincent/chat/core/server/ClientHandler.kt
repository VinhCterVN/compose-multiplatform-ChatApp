package com.vincent.chat.core.server

import com.vincent.chat.core.model.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ClientHandler(
    private val socket: Socket,
    private val server: Server,
    private val scope: CoroutineScope
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val receiveChannel = socket.openReadChannel()
    private val sendChannel = socket.openWriteChannel(autoFlush = true)

    var username: String? = null

    fun send(obj: Any) = scope.launch(Dispatchers.IO) {
        try {
            val text = when (obj) {
                is Err -> json.encodeToString(obj)
                is Users -> json.encodeToString(obj)
                is Deliver -> json.encodeToString(obj)
                is FileDeliver -> json.encodeToString(obj)
                else -> throw IllegalArgumentException("Cannot serialize object of type ${obj::class.simpleName}")
            }
            sendChannel.writeStringUtf8(text + "\n")
        } catch (e: NullPointerException) {
            println("Send error: ${e.message}")
        }
    }

    fun start() = scope.launch(Dispatchers.IO) {
        try {
            var line: String?
            while (receiveChannel.readUTF8Line().also { line = it } != null) {
                val text = line!!.trim()
                if (text.isEmpty()) continue

                val el = Json.parseToJsonElement(text)
                val type = el.jsonObject["type"]?.jsonPrimitive?.content

                when (type) {
                    "login" -> {
                        val m = json.decodeFromString<Msg.Login>(text)
                        if (!server.register(m.from, this@ClientHandler)) {
                            send(Err(message = "Username already taken"))
                            socket.close(); return@launch
                        }
                        username = m.from
                        server.broadcastUsers()
                    }

                    "send" -> {
                        val m = json.decodeFromString<Msg.Send>(text)
                        if (m.from != username) send(Err(message = "Spoofed sender not allowed"))
                        else server.deliver(m.to, Deliver(from = m.from, text = m.text, msgType = m.msgType))
                    }

                    "file" -> {
                        val fileObject = json.decodeFromString<FilePacket>(text)
                        println("Server received a file packet: ${fileObject.fileName}")
                        if (fileObject.from != username) {
                            send(Err(message = "Spoofed sender not allowed"))
                            return@launch
                        }

                        server.deliver(
                            fileObject.to, FileDeliver(
                                from = fileObject.from,
                                fileName = fileObject.fileName,
                                fileSize = fileObject.fileSize,
                                bytes = fileObject.bytes
                            )
                        )
                    }

                    "logout" -> {
                        val m = json.decodeFromString<Msg.Logout>(text)
                        if (m.from != username) send(Err(message = "Spoofed sender not allowed"))
                        else {
                            username?.let { server.unregister(it) }
                            server.broadcastUsers()
                        }
                    }

                    else -> send(Err(message = "Unknown type: $type"))
                }
            }
        } catch (e: Exception) {
            println("ClientHandler error: ${e.javaClass.simpleName}")
            receiveChannel.cancel()
            sendChannel.flushAndClose()
            socket.close()
        }
    }
}