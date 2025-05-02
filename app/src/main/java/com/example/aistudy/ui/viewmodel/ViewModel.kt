package com.example.aistudy.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudy.api.ChatRepository
import com.example.aistudy.api.ChatRequest
import kotlinx.coroutines.launch

class ViewModel : ViewModel(){
    private val repository = ChatRepository()
    private val _chatResponse = MutableLiveData<String>()
    val chatResponse: LiveData<String> get() = _chatResponse
    private var conversationId: String? = null

    fun sendMessage(message: String){
        viewModelScope.launch{
            val request = ChatRequest(
                user_message = message,
                conversation_id = conversationId // This will be null if no existing conversationId found
            )
            val result = repository.chat(request)
            result.onSuccess { response ->
                conversationId = response.conversation_id // Update with new conversationId
                _chatResponse.value = response.answer
            }.onFailure { error ->
                _chatResponse.value = "Error: ${error.localizedMessage}"
            }
        }

    }
}