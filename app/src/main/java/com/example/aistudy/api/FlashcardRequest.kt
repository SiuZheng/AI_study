package com.example.aistudy.api

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class FlashcardRequest(
    val step: String = "flashcard",
    val flashcard_type: String? = null
    // file will be handled separately in multipart request
) 