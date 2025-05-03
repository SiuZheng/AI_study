package com.example.aistudy.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aistudy.api.Flashcard
import com.example.aistudy.ui.viewmodel.FlashcardViewModel

@Composable
fun FlashcardsScreen(selectedIndex: Int = 1, navController: NavController) {
    val viewModel: FlashcardViewModel = viewModel()
    val flashcards by viewModel.flashcards.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    
    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.generateFlashcardsFromFile(context, it)
        }
    }
    
    // Type selection
    var showTypeDialog by remember { mutableStateOf(false) }
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
                                viewModel.generateFlashcardsFromType(type)
                                showTypeDialog = false
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Flashcards",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    Button(
                        onClick = { navController.navigate(Routes.CREATE_FLASHCARDS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Create", fontSize = 14.sp)
                    }
                }
            }
            
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = "Search", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = { showTypeDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Add, 
                        contentDescription = "Add flashcards", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = { launcher.launch("*/*") },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Upload, 
                        contentDescription = "Upload file", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Flashcard Grid with loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (flashcards.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No flashcards yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create flashcards by selecting a type or uploading a file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(flashcards.filter { 
                            searchText.isEmpty() || 
                            it.question.contains(searchText, ignoreCase = true) ||
                            it.answer.contains(searchText, ignoreCase = true)
                        }) { flashcard ->
                            FlashcardItem(flashcard)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(flashcard: Flashcard) {
    var showAnswer by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { showAnswer = !showAnswer }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showAnswer) flashcard.answer else flashcard.question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = if (showAnswer) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
} 