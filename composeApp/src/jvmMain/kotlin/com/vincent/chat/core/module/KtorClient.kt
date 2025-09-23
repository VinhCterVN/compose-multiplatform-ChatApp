package com.vincent.chat.core.module

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

object KtorClient {
	val client = HttpClient(CIO) {
		expectSuccess = true
	}
}


suspend fun main() {
	val client = KtorClient.client
	val response: HttpResponse = client.get("https://jsonplaceholder.typicode.com/posts/23")
	println(response.bodyAsText())
	client.close()
}

data class Post(
	val userId: Int,
	val id: Int,
	val title: String,
	val body: String
)