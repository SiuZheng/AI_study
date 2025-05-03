package com.example.aistudy.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aistudy.ui.viewmodel.FlashcardViewModel

@Composable
fun CreateFlashcardsScreen(selectedIndex: Int = 1, navController: NavController, viewModel: FlashcardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val context = LocalContext.current
    
    // Dialog states
    var showAITopicDialog by remember { mutableStateOf(false) }
    var aiTopic by remember { mutableStateOf("") }
    
    var showTypeDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("") }
    
    var showTitleDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogAction by remember { mutableStateOf<() -> Unit>({}) }
    
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            dialogTitle = "Document Flashcards"
            dialogAction = {
                viewModel.generateFlashcardsFromFile(context, selectedFileUri!!, dialogTitle)
                navController.navigate(Routes.FLASHCARDS)
            }
            showTitleDialog = true
        }
    }
    
    // Title customization dialog
    if (showTitleDialog) {
        AlertDialog(
            onDismissRequest = { 
                showTitleDialog = false 
                dialogTitle = ""
                selectedFileUri = null
            },
            title = { Text("Customize set name") },
            text = {
                Column {
                    Text(
                        "Give your flashcard set a name:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        placeholder = { Text("Topic name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTitleDialog = false
                        dialogAction()
                    },
                    enabled = dialogTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showTitleDialog = false 
                        dialogTitle = ""
                        selectedFileUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // AI Topic dialog
    if (showAITopicDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAITopicDialog = false 
                aiTopic = ""
            },
            title = { Text("Enter topic for AI to generate") },
            text = {
                Column {
                    Text(
                        "What topic would you like flashcards for?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = aiTopic,
                        onValueChange = { aiTopic = it },
                        placeholder = { Text("E.g., Mathematics, History, Science") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (aiTopic.isNotBlank()) {
                            showAITopicDialog = false
                            dialogTitle = "$aiTopic Flashcards"
                            dialogAction = {
                                viewModel.generateFlashcardsFromType(aiTopic.lowercase(), dialogTitle)
                                navController.navigate(Routes.FLASHCARDS)
                            }
                            showTitleDialog = true
                            aiTopic = ""
                        }
                    },
                    enabled = aiTopic.isNotBlank()
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAITopicDialog = false 
                        aiTopic = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Type selection dialog
    val flashcardTypes = listOf("math", "programming", "language", "history", "science")
    
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text("Select flashcard type") },
            text = {
                Column {
                    flashcardTypes.forEach { type ->
                        TextButton(
                            onClick = {
                                showTypeDialog = false
                                selectedType = type
                                val typeName = type.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                }
                                dialogTitle = "$typeName Flashcards"
                                dialogAction = {
                                    viewModel.generateFlashcardsFromType(selectedType, dialogTitle)
                                    navController.navigate(Routes.FLASHCARDS)
                                }
                                showTitleDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showTypeDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error dialog
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearError() }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedIndex,
                navController = navController,
                onChatClicked = { 
                    navController.navigate(Routes.TESTING)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Text(
                            text = "Create Flashcards",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Options subtitle
                Text(
                    text = "Options",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp)
                )
                
                // Vertical buttons layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option 1: Choose a topic
                    Button(
                        onClick = { showTypeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Choose a topic",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Option 2: AI generate
                    Button(
                        onClick = { showAITopicDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "AI generate",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Option 3: Upload Document
                    Button(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            "Upload Document",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Loading indicator overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
} 