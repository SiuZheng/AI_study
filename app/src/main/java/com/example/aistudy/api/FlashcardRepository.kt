package com.example.aistudy.api

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
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
            
            // Determine MIME type based on file extension
            val mimeType = getMimeType(file.name) ?: "application/octet-stream"
            
            // Create request parts
            val stepPart = "flashcard".toRequestBody("text/plain".toMediaTypeOrNull())
            
            val fileRequestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
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
        
        // Try to get the file extension from the Uri
        val extension = getExtensionFromUri(context, uri)
        val tempFile = File.createTempFile("upload", ".$extension", context.cacheDir)
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        
        inputStream?.close()
        return tempFile
    }
    
    // Get file extension from Uri
    private fun getExtensionFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "tmp"
    }
    
    // Get MIME type from file name
    private fun getMimeType(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
} 