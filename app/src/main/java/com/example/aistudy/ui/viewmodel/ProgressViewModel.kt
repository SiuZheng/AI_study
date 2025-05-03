package com.example.aistudy.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

data class StudyTimeData(
    val completed: Float = 0f,
    val incomplete: Float = 0f
)

data class FlashcardData(
    val date: Date = Date(),
    val count: Int = 0
)

class ProgressViewModel : ViewModel() {
    private val TAG = "ProgressViewModel"
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _studyTimeData = MutableStateFlow(StudyTimeData())
    val studyTimeData: StateFlow<StudyTimeData> = _studyTimeData
    
    private val _flashcardData = MutableStateFlow<List<FlashcardData>>(emptyList())
    val flashcardData: StateFlow<List<FlashcardData>> = _flashcardData
    
    private val _totalFlashcardsCreated = MutableStateFlow(0)
    val totalFlashcardsCreated: StateFlow<Int> = _totalFlashcardsCreated
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // For displaying study time in hours and minutes
    private val _completedStudyTimeFormatted = MutableStateFlow("")
    val completedStudyTimeFormatted: StateFlow<String> = _completedStudyTimeFormatted
    
    private val _incompleteStudyTimeFormatted = MutableStateFlow("")
    val incompleteStudyTimeFormatted: StateFlow<String> = _incompleteStudyTimeFormatted
    
    init {
        Log.d(TAG, "ProgressViewModel initialized")
        loadUserData()
        loadDataForInterval(TimeInterval.WEEK)
    }
    
    enum class TimeInterval {
        WEEK, MONTH, YEAR
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot load user data: User not logged in")
            return
        }
        
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val flashcardsCreated = userDoc.getLong("flashcardsCreated")?.toInt() ?: 0
                
                _totalFlashcardsCreated.value = flashcardsCreated
                
                Log.d(TAG, "Loaded user data: flashcardsCreated=$flashcardsCreated")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data", e)
            }
        }
    }
    
    fun loadDataForInterval(interval: TimeInterval) {
        Log.d(TAG, "Loading data for interval: $interval")
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "Cannot load data: User not logged in")
            _error.value = "User not logged in"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Get start date based on interval
                val startDate = getStartDateForInterval(interval)
                Log.d(TAG, "Start date for interval $interval: $startDate")
                
                // Fetch study time data
                fetchStudyTimeData(userId, startDate)
                
                // Fetch flashcard data
                fetchFlashcardData(userId, startDate, interval)
                
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading progress data", e)
                _error.value = "Failed to load progress data: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun fetchStudyTimeData(userId: String, startDate: Date) {
        try {
            // First try to get study time data from statistics collection
            val statsDoc = firestore.collection("users")
                .document(userId)
                .collection("statistics")
                .document("study_stats")
                .get()
                .await()
            
            if (statsDoc.exists()) {
                // Try to get the completed and incomplete study time from statistics
                val totalStudyMinutes = statsDoc.getLong("totalStudyMinutes")?.toFloat() ?: 0f
                val totalPlansCompleted = statsDoc.getLong("totalPlansCompleted")?.toInt() ?: 0
                val totalPlansIncomplete = statsDoc.getLong("totalPlansIncomplete")?.toInt() ?: 0
                
                // Get the study plans to calculate incomplete study time
                val plansSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .get()
                    .await()
                
                var completedMinutes = 0f
                var incompleteMinutes = 0f
                
                // Calculate completed and incomplete minutes from the plans
                plansSnapshot.documents.forEach { doc ->
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val tasks = doc.get("tasks") as? List<Map<String, Any>> ?: return@forEach
                    val completedTasks = doc.get("completedTasks") as? List<String> ?: emptyList()
                    
                    tasks.forEach { task ->
                        val subject = task["subject"] as? String ?: return@forEach
                        val duration = (task["duration"] as? Long)?.toFloat() ?: 0f
                        
                        if (completedTasks.contains(subject)) {
                            // This task was completed
                            completedMinutes += duration
                        } else {
                            // This task was not completed
                            incompleteMinutes += duration
                        }
                    }
                }
                
                // Use the appropriate data source
                val completed = completedMinutes
                val incomplete = incompleteMinutes
                
                _studyTimeData.value = StudyTimeData(completed, incomplete)
                
                // Format for display
                formatStudyTimes(completed, incomplete)
                
                Log.d(TAG, "Fetched study time data from statistics: completed=$completed, incomplete=$incomplete")
            } else {
                // Fallback to user document if statistics aren't available
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val completedTime = userDoc.getLong("completedStudyTime")?.toFloat() ?: 0f
                val incompleteTime = userDoc.getLong("incompleteStudyTime")?.toFloat() ?: 0f
                
                _studyTimeData.value = StudyTimeData(completedTime, incompleteTime)
                
                // Format for display
                formatStudyTimes(completedTime, incompleteTime)
                
                Log.d(TAG, "Fetched study time data from user doc: completed=$completedTime, incomplete=$incompleteTime")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching study time data", e)
            throw e
        }
    }
    
    private fun formatStudyTimes(completedMinutes: Float, incompleteMinutes: Float) {
        // Format completed time
        val completedHours = (completedMinutes / 60).toInt()
        val completedMins = (completedMinutes % 60).toInt()
        _completedStudyTimeFormatted.value = if (completedHours > 0) {
            "$completedHours h $completedMins min"
        } else {
            "$completedMins min"
        }
        
        // Format incomplete time
        val incompleteHours = (incompleteMinutes / 60).toInt()
        val incompleteMins = (incompleteMinutes % 60).toInt()
        _incompleteStudyTimeFormatted.value = if (incompleteHours > 0) {
            "$incompleteHours h $incompleteMins min"
        } else {
            "$incompleteMins min"
        }
    }
    
    private suspend fun fetchFlashcardData(userId: String, startDate: Date, interval: TimeInterval) {
        try {
            Log.d(TAG, "Fetching flashcard data for userId=$userId, startDate=$startDate")
            
            // Query flashcard sets created after the start date
            val flashcardSets = firestore.collection("users")
                .document(userId)
                .collection("flashcardSets")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .get()
                .await()
            
            if (flashcardSets.isEmpty) {
                Log.d(TAG, "No flashcard sets found in the specified time interval")
            } else {
                Log.d(TAG, "Found ${flashcardSets.size()} flashcard sets")
            }
            
            // Group by date according to interval
            val groupedData = mutableMapOf<Date, Int>()
            
            // Count flashcard sets created per day/period
            for (doc in flashcardSets.documents) {
                val createdAt = doc.getTimestamp("createdAt")?.toDate()
                if (createdAt != null) {
                    val truncatedDate = truncateDate(createdAt, interval)
                    val currentCount = groupedData[truncatedDate] ?: 0
                    
                    // Count each set as 1 regardless of the number of cards it contains
                    // If you want to count individual cards, you'd need to query subcollections
                    groupedData[truncatedDate] = currentCount + 1
                    
                    // Also try to count individual cards in sets
                    try {
                        val setId = doc.id
                        val cards = firestore.collection("users")
                            .document(userId)
                            .collection("flashcardSets")
                            .document(setId)
                            .collection("flashcards")
                            .get()
                            .await()
                        
                        if (!cards.isEmpty) {
                            Log.d(TAG, "Found ${cards.size()} cards in set $setId")
                            val additionalCardCount = cards.size() - 1  // -1 to avoid double-counting the set itself
                            if (additionalCardCount > 0) {
                                groupedData[truncatedDate] = groupedData[truncatedDate]!! + additionalCardCount
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error counting cards in set ${doc.id}", e)
                        // Continue with the loop even if there's an error with one set
                    }
                } else {
                    Log.w(TAG, "Skipping document ${doc.id} as it has no createdAt timestamp")
                }
            }
            
            // Log the grouped data for debugging
            for ((date, count) in groupedData) {
                Log.d(TAG, "Grouped data: date=$date, count=$count")
            }
            
            // Fill in missing dates with zero counts
            val filledData = fillMissingDates(groupedData, startDate, interval)
            
            Log.d(TAG, "Final data has ${filledData.size} points")
            filledData.forEach { data ->
                Log.d(TAG, "  - ${data.date}: ${data.count} flashcards")
            }
            
            _flashcardData.value = filledData
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching flashcard data", e)
            throw e
        }
    }
    
    private fun getStartDateForInterval(interval: TimeInterval): Date {
        val calendar = Calendar.getInstance()
        
        when (interval) {
            TimeInterval.WEEK -> {
                // Go back 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            }
            TimeInterval.MONTH -> {
                // Go back 30 days
                calendar.add(Calendar.DAY_OF_YEAR, -30)
            }
            TimeInterval.YEAR -> {
                // Go back 365 days
                calendar.add(Calendar.DAY_OF_YEAR, -365)
            }
        }
        
        // Ensure we're at the start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.time
    }
    
    private fun truncateDate(date: Date, interval: TimeInterval): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        when (interval) {
            TimeInterval.WEEK -> {
                // Keep day precision for week view
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeInterval.MONTH -> {
                // Keep day precision for month view
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeInterval.YEAR -> {
                // Keep month precision for year view
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        return calendar.time
    }
    
    private fun fillMissingDates(
        data: Map<Date, Int>,
        startDate: Date,
        interval: TimeInterval
    ): List<FlashcardData> {
        val result = mutableListOf<FlashcardData>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        // Determine the increment and count based on interval
        val (increment, field, count) = when (interval) {
            TimeInterval.WEEK -> Triple(1, Calendar.DAY_OF_YEAR, 7)
            TimeInterval.MONTH -> Triple(1, Calendar.DAY_OF_YEAR, 30)
            TimeInterval.YEAR -> Triple(1, Calendar.MONTH, 12)
        }
        
        // Create data points for each date in the interval
        repeat(count) {
            val date = calendar.time
            val truncatedDate = truncateDate(date, interval)
            val value = data[truncatedDate] ?: 0
            
            result.add(FlashcardData(truncatedDate, value))
            
            calendar.add(field, increment)
        }
        
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
} 