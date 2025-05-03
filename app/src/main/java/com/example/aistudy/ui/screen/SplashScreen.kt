package com.example.aistudy.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aistudy.ui.navigation.Screen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Observe the current user
    val currentUser by authViewModel.currentUser.observeAsState()
    
    // Animation states
    var visible by remember { mutableStateOf(false) }
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )
    
    // Infinite animation for pulsing and rotating effects
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    // Subtle pulsing effect for the logo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Subtle rotation for book icon
    val bookRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bookRotation"
    )
    
    // Subtle rotation for AI icon
    val aiRotation by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aiRotation"
    )
    
    // Start animation when composed
    LaunchedEffect(Unit) {
        visible = true
    }

    // Navigate after delay
    LaunchedEffect(currentUser) {
        delay(2000) // 2-second delay for splash screen
        if (currentUser != null) {
            // User is already logged in, navigate to Profile screen
            navController.navigate(Screen.StudyPlanner.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // User not logged in, navigate to Login screen
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }
    
    // Initialize context for streak manager
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        authViewModel.initializeStreakManager(context)
        authViewModel.updateStreak()
    }

    // Dark background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E), // Dark blue
            Color(0xFF0F3460)  // Deep navy
        )
    )
    
    // Create a primary color with darker shade
    val primaryColor = Color(0xFF16213E)
    val accentColor = Color(0xFF4ECCA3)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        // Decorative elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw some circles for decoration
            drawCircle(
                color = accentColor.copy(alpha = 0.05f),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
            
            drawCircle(
                color = accentColor.copy(alpha = 0.05f),
                radius = size.width * 0.3f,
                center = Offset(size.width * 0.1f, size.height * 0.8f)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Custom logo with book and AI robot icons
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale * pulse) // Apply scale and pulse animations
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Book Icon",
                        tint = accentColor,
                        modifier = Modifier
                            .size(60.dp)
                            .rotate(bookRotation) // Apply rotation animation
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(60.dp)
                            .rotate(aiRotation) // Apply rotation animation
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Name
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(
                            animationSpec = tween(1000),
                            initialOffsetY = { fullHeight -> fullHeight / 4 }
                        )
            ) {
                Text(
                    text = "AI Study",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tagline
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200)) +
                        slideInVertically(
                            animationSpec = tween(1200),
                            initialOffsetY = { fullHeight -> fullHeight / 4 }
                        )
            ) {
                Text(
                    text = "Learn smartly, grow quickly",
                    fontSize = 18.sp,
                    color = accentColor
                )
            }
        }
    }
} 