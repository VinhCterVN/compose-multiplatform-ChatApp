package com.vincent.chat.core.server

import com.vincent.chat.core.model.Users
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.net.BindException
import java.util.concurrent.ConcurrentHashMap

class Server(
    private val port: Int = 9999
) {
    private val clients = ConcurrentHashMap<String, ClientHandler>()
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun run() = runBlocking {
        try {
            aSocket(selectorManager).tcp().bind("127.0.0.1", port).use { ss ->
                println("Server listening on $port")
                while (true) {
                    val clientSocket = ss.accept()
                    println("Client connected: ${clientSocket.localAddress}")
                    ClientHandler(clientSocket, this@Server, scope).start()
                    delay(1000)
                }
            }
        } catch (e: BindException) {
            println("Port $port is already in use. Please choose another port - ${e.message}\n")
            return@runBlocking
        } catch (e: Exception) {
            println("Server Exception: ${e.message}\n")
        }
    }

    fun register(username: String, handler: ClientHandler): Boolean =
        clients.putIfAbsent(username, handler) == null

    fun unregister(username: String) {
        clients.remove(username)
    }

    fun deliver(to: String, msg: Any) {
        clients[to]?.send(msg)
    }

    fun broadcastUsers() {
        val list = clients.keys().toList().sorted()
        val u = Users(list = list)
        clients.values.forEach { it.send(u) }
    }
}

fun main() {
    Server().run()
}