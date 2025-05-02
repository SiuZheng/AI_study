package com.example.aistudy.api

import com.example.aistudy.config.Config
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST(Config.CHAT_ENDPOINT)
    suspend fun chat(@Body request: ChatRequest): ChatResponse

}