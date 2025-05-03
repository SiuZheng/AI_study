package com.example.aistudy.api

import com.google.gson.annotations.SerializedName

data class FlashcardResponse(
    @SerializedName("flashcards")
    val cards: List<Flashcard>? = emptyList()
)

data class Flashcard(
    val question: String = "",
    @SerializedName("correctAnswer")
    val answer: String = ""
) 