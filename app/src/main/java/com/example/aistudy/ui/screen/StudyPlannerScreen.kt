package com.example.aistudy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StudyPlannerScreen(selectedIndex: Int = 0, navController: NavController) {
    var selectedColumn by remember { mutableStateOf(0) }
    val columns = listOf("1", "2", "3")
    val plans = listOf("Plan 1", "Plan 2")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedIndex,
                navController = navController
            )
        },
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
                    text = "Study Planner",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Column Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                columns.forEachIndexed { idx, col ->
                    Button(
                        onClick = { selectedColumn = idx },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedColumn == idx) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedColumn == idx) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(col, fontSize = 14.sp)
                    }
                }
            }
            
            // Plan Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                plans.forEach { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                plan, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Add new plan card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable { /* TODO: Add new plan */ },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = "Add Plan", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
} 