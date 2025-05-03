package com.example.aistudy.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudy.api.DailyPlan
import com.example.aistudy.api.StudyPlanRepository
import com.example.aistudy.api.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class StudyPlanViewModel : ViewModel() {
    private val TAG = "StudyPlanViewModel"
    private val repository = StudyPlanRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _studyPlan = MutableStateFlow<List<DailyPlan>>(emptyList())
    val studyPlan: StateFlow<List<DailyPlan>> = _studyPlan
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Track completed tasks by plan date and task subject
    private val _completedTasks = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val completedTasks: StateFlow<Map<String, Set<String>>> = _completedTasks
    
    // Track completed plans by their date
    private val _completedPlans = MutableStateFlow<Set<String>>(emptySet())
    val completedPlans: StateFlow<Set<String>> = _completedPlans
    
    // Track incomplete plans that are past their due date
    private val _incompletePlans = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val incompletePlans: StateFlow<Map<String, List<String>>> = _incompletePlans
    
    // Study statistics
    private val _totalStudyHours = MutableStateFlow(0)
    val totalStudyHours: StateFlow<Int> = _totalStudyHours
    
    private val _subjectHours = MutableStateFlow<Map<String, Int>>(emptyMap())
    val subjectHours: StateFlow<Map<String, Int>> = _subjectHours
    
    init {
        // Load existing plans from Firestore when ViewModel is created
        loadPlansFromFirestore()
        // Load study statistics
        loadStudyStatistics()
    }
    
    private fun loadPlansFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading plans from Firestore")
                
                // Load plans
                val plansSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .get()
                    .await()
                
                val plans = plansSnapshot.documents.mapNotNull { doc ->
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val tasksData = doc.get("tasks") as? List<Map<String, Any>> ?: return@mapNotNull null
                    
                    val tasks = tasksData.mapNotNull { taskMap ->
                        val subject = taskMap["subject"] as? String ?: return@mapNotNull null
                        val duration = (taskMap["duration"] as? Long)?.toInt() ?: return@mapNotNull null
                        Task(subject, duration)
                    }
                    
                    DailyPlan(date, tasks)
                }
                
                Log.d(TAG, "Loaded ${plans.size} plans from Firestore")
                _studyPlan.value = plans
                
                // Load completed tasks
                val completedTasksMap = mutableMapOf<String, MutableSet<String>>()
                
                plansSnapshot.documents.forEach { doc ->
                    val date = doc.getString("date") ?: return@forEach
                    val completedTasksArray = doc.get("completedTasks") as? List<String> ?: return@forEach
                    
                    if (completedTasksArray.isNotEmpty()) {
                        completedTasksMap[date] = completedTasksArray.toMutableSet()
                        Log.d(TAG, "Loaded ${completedTasksArray.size} completed tasks for date $date")
                    }
                }
                
                _completedTasks.value = completedTasksMap
                
                // Load incomplete plans data
                val incompletePlansMap = mutableMapOf<String, List<String>>()
                
                plansSnapshot.documents.forEach { doc ->
                    val date = doc.getString("date") ?: return@forEach
                    val incompleteTasks = doc.get("incompleteTasks") as? List<String> ?: return@forEach
                    
                    if (incompleteTasks.isNotEmpty()) {
                        incompletePlansMap[date] = incompleteTasks
                        Log.d(TAG, "Loaded ${incompleteTasks.size} incomplete tasks for date $date")
                    }
                }
                
                _incompletePlans.value = incompletePlansMap
                
                // Update completed plans based on task completion
                updateCompletedPlansStatus()
                
                // Check for overdue plans
                checkForOverduePlans()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading plans from Firestore", e)
                _error.value = "Failed to load plans: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadStudyStatistics() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val statsDoc = firestore.collection("users")
                    .document(userId)
                    .collection("statistics")
                    .document("study_stats")
                    .get()
                    .await()
                
                if (statsDoc.exists()) {
                    val totalHours = statsDoc.getLong("totalStudyMinutes")?.toInt() ?: 0
                    _totalStudyHours.value = totalHours / 60 // Convert minutes to hours
                    
                    val subjectStats = statsDoc.get("subjectMinutes") as? Map<String, Long>
                    if (subjectStats != null) {
                        _subjectHours.value = subjectStats.mapValues { (it.value.toInt() / 60) }
                    }
                } else {
                    // Create stats document if it doesn't exist
                    firestore.collection("users")
                        .document(userId)
                        .collection("statistics")
                        .document("study_stats")
                        .set(
                            hashMapOf(
                                "totalStudyMinutes" to 0,
                                "subjectMinutes" to hashMapOf<String, Int>(),
                                "totalPlansCompleted" to 0,
                                "totalPlansIncomplete" to 0
                            )
                        )
                        .await()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load study statistics: ${e.message}"
            }
        }
    }
    
    fun generateStudyPlan(prompt: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.generateStudyPlan(prompt)
                .onSuccess { response ->
                    _studyPlan.value = response.study_plan
                    _isLoading.value = false
                    
                    // Reset the completed tasks when generating a new plan
                    _completedTasks.value = emptyMap()
                    
                    // Save the generated plan to Firestore
                    savePlansToFirestore(response.study_plan)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to generate study plan"
                    _isLoading.value = false
                }
        }
    }
    
    private fun savePlansToFirestore(plans: List<DailyPlan>) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving ${plans.size} plans to Firestore")
                
                // Delete old plans first
                val oldPlansSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .get()
                    .await()
                
                // Save old plans to history before deleting them
                val historyBatch = firestore.batch()
                val currentTimestamp = Date()
                
                oldPlansSnapshot.documents.forEach { doc ->
                    // Only save completed or past due plans to history
                    val date = doc.getString("date") ?: return@forEach
                    val planDate = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                    } catch (e: Exception) {
                        null
                    }
                    
                    val isCompleted = _completedPlans.value.contains(date)
                    val isPastDue = planDate?.before(Date()) == true
                    
                    if (isCompleted || isPastDue) {
                        // Copy this plan to history
                        val historyRef = firestore.collection("users")
                            .document(userId)
                            .collection("plan_history")
                            .document()
                            
                        // Get all the data from the original plan
                        val planData = doc.data ?: return@forEach
                        
                        // Add additional metadata
                        planData["archived_at"] = currentTimestamp
                        planData["was_completed"] = isCompleted
                        
                        historyBatch.set(historyRef, planData)
                        
                        // Update study statistics if plan was completed
                        if (isCompleted) {
                            val completedTasks = doc.get("completedTasks") as? List<String> ?: emptyList()
                            val tasksData = doc.get("tasks") as? List<Map<String, Any>> ?: emptyList()
                            
                            // Calculate total minutes studied
                            updateStudyStatistics(date, tasksData, completedTasks)
                        }
                    }
                }
                
                // Apply history batch
                historyBatch.commit().await()
                
                // Delete all existing documents in batch
                val batch = firestore.batch()
                oldPlansSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                
                // Add new plans - always with empty completedTasks
                plans.forEach { plan ->
                    val planData = hashMapOf(
                        "date" to plan.date,
                        "tasks" to plan.tasks.map { task ->
                            hashMapOf(
                                "subject" to task.subject,
                                "duration" to task.duration
                            )
                        },
                        "completedTasks" to listOf<String>(),  // Always empty for new plans
                        "incompleteTasks" to listOf<String>(),
                        "created_at" to Date()
                    )
                    
                    firestore.collection("users")
                        .document(userId)
                        .collection("study_plans")
                        .document(plan.date)
                        .set(planData)
                        .await()
                }
                
                // Reload statistics after update
                loadStudyStatistics()
                
                // Clear any cached data after saving new plans
                _completedTasks.value = emptyMap()
                _completedPlans.value = emptySet()
                _incompletePlans.value = emptyMap()
                
                // Reload the plans to refresh the UI
                loadPlansFromFirestore()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save plans to Firestore", e)
                _error.value = "Failed to save plans: ${e.message}"
            }
        }
    }
    
    fun removePlan(date: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Check if plan is completed or past due before removing
                val isCompleted = _completedPlans.value.contains(date)
                val isPastDue = try {
                    val planDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                    planDate?.before(Date()) == true
                } catch (e: Exception) {
                    false
                }
                
                // Get plan data for history
                val planDoc = firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .document(date)
                    .get()
                    .await()
                
                if (isCompleted || isPastDue) {
                    // Save to history before deleting
                    val planData = planDoc.data
                    if (planData != null) {
                        planData["archived_at"] = Date()
                        planData["was_completed"] = isCompleted
                        planData["was_deleted"] = true
                        
                        firestore.collection("users")
                            .document(userId)
                            .collection("plan_history")
                            .document()
                            .set(planData)
                            .await()
                        
                        // Update study statistics if completed
                        if (isCompleted) {
                            val completedTasks = planDoc.get("completedTasks") as? List<String> ?: emptyList()
                            val tasksData = planDoc.get("tasks") as? List<Map<String, Any>> ?: emptyList()
                            
                            updateStudyStatistics(date, tasksData, completedTasks)
                        }
                    }
                }
                
                // Remove from Firestore
                firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .document(date)
                    .delete()
                    .await()
                
                // Remove from local state
                val currentPlans = _studyPlan.value.toMutableList()
                val updatedPlans = currentPlans.filter { it.date != date }
                _studyPlan.value = updatedPlans
                
                // Also remove from completed tasks
                val updatedCompletedTasks = _completedTasks.value.toMutableMap()
                updatedCompletedTasks.remove(date)
                _completedTasks.value = updatedCompletedTasks
                
                // Remove from incomplete plans
                val updatedIncompletePlans = _incompletePlans.value.toMutableMap()
                updatedIncompletePlans.remove(date)
                _incompletePlans.value = updatedIncompletePlans
                
                // Update completed plans
                val updatedCompletedPlans = _completedPlans.value.toMutableSet()
                updatedCompletedPlans.remove(date)
                _completedPlans.value = updatedCompletedPlans
            } catch (e: Exception) {
                _error.value = "Failed to delete plan: ${e.message}"
            }
        }
    }
    
    private fun updateStudyStatistics(date: String, tasksData: List<Map<String, Any>>, completedTasks: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val statsRef = firestore.collection("users")
                    .document(userId)
                    .collection("statistics")
                    .document("study_stats")
                
                // Calculate total minutes studied
                var totalMinutes = 0
                val subjectMinutes = mutableMapOf<String, Int>()
                
                tasksData.forEach { taskMap ->
                    val subject = taskMap["subject"] as? String ?: return@forEach
                    val duration = (taskMap["duration"] as? Long)?.toInt() ?: return@forEach
                    
                    if (completedTasks.contains(subject)) {
                        totalMinutes += duration
                        subjectMinutes[subject] = (subjectMinutes[subject] ?: 0) + duration
                    }
                }
                
                // Update statistics in Firestore
                val batch = firestore.batch()
                
                // Update total study minutes
                batch.update(statsRef, "totalStudyMinutes", FieldValue.increment(totalMinutes.toLong()))
                batch.update(statsRef, "totalPlansCompleted", FieldValue.increment(1))
                
                // Update subject-specific minutes 
                subjectMinutes.forEach { (subject, minutes) ->
                    batch.update(statsRef, "subjectMinutes.$subject", FieldValue.increment(minutes.toLong()))
                }
                
                // Add to completed plans list
                batch.update(
                    statsRef, 
                    "completedPlans", 
                    FieldValue.arrayUnion(
                        hashMapOf(
                            "date" to date,
                            "totalMinutes" to totalMinutes,
                            "completedAt" to Date()
                        )
                    )
                )
                
                batch.commit().await()
                
            } catch (e: Exception) {
                _error.value = "Failed to update study statistics: ${e.message}"
            }
        }
    }
    
    fun toggleTaskCompletion(date: String, subject: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Update local state
                val updatedCompletedTasks = _completedTasks.value.toMutableMap()
                val tasksForDate = updatedCompletedTasks[date]?.toMutableSet() ?: mutableSetOf()
                
                if (tasksForDate.contains(subject)) {
                    // If already completed, remove it
                    tasksForDate.remove(subject)
                } else {
                    // Otherwise mark as complete
                    tasksForDate.add(subject)
                }
                
                // If no tasks are completed, remove the date entry
                if (tasksForDate.isEmpty()) {
                    updatedCompletedTasks.remove(date)
                } else {
                    updatedCompletedTasks[date] = tasksForDate
                }
                
                _completedTasks.value = updatedCompletedTasks
                
                // Update in Firestore
                firestore.collection("users")
                    .document(userId)
                    .collection("study_plans")
                    .document(date)
                    .update("completedTasks", tasksForDate.toList())
                    .await()
                
                // Update completed plans status
                updateCompletedPlansStatus()
                
                // Check for overdue plans after task completion update
                checkForOverduePlans()
                
            } catch (e: Exception) {
                _error.value = "Failed to update task completion: ${e.message}"
            }
        }
    }
    
    fun updateCompletedPlansStatus() {
        val userId = auth.currentUser?.uid ?: return
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val updatedCompletedPlans = mutableSetOf<String>()
        val previousCompletedPlans = _completedPlans.value
        
        viewModelScope.launch {
            try {
                studyPlan.value.forEach { plan ->
                    try {
                        val planDate = dateFormat.parse(plan.date)
                        val completedTasksForPlan = _completedTasks.value[plan.date] ?: emptySet()
                        
                        // Check if all tasks are completed
                        val allTasksCompleted = plan.tasks.isNotEmpty() && plan.tasks.all { task -> 
                            completedTasksForPlan.contains(task.subject) 
                        }
                        
                        // Today or future date: completed status depends on all tasks being completed
                        if (planDate != null && (planDate.after(today) || dateFormat.format(planDate) == dateFormat.format(today))) {
                            if (allTasksCompleted) {
                                updatedCompletedPlans.add(plan.date)
                            }
                        } else {
                            // Past date: completed only if all tasks were completed, otherwise marked as incomplete
                            if (allTasksCompleted) {
                                updatedCompletedPlans.add(plan.date)
                            }
                        }
                    } catch (e: Exception) {
                        // If date parsing fails, skip this plan
                        Log.e(TAG, "Error parsing date for plan: ${plan.date}", e)
                        return@forEach
                    }
                }
                
                _completedPlans.value = updatedCompletedPlans
                
                // Check for newly completed plans and update statistics
                updatedCompletedPlans.forEach { date ->
                    if (!previousCompletedPlans.contains(date)) {
                        // This plan was just completed, update statistics
                        val planDoc = firestore.collection("users")
                            .document(userId)
                            .collection("study_plans")
                            .document(date)
                            .get()
                            .await()
                        
                        val completedTasks = planDoc.get("completedTasks") as? List<String> ?: emptyList()
                        val tasksData = planDoc.get("tasks") as? List<Map<String, Any>> ?: emptyList()
                        
                        // Mark plan as completed in Firestore
                        firestore.collection("users")
                            .document(userId)
                            .collection("study_plans")
                            .document(date)
                            .update("isCompleted", true)
                            .await()
                        
                        // Don't update statistics here - we'll do that when plans are archived
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update plan completion status", e)
                _error.value = "Failed to update plan completion status: ${e.message}"
            }
        }
    }
    
    fun checkForOverduePlans() {
        val userId = auth.currentUser?.uid ?: return
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val updatedIncompletePlans = _incompletePlans.value.toMutableMap()
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking for overdue plans, today is: ${dateFormat.format(today.time)}")
                
                studyPlan.value.forEach { plan ->
                    try {
                        val planDate = dateFormat.parse(plan.date) ?: return@forEach
                        
                        // Only check plans in the past, not future plans
                        if (planDate.before(today.time)) {
                            Log.d(TAG, "Plan date ${plan.date} is before today")
                            
                            val completedTasksForPlan = _completedTasks.value[plan.date] ?: emptySet()
                            
                            // Find tasks that were not completed
                            val incompleteTasksList = plan.tasks
                                .filter { task -> !completedTasksForPlan.contains(task.subject) }
                                .map { it.subject }
                            
                            // If there are incomplete tasks, record them
                            if (incompleteTasksList.isNotEmpty()) {
                                Log.d(TAG, "Found ${incompleteTasksList.size} incomplete tasks for overdue plan ${plan.date}")
                                
                                updatedIncompletePlans[plan.date] = incompleteTasksList
                                
                                // Update in Firestore
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("study_plans")
                                    .document(plan.date)
                                    .update(
                                        mapOf(
                                            "incompleteTasks" to incompleteTasksList,
                                            "isOverdue" to true
                                        )
                                    )
                                    .await()
                                
                                // Update incomplete plans counter in statistics
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("statistics")
                                    .document("study_stats")
                                    .update("totalPlansIncomplete", FieldValue.increment(1))
                                    .await()
                            }
                        } else {
                            // Handle future plans - make sure they're not marked as incomplete
                            Log.d(TAG, "Plan date ${plan.date} is not before today, removing from incomplete plans")
                            
                            // Remove from incomplete plans if it exists
                            if (updatedIncompletePlans.containsKey(plan.date)) {
                                updatedIncompletePlans.remove(plan.date)
                                
                                // Update in Firestore
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("study_plans")
                                    .document(plan.date)
                                    .update(
                                        mapOf(
                                            "incompleteTasks" to listOf<String>(),
                                            "isOverdue" to false
                                        )
                                    )
                                    .await()
                            }
                        }
                    } catch (e: Exception) {
                        // If date parsing fails, skip this plan
                        Log.e(TAG, "Error processing plan date: ${plan.date}", e)
                        return@forEach
                    }
                }
                
                _incompletePlans.value = updatedIncompletePlans
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check overdue plans", e)
                _error.value = "Failed to check overdue plans: ${e.message}"
            }
        }
    }
    
    fun getStudyStatistics(): Map<String, Any> {
        return mapOf(
            "totalHours" to (_totalStudyHours.value),
            "subjectHours" to (_subjectHours.value)
        )
    }
    
    fun isTaskCompleted(date: String, subject: String): Boolean {
        return _completedTasks.value[date]?.contains(subject) == true
    }
    
    fun isPlanCompleted(date: String): Boolean {
        return _completedPlans.value.contains(date)
    }
    
    fun getCompletedTasksForPlan(date: String): Set<String> {
        return _completedTasks.value[date] ?: emptySet()
    }
    
    fun getIncompleteTasks(date: String): List<String> {
        return _incompletePlans.value[date] ?: emptyList()
    }
    
    fun clearError() {
        _error.value = null
    }
} 