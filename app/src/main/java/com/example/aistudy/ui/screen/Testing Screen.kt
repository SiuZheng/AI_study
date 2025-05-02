package com.example.aistudy.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.aistudy.ui.viewmodel.ViewModel
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect

data class ChatMessage(
    val message: String,
    val isUser: Boolean
)
@Composable
fun TestingScreen(
    chatViewModel: ViewModel,
){
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val response by chatViewModel.chatResponse.observeAsState("")

    userInput = "hi"
    LaunchedEffect(userInput) {
        chatViewModel.sendMessage(userInput)
    }
    println("Respond:$response")
    Column {
        Text(text = "User input: $userInput")
        Text(text = "Response: $response")
    }
}

