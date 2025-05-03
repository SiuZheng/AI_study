package com.example.aistudy.api

data class FlashcardResponse(
    val cards: List<Flashcard>
)

data class Flashcard(
    val question: String,
    val answer: String
) 