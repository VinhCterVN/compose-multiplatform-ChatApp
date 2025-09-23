package com.vincent.chat.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Login(
    val type: String = "login",
    val from: String
)

@Serializable
data class Send(
    val type: String = "send",
    val from: String,
    val to: String,
    val text: String
)

@Serializable
sealed class Msg {
    @Serializable
    @SerialName("login")
    data class Login(val type: String = "login", val from: String) : Msg()

    @Serializable
    @SerialName("send")
    data class Send(
        val type: String = "send",
        val from: String,
        val to: String,
        val text: String,
        val msgType: MessageType = MessageType.TEXT
    ) : Msg()


    @Serializable
    @SerialName("logout")
    data class Logout(val type: String = "logout", val from: String) : Msg()
}

@Serializable
data class FilePacket(
    val type: String = "file",
    val from: String,
    val to: String,
    val fileName: String,
    val fileSize: Long,
    val bytes: ByteArray,
)

@Serializable
data class FileDeliver(
    val type: String = "file",
    val from: String,
    val fileName: String,
    val fileSize: Long,
    val bytes: ByteArray,
)

// Outgoing
@Serializable
data class Deliver(
    val type: String = "deliver",
    val from: String,
    val text: String,
    val msgType: MessageType = MessageType.TEXT
)

@Serializable
data class Users(val type: String = "users", val list: List<String>)

@Serializable
data class Err(val type: String = "error", val message: String)

@Serializable
enum class MessageType {
    TEXT, FILE
}