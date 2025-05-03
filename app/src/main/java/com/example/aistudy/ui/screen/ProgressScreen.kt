package com.example.aistudy.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aistudy.ui.components.CircularProgressChart
import com.example.aistudy.ui.viewmodel.ProgressViewModel

private const val TAG = "ProgressScreen"

@Composable
fun ProgressScreen(selectedIndex: Int = 2, navController: NavController) {
    val viewModel: ProgressViewModel = viewModel()
    
    val studyTimeData by viewModel.studyTimeData.collectAsState()
    val completedTimeFormatted by viewModel.completedStudyTimeFormatted.collectAsState()
    val incompleteTimeFormatted by viewModel.incompleteStudyTimeFormatted.collectAsState()
    val totalFlashcardsCreated by viewModel.totalFlashcardsCreated.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var selectedIntervalIndex by remember { mutableStateOf(0) }
    val intervals = listOf("Week", "Month", "Year")
    
    // Effect to load data when interval changes
    LaunchedEffect(selectedIntervalIndex) {
        Log.d(TAG, "Interval changed to: ${intervals[selectedIntervalIndex]}")
        val interval = when (selectedIntervalIndex) {
            0 -> ProgressViewModel.TimeInterval.WEEK
            1 -> ProgressViewModel.TimeInterval.MONTH
            2 -> ProgressViewModel.TimeInterval.YEAR
            else -> ProgressViewModel.TimeInterval.WEEK
        }
        viewModel.loadDataForInterval(interval)
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
                navController = navController
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
                    .padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "Progress Tracker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Time Interval Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                intervals.forEachIndexed { idx, label ->
                    Button(
                        onClick = { selectedIntervalIndex = idx },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedIntervalIndex == idx)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedIntervalIndex == idx)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label, 
                            fontSize = 14.sp,
                            fontWeight = if (selectedIntervalIndex == idx) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            // If loading, show loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Data Visualization
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Study Time Card with Circular Progress
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Study Time",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            CircularProgressChart(
                                studyTimeData = studyTimeData,
                                completedTimeFormatted = completedTimeFormatted,
                                incompleteTimeFormatted = incompleteTimeFormatted,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    // Flashcards Created Card with Total Count
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Flashcards Created",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = "$totalFlashcardsCreated",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Total Cards",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // Empty space to fill the remaining area
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
} 