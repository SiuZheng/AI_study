package com.example.aistudy.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aistudy.ui.viewmodel.FlashcardData
import com.example.aistudy.ui.viewmodel.StudyTimeData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "ProgressCharts"

@Composable
fun CircularProgressChart(
    studyTimeData: StudyTimeData,
    modifier: Modifier = Modifier,
    completedTimeFormatted: String = "",
    incompleteTimeFormatted: String = ""
) {
    val completedRatio = if (studyTimeData.completed + studyTimeData.incomplete > 0) {
        studyTimeData.completed / (studyTimeData.completed + studyTimeData.incomplete)
    } else {
        0f
    }
    
    val animatedProgress = animateFloatAsState(
        targetValue = completedRatio,
        animationSpec = tween(durationMillis = 1000)
    )
    
    // Extract the theme color before the Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            // Background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(completedRatio * 100).roundToInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Display time values for completed and incomplete
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Text(
                        text = if (completedTimeFormatted.isNotEmpty()) "Completed: $completedTimeFormatted" else "Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Text(
                        text = if (incompleteTimeFormatted.isNotEmpty()) "Incomplete: $incompleteTimeFormatted" else "Incomplete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun BarChart(
    flashcardData: List<FlashcardData>,
    interval: String, // "Week", "Month", or "Year"
    modifier: Modifier = Modifier
) {
    // Add debug log to see the data being passed to the chart
    LaunchedEffect(flashcardData) {
        Log.d(TAG, "BarChart received flashcardData: ${flashcardData.size} items")
        flashcardData.forEach { 
            Log.d(TAG, "  - Date: ${it.date}, Count: ${it.count}")
        }
    }
    
    // Handle empty data case
    if (flashcardData.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available for this time period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        return
    }
    
    // Find the maximum value for scaling, ensuring it's at least 1
    val maxValue = remember(flashcardData) {
        val max = flashcardData.maxOfOrNull { it.count } ?: 0
        max(max, 1).toFloat()
    }
    
    val dateFormat = remember(interval) {
        when (interval) {
            "Week" -> SimpleDateFormat("EEE", Locale.getDefault()) // Day of week (e.g., Mon)
            "Month" -> SimpleDateFormat("d", Locale.getDefault()) // Day of month (e.g., 15)
            "Year" -> SimpleDateFormat("MMM", Locale.getDefault()) // Month (e.g., Jan)
            else -> SimpleDateFormat("d", Locale.getDefault())
        }
    }
    
    // Extract theme color before Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Y-axis label
        Text(
            text = "Flashcards Created",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Bar chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Take remaining space
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .padding(
                    top = 24.dp,
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 8.dp
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / flashcardData.size
                val barPadding = min(8.dp.toPx(), barWidth * 0.2f)
                val effectiveBarWidth = barWidth - (barPadding * 2)
                
                // Draw horizontal grid lines
                val gridLineCount = 5
                val gridLineSpacing = size.height / gridLineCount
                
                repeat(gridLineCount + 1) { i ->
                    val y = size.height - (i * gridLineSpacing)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                // Draw bars
                flashcardData.forEachIndexed { index, data ->
                    // Calculate bar height - ensure non-zero values have visible bars
                    val barHeight = if (data.count > 0) {
                        max((data.count.toFloat() / maxValue) * size.height, 5.dp.toPx())
                    } else {
                        0f
                    }
                    
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(
                            x = index * barWidth + barPadding,
                            y = size.height - barHeight
                        ),
                        size = Size(
                            width = effectiveBarWidth,
                            height = barHeight
                        )
                    )
                }
            }
            
            // Draw x-axis labels below the graph
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pre-format the dates outside of the composable loop
                val formattedDates = remember(flashcardData, dateFormat) {
                    flashcardData.map { data -> dateFormat.format(data.date) }
                }
                
                formattedDates.forEach { formattedDate ->
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
} 