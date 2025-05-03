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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ChatMessage(
    val message: String,
    val isUser: Boolean
)

@Composable
fun TestingScreen(
    chatViewModel: ViewModel,
    navController: NavController? = null
){
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val response by chatViewModel.chatResponse.observeAsState("")

    userInput = "hi"
    LaunchedEffect(userInput) {
        chatViewModel.sendMessage(userInput)
    }
    
    Scaffold(
        bottomBar = {
            navController?.let {
                BottomNavBar(
                    selectedIndex = -1, // No tab selected in testing screen
                    navController = navController
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Testing Chat",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "User input: $userInput")
            Text(text = "Response: $response")
        }
    }
}

