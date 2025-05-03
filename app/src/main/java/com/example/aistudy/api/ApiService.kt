package com.example.aistudy.api

import com.example.aistudy.config.Config
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST(Config.CHAT_ENDPOINT)
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @Multipart
    @POST(Config.WORKFLOW_ENDPOINT)
    suspend fun generateFlashcardsFromFile(
        @Part("step") step: RequestBody,
        @Part file: MultipartBody.Part
    ): FlashcardResponse

    @Multipart
    @POST(Config.WORKFLOW_ENDPOINT)
    suspend fun generateFlashcardsFromType(
        @Part("step") step: RequestBody,
        @Part("flashcard_type") flashcardType: RequestBody
    ): FlashcardResponse
}