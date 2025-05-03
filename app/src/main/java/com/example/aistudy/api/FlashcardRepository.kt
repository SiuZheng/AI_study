package com.example.aistudy.api

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class FlashcardRepository {
    
    private val apiService = ApiClient.apiService
    
    suspend fun generateFlashcardsFromFile(
        context: Context,
        fileUri: Uri
    ): Result<FlashcardResponse> = withContext(Dispatchers.IO) {
        try {
            // Convert Uri to File
            val file = uriToFile(context, fileUri)
            
            // Create request parts
            val stepPart = "flashcard".toRequestBody("text/plain".toMediaTypeOrNull())
            
            val fileRequestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = fileRequestBody
            )
            
            // Make API call
            val response = apiService.generateFlashcardsFromFile(
                step = stepPart,
                file = filePart
            )
            
            // Clean up temporary file
            file.delete()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateFlashcardsFromType(
        flashcardType: String
    ): Result<FlashcardResponse> = withContext(Dispatchers.IO) {
        try {
            // Create request parts
            val stepPart = "flashcard".toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = flashcardType.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Make API call
            val response = apiService.generateFlashcardsFromType(
                step = stepPart,
                flashcardType = typePart
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper method to convert content Uri to File
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".tmp", context.cacheDir)
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        
        inputStream?.close()
        return tempFile
    }
} 