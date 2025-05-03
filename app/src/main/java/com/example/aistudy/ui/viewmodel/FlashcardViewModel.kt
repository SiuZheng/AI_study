package com.example.aistudy.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudy.api.Flashcard
import com.example.aistudy.api.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel : ViewModel() {
    private val repository = FlashcardRepository()
    
    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun generateFlashcardsFromFile(context: Context, fileUri: Uri) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.generateFlashcardsFromFile(context, fileUri)
                .onSuccess { response ->
                    _flashcards.value = response.cards
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to generate flashcards"
                    _isLoading.value = false
                }
        }
    }
    
    fun generateFlashcardsFromType(flashcardType: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.generateFlashcardsFromType(flashcardType)
                .onSuccess { response ->
                    _flashcards.value = response.cards
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to generate flashcards"
                    _isLoading.value = false
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 