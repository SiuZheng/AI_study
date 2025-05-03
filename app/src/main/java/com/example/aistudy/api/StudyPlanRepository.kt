package com.example.aistudy.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class StudyPlanRepository {
    
    private val apiService = ApiClient.apiService
    
    suspend fun generateStudyPlan(
        plannerPrompt: String
    ): Result<StudyPlanResponse> = withContext(Dispatchers.IO) {
        try {
            // Create request parts
            val stepPart = "study planner".toRequestBody("text/plain".toMediaTypeOrNull())
            val promptPart = plannerPrompt.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Make API call
            val response = apiService.generateStudyPlan(
                step = stepPart,
                plannerPrompt = promptPart
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 