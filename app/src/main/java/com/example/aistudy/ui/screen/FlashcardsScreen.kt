package com.example.aistudy.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Delete
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
import com.example.aistudy.api.FlashcardSet
import com.example.aistudy.ui.navigation.Screen
import com.example.aistudy.ui.viewmodel.FlashcardViewModel
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

// Define navigation route constants
private const val FLASHCARD_DETAIL_ROUTE = "flashcard_detail"

@Composable
fun FlashcardsScreen(selectedIndex: Int = 1, navController: NavController, viewModel: FlashcardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val flashcardSets by viewModel.flashcardSets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load flashcard sets when the screen launches
    LaunchedEffect(Unit) {
        viewModel.loadUserFlashcardSets()
    }
    
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    
    // State for file upload dialog
    var showFileUploadDialog by remember { mutableStateOf(false) }
    var fileUploadTitle by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            showFileUploadDialog = true
        }
    }
    
    // File upload dialog
    if (showFileUploadDialog) {
        AlertDialog(
            onDismissRequest = { 
                showFileUploadDialog = false 
                selectedFileUri = null
                fileUploadTitle = ""
            },
            title = { Text("Enter topic name") },
            text = {
                Column {
                    Text(
                        "Give your flashcard set a name:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = fileUploadTitle,
                        onValueChange = { fileUploadTitle = it },
                        placeholder = { Text("Topic name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedFileUri?.let {
                            viewModel.generateFlashcardsFromFile(context, it, fileUploadTitle)
                            showFileUploadDialog = false
                            selectedFileUri = null
                            fileUploadTitle = ""
                        }
                    },
                    enabled = fileUploadTitle.isNotBlank()
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showFileUploadDialog = false 
                        selectedFileUri = null
                        fileUploadTitle = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Type selection
    var showTypeDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("") }
    var showTypeNameDialog by remember { mutableStateOf(false) }
    var typeTitle by remember { mutableStateOf("") }
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
                                selectedType = type
                                showTypeDialog = false
                                
                                // Pre-populate the title with a capitalized type name
                                val typeName = type.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                }
                                typeTitle = "$typeName Flashcards"
                                showTypeNameDialog = true
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
    
    // Type name customization dialog
    if (showTypeNameDialog) {
        AlertDialog(
            onDismissRequest = { 
                showTypeNameDialog = false 
                selectedType = ""
                typeTitle = ""
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
                        value = typeTitle,
                        onValueChange = { typeTitle = it },
                        placeholder = { Text("Topic name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedType.isNotBlank()) {
                            val customTitle = if (typeTitle.isBlank()) {
                                "${selectedType.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                }} Flashcards"
                            } else {
                                typeTitle
                            }
                            
                            viewModel.generateFlashcardsFromType(selectedType, customTitle)
                            showTypeNameDialog = false
                            selectedType = ""
                            typeTitle = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showTypeNameDialog = false 
                        selectedType = ""
                        typeTitle = ""
                    }
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
            
            // Flashcard Sets Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (flashcardSets.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No flashcard sets yet",
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
                        columns = GridCells.Fixed(1),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(flashcardSets.filter { 
                            searchText.isEmpty() || 
                            it.title.contains(searchText, ignoreCase = true) ||
                            it.type.contains(searchText, ignoreCase = true)
                        }) { flashcardSet ->
                            SwipeableFlashcardSetItem(
                                flashcardSet = flashcardSet,
                                onClick = {
                                    viewModel.selectSet(flashcardSet.id)
                                    navController.navigate("$FLASHCARD_DETAIL_ROUTE/${flashcardSet.id}")
                                },
                                onDelete = {
                                    viewModel.deleteFlashcardSet(flashcardSet.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableFlashcardSetItem(flashcardSet: FlashcardSet, onClick: () -> Unit, onDelete: () -> Unit) {
    val delete = SwipeAction(
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        },
        background = Color.Red,
        onSwipe = { onDelete() },
        isUndo = false
    )
    
    SwipeableActionsBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        startActions = listOf(delete),
        swipeThreshold = 120.dp,
        backgroundUntilSwipeThreshold = Color.Red.copy(alpha = 0.2f)
    ) {
        FlashcardSetItem(flashcardSet, onClick)
    }
}

@Composable
fun FlashcardSetItem(flashcardSet: FlashcardSet, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = flashcardSet.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${flashcardSet.cards.size} cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Tap to view",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 