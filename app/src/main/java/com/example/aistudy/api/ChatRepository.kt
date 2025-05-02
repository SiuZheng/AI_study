package com.example.aistudy.api


class ChatRepository {
    private val apiService = ApiClient.apiService

    suspend fun chat(request: ChatRequest): Result<ChatResponse> {
        return try {
            val response = apiService.chat(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}