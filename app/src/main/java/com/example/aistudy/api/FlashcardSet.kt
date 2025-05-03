package com.example.aistudy.api

import java.util.UUID

data class FlashcardSet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String = "",
    val cards: List<Flashcard> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) 