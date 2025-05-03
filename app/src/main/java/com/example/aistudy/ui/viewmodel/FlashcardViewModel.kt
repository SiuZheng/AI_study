package com.example.aistudy.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudy.api.Flashcard
import com.example.aistudy.api.FlashcardRepository
import com.example.aistudy.api.FlashcardSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class FlashcardViewModel : ViewModel() {
    private val repository = FlashcardRepository()
    private val TAG = "FlashcardViewModel"
    
    private val _flashcardSets = MutableStateFlow<List<FlashcardSet>>(emptyList())
    val flashcardSets: StateFlow<List<FlashcardSet>> = _flashcardSets
    
    private val _selectedSet = MutableStateFlow<FlashcardSet?>(null)
    val selectedSet: StateFlow<FlashcardSet?> = _selectedSet
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    init {
        // Initialize with test flashcards for debugging
        Log.d(TAG, "FlashcardViewModel initialized")
    }
    
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    private suspend fun saveFlashcardSet(flashcardSet: FlashcardSet) {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Cannot save flashcard set: user not logged in")
            return
        }
        
        try {
            // Update user's flashcardsCreated count
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("flashcardsCreated", FieldValue.increment(1))
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update flashcardsCreated counter, creating field", e)
                // If the field doesn't exist, set it to 1
                firestore.collection("users")
                    .document(userId)
                    .update(mapOf("flashcardsCreated" to 1))
                    .await()
            }
            
            // Save the set document
            val setData = hashMapOf(
                "id" to flashcardSet.id,
                "title" to flashcardSet.title,
                "type" to flashcardSet.type,
                "createdAt" to Date(flashcardSet.createdAt),
                "cardCount" to flashcardSet.cards.size
            )
            
            // Create the set document
            val setDocRef = firestore.collection("users")
                .document(userId)
                .collection("flashcardSets")
                .document(flashcardSet.id)
            
            setDocRef.set(setData).await()
            
            // Save individual flashcards
            flashcardSet.cards.forEachIndexed { index, flashcard ->
                val cardData = hashMapOf(
                    "question" to flashcard.question,
                    "answer" to flashcard.answer,
                    "topic" to flashcardSet.title,
                    "index" to index,
                    "createdAt" to Date()
                )
                
                setDocRef.collection("cards")
                    .add(cardData)
                    .await()
            }
            
            Log.d(TAG, "Successfully saved flashcard set with ID: ${flashcardSet.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving flashcard set to database", e)
        }
    }
    
    fun selectSet(setId: String) {
        val set = _flashcardSets.value.find { it.id == setId }
        _selectedSet.value = set
    }
    
    fun clearSelectedSet() {
        _selectedSet.value = null
    }
    
    fun generateFlashcardsFromFile(context: Context, fileUri: Uri, title: String) {
        _isLoading.value = true
        _error.value = null
        Log.d(TAG, "Starting to generate flashcards from file: $fileUri")
        
        viewModelScope.launch {
            repository.generateFlashcardsFromFile(context, fileUri)
                .onSuccess { response ->
                    if (response == null) {
                        Log.e(TAG, "API response is null")
                        _error.value = "Received null response from server"
                        _isLoading.value = false
                        return@onSuccess
                    }
                    
                    val cards = response.cards ?: emptyList()
                    Log.d(TAG, "API response received with ${cards.size} cards")
                    
                    if (cards.isNotEmpty()) {
                        val newSet = FlashcardSet(
                            title = title.ifEmpty { "Document Flashcards" },
                            type = "document",
                            cards = cards
                        )
                        
                        val currentSets = _flashcardSets.value.toMutableList()
                        currentSets.add(newSet)
                        _flashcardSets.value = currentSets
                        
                        // Save to database
                        saveFlashcardSet(newSet)
                    }
                    
                    _isLoading.value = false
                    Log.d(TAG, "Successfully generated ${cards.size} flashcards from file")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to generate flashcards"
                    _isLoading.value = false
                    Log.e(TAG, "Error generating flashcards from file: ${exception.message}", exception)
                }
        }
    }
    
    fun generateFlashcardsFromType(flashcardType: String, title: String = "") {
        _isLoading.value = true
        _error.value = null
        Log.d(TAG, "Starting to generate flashcards of type: $flashcardType")
        
        viewModelScope.launch {
            try {
                val result = repository.generateFlashcardsFromType(flashcardType)
                result.onSuccess { response ->
                    if (response == null) {
                        Log.e(TAG, "API response is null")
                        _error.value = "Received null response from server"
                        _isLoading.value = false
                        return@onSuccess
                    }
                    
                    val cards = response.cards ?: emptyList()
                    Log.d(TAG, "API response received with ${cards.size} cards")
                    
                    if (cards.isNotEmpty()) {
                        val typeTitle = if (title.isNotBlank()) {
                            title
                        } else {
                            "${flashcardType.replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase() else it.toString() 
                            }} Flashcards"
                        }
                        
                        val newSet = FlashcardSet(
                            title = typeTitle,
                            type = flashcardType,
                            cards = cards
                        )
                        
                        val currentSets = _flashcardSets.value.toMutableList()
                        currentSets.add(newSet)
                        _flashcardSets.value = currentSets
                        
                        // Save to database
                        saveFlashcardSet(newSet)
                    }
                    
                    _isLoading.value = false
                    Log.d(TAG, "Successfully generated ${cards.size} flashcards of type $flashcardType")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to generate flashcards"
                    _isLoading.value = false
                    Log.e(TAG, "Error generating flashcards of type $flashcardType: ${exception.message}", exception)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
                Log.e(TAG, "Unexpected error in generateFlashcardsFromType: ${e.message}", e)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    // For testing/debugging purposes
    fun createTestFlashcards() {
        Log.d(TAG, "Creating test flashcards")
        
        val mathSet = FlashcardSet(
            title = "Math Flashcards",
            type = "math",
            cards = listOf(
                Flashcard("What is 2 + 2?", "4"),
                Flashcard("What is the square root of 16?", "4"),
                Flashcard("What is 5 × 7?", "35")
            )
        )
        
        val scienceSet = FlashcardSet(
            title = "Science Flashcards",
            type = "science",
            cards = listOf(
                Flashcard("What is the chemical symbol for water?", "H2O"),
                Flashcard("What is the largest planet in our solar system?", "Jupiter"),
                Flashcard("What is the atomic number of carbon?", "6")
            )
        )
        
        val historySet = FlashcardSet(
            title = "History Flashcards",
            type = "history",
            cards = listOf(
                Flashcard("Who wrote Romeo and Juliet?", "William Shakespeare"),
                Flashcard("In what year did World War II end?", "1945"),
                Flashcard("What was the name of the first man on the moon?", "Neil Armstrong")
            )
        )
        
        _flashcardSets.value = listOf(mathSet, scienceSet, historySet)
        
        // Save test sets to database
        viewModelScope.launch {
            saveFlashcardSet(mathSet)
            saveFlashcardSet(scienceSet)
            saveFlashcardSet(historySet)
        }
    }
    
    // Load flashcard sets from database for the current user
    fun loadUserFlashcardSets() {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Cannot load flashcard sets: user not logged in")
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val setDocuments = firestore.collection("users")
                    .document(userId)
                    .collection("flashcardSets")
                    .get()
                    .await()
                
                val sets = mutableListOf<FlashcardSet>()
                
                for (setDoc in setDocuments) {
                    val setData = setDoc.data
                    val setId = setData["id"] as? String ?: continue
                    
                    // Get cards for this set
                    val cardDocuments = setDoc.reference.collection("cards")
                        .get()
                        .await()
                    
                    val cards = cardDocuments.mapNotNull { cardDoc ->
                        val cardData = cardDoc.data
                        val question = cardData["question"] as? String
                        val answer = cardData["answer"] as? String
                        
                        if (question != null && answer != null) {
                            Flashcard(question, answer)
                        } else {
                            null
                        }
                    }
                    
                    val set = FlashcardSet(
                        id = setId,
                        title = setData["title"] as? String ?: "Untitled",
                        type = setData["type"] as? String ?: "",
                        cards = cards,
                        createdAt = (setData["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                    )
                    
                    sets.add(set)
                }
                
                _flashcardSets.value = sets
                _isLoading.value = false
                
                Log.d(TAG, "Loaded ${sets.size} flashcard sets from database")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading flashcard sets from database", e)
                _error.value = "Failed to load your flashcard sets"
                _isLoading.value = false
            }
        }
    }
    
    // Delete a flashcard set
    fun deleteFlashcardSet(setId: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Cannot delete flashcard set: user not logged in")
            _error.value = "You must be logged in to delete flashcards"
            return
        }
        
        // First remove from UI state
        val currentSets = _flashcardSets.value.toMutableList()
        val setToRemove = currentSets.find { it.id == setId }
        
        if (setToRemove != null) {
            currentSets.remove(setToRemove)
            _flashcardSets.value = currentSets
            
            // Then delete from Firestore
            viewModelScope.launch {
                try {
                    // Get reference to set document
                    val setDocRef = firestore.collection("users")
                        .document(userId)
                        .collection("flashcardSets")
                        .document(setId)
                    
                    // Delete all cards in the set
                    val cardDocs = setDocRef.collection("cards").get().await()
                    for (cardDoc in cardDocs) {
                        cardDoc.reference.delete().await()
                    }
                    
                    // Delete the set document
                    setDocRef.delete().await()
                    
                    Log.d(TAG, "Successfully deleted flashcard set with ID: $setId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting flashcard set from database", e)
                    _error.value = "Failed to delete flashcard set"
                    
                    // If database deletion fails, add the set back to UI
                    currentSets.add(setToRemove)
                    _flashcardSets.value = currentSets
                }
            }
        } else {
            Log.w(TAG, "Flashcard set with ID $setId not found")
        }
    }
} 