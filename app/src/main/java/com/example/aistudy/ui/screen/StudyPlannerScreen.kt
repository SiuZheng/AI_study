package com.example.aistudy.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aistudy.api.DailyPlan
import com.example.aistudy.api.Task
import com.example.aistudy.ui.viewmodel.StudyPlanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun StudyPlannerScreen(selectedIndex: Int = 0, navController: NavController) {
    val viewModel: StudyPlanViewModel = viewModel()
    val studyPlan by viewModel.studyPlan.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val completedPlans by viewModel.completedPlans.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    
    // Create plan dialog state
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var plannerPrompt by remember { mutableStateOf("") }
    
    // Track items being removed with animation
    val itemsRemoved = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()
    
    // Error dialog
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Create plan dialog
    if (showCreatePlanDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlanDialog = false },
            title = { Text("Create Study Plan") },
            text = { 
                Column {
                    Text(
                        "Describe your study goals and requirements:",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = plannerPrompt,
                        onValueChange = { plannerPrompt = it },
                        placeholder = { Text("E.g. Create a study plan for final exams in Biology and Computer Science next week") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (plannerPrompt.isNotBlank()) {
                            viewModel.generateStudyPlan(plannerPrompt)
                            showCreatePlanDialog = false
                        }
                    },
                    enabled = plannerPrompt.isNotBlank() && !isLoading
                ) {
                    Text("Generate Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedIndex,
                navController = navController
            )
        },
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
                        .padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "Study Planner",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Study Plans
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (studyPlan.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "No study plans yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Create a study plan to start organizing your study sessions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreatePlanDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Create Plan",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Study Plan")
                            }
                        }
                    }
                } else {
                    // Show study plans
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Sort plans by date
                        val sortedPlans = studyPlan.sortedBy { 
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)?.time ?: 0
                            } catch (e: Exception) {
                                0
                            }
                        }
                        
                        itemsIndexed(
                            items = sortedPlans,
                            key = { _, plan -> plan.date }
                        ) { index, dailyPlan ->
                            val isBeingRemoved = itemsRemoved.contains(dailyPlan.date)
                            val isCompleted = completedPlans.contains(dailyPlan.date)
                            val planCompletedTasks = completedTasks[dailyPlan.date] ?: emptySet()
                            
                            // Check if date is in the past
                            val isPastDate = try {
                                val planDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dailyPlan.date)
                                val today = Calendar.getInstance().time
                                planDate?.before(today) ?: false
                            } catch (e: Exception) {
                                false
                            }
                            
                            // Animate removal if needed
                            AnimatedVisibility(
                                visible = !isBeingRemoved,
                                exit = shrinkVertically(
                                    animationSpec = tween(durationMillis = 300)
                                ) + fadeOut()
                            ) {
                                SwipeablePlanCard(
                                    dailyPlan = dailyPlan,
                                    isCompleted = isCompleted,
                                    isPastDate = isPastDate,
                                    completedTasks = planCompletedTasks,
                                    onToggleTaskCompletion = { subject ->
                                        viewModel.toggleTaskCompletion(dailyPlan.date, subject)
                                    },
                                    onDelete = {
                                        // Add to removed items first for animation
                                        itemsRemoved.add(dailyPlan.date)
                                        
                                        // Trigger the animation and then remove from ViewModel
                                        coroutineScope.launch {
                                            delay(300) // Wait for animation to complete
                                            viewModel.removePlan(dailyPlan.date)
                                            itemsRemoved.remove(dailyPlan.date)
                                        }
                                    }
                                )
                            }
                        }
                        
                        item {
                            // Add new plan card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clickable { showCreatePlanDialog = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Add, 
                                            contentDescription = "Create Plan", 
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Create New Plan",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeablePlanCard(
    dailyPlan: DailyPlan,
    isCompleted: Boolean,
    isPastDate: Boolean,
    completedTasks: Set<String>,
    onToggleTaskCompletion: (String) -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var cardWidth by remember { mutableStateOf(0f) }
    
    // Used to animate the card back to original position after partial swipe
    val offsetX = remember { Animatable(0f) }
    
    // Threshold for delete action (30% of card width)
    val deleteThreshold = 0.3f
    
    var expanded by remember { mutableStateOf(false) }

    // Status badge color
    val statusColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isPastDate -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    
    // Status badge text
    val statusText = when {
        isCompleted -> "Completed"
        isPastDate -> "Incomplete"
        else -> ""
    }
    
    // Card background color based on status
    val cardBackgroundColor = when {
        isCompleted -> Color(0xFFE6F4EA) // Light green for completed
        isPastDate -> Color(0xFFFCE8E6)  // Light red for incomplete/past due
        else -> MaterialTheme.colorScheme.surfaceVariant // Default color
    }

    // Main card content
    Box {
        // Delete background (shown when card is dragged)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
        
        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .onGloballyPositioned { layoutCoordinates ->
                    cardWidth = layoutCoordinates.size.width.toFloat()
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Only allow dragging to the left (negative delta)
                        if (delta <= 0) {
                            scope.launch {
                                offsetX.snapTo(offsetX.value + delta)
                            }
                        } else if (offsetX.value < 0) {
                            // Allow returning to original position
                            scope.launch {
                                offsetX.snapTo(minOf(0f, offsetX.value + delta))
                            }
                        }
                    },
                    onDragStopped = { velocity ->
                        scope.launch {
                            // If dragged past threshold, trigger delete
                            if (offsetX.value < -cardWidth * deleteThreshold) {
                                onDelete()
                            } else {
                                // Otherwise, animate back to original position
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    initialVelocity = velocity
                                )
                            }
                        }
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { expanded = !expanded }
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatDate(dailyPlan.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Show status badge if necessary
                        if (statusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = statusColor
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isPastDate && !isCompleted) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // Expand/collapse button
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Completion progress
                    val completedTaskCount = completedTasks.size
                    val totalTaskCount = dailyPlan.tasks.size
                    val progressPercentage = if (totalTaskCount > 0) {
                        completedTaskCount.toFloat() / totalTaskCount.toFloat()
                    } else 0f
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: $completedTaskCount/$totalTaskCount tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = "${(progressPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LinearProgressIndicator(
                            progress = { progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Display message for past due plans
                    if (isPastDate && !isCompleted) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Past Due",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "This plan is past due. You can view but not mark tasks as complete.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    dailyPlan.tasks.forEach { task ->
                        val isTaskCompleted = completedTasks.contains(task.subject)
                        TaskItem(
                            task = task,
                            isCompleted = isTaskCompleted,
                            onToggleCompleted = {
                                onToggleTaskCompletion(task.subject)
                            },
                            isPastDue = isPastDate && !isCompleted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    isCompleted: Boolean = false,
    onToggleCompleted: () -> Unit,
    isPastDue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) 
                    MaterialTheme.colorScheme.outline 
                else 
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${task.duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Complete button
            IconButton(
                onClick = onToggleCompleted,
                modifier = Modifier.size(32.dp),
                enabled = !isPastDue || isCompleted
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = if (isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isPastDue -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    } catch (e: Exception) {
        return dateString
    }
} 