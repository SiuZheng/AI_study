package com.example.aistudy.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aistudy.R
import com.example.aistudy.data.UserData
import com.example.aistudy.ui.navigation.Screen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.UserDataStatus
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.ExitToApp

@Composable
fun ProfileScreen(
    navController: NavController? = null,
    authViewModel: AuthViewModel
) {
    val currentUser = authViewModel.currentUser.value
    val userData by authViewModel.userData.observeAsState()
    val userDataStatus by authViewModel.userDataStatus.observeAsState(UserDataStatus.Initial)
    
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isSmallScreen = screenWidth < 360.dp
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isEditing by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf("") }
    
    // Initialize with current data when editing starts
    LaunchedEffect(isEditing) {
        if (isEditing && userData != null) {
            editedUsername = userData?.username ?: ""
        }
    }
    
    // Show loading indicator or error message
    LaunchedEffect(userDataStatus) {
        when (userDataStatus) {
            is UserDataStatus.Error -> {
                snackbarHostState.showSnackbar(
                    message = (userDataStatus as UserDataStatus.Error).message
                )
            }
            else -> {}
        }
    }
    
    // If currentUser is null, fetch it again (handle deep linking cases)
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController?.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (userData == null) {
            authViewModel.fetchUserData(currentUser.uid)
        }
    }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!isEditing) {
                FloatingActionButton(
                    onClick = { isEditing = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Edit, "Edit Profile")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(
                    start = if (isSmallScreen) 8.dp else 16.dp,
                    end = if (isSmallScreen) 8.dp else 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            if (isEditing) {
                ProfileEditHeader(
                    userData = userData,
                    editedUsername = editedUsername,
                    onUsernameChange = { editedUsername = it },
                    onSave = {
                        userData?.let { currentUserData ->
                            val updatedUserData = currentUserData.copy(username = editedUsername)
                            authViewModel.updateUserData(updatedUserData)
                        }
                        isEditing = false
                    },
                    onCancel = { isEditing = false },
                    isSmallScreen = isSmallScreen
                )
            } else {
                ProfileHeader(
                    user = currentUser,
                    userData = userData,
                    isSmallScreen = isSmallScreen
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StreakAchievement(streak = userData?.streak ?: 0, isSmallScreen = isSmallScreen)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            UserStats(userData = userData)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SignOutButton(onClick = { 
                authViewModel.logout()
                navController?.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}

@Composable
fun ProfileEditHeader(
    userData: UserData?,
    editedUsername: String,
    onUsernameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSmallScreen: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile picture
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(if (isSmallScreen) 80.dp else 100.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Use a placeholder image
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Edit button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(if (isSmallScreen) 24.dp else 30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { /* TODO: Add photo picker functionality */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(if (isSmallScreen) 14.dp else 18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username field
            OutlinedTextField(
                value = editedUsername,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email field (read-only)
            OutlinedTextField(
                value = userData?.email ?: "",
                onValueChange = { },
                label = { Text("Email") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                readOnly = true,
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileHeader(
    user: FirebaseUser?,
    userData: UserData?,
    isSmallScreen: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 180.dp else 200.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSmallScreen) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 80.dp else 100.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // Use a placeholder image for now
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Edit button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(if (isSmallScreen) 24.dp else 30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { /* TODO: Add photo picker functionality */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(if (isSmallScreen) 14.dp else 18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(if (isSmallScreen) 12.dp else 16.dp))
            
            // User info
            Column {
                Text(
                    text = userData?.username ?: user?.displayName ?: "AI Study User",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = userData?.email ?: user?.email ?: "user@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "School",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Computer Science",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Malaysia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                // Show member since date
                userData?.createdAt?.let { createdAt ->
                    val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    val memberSince = formatter.format(createdAt)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Member since: $memberSince",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakAchievement(streak: Int, isSmallScreen: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    
    // Get date information
    val today = LocalDate.now()
    val currentMonth = today.month.toString()
    val currentYear = today.year.toString()
    
    // Calculate first day of month and its offset
    val firstDayOfMonth = today.withDayOfMonth(1)
    val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
    
    // Determine how many weeks to show initially vs when expanded
    val weeksToShow = 6
    
    // Calculate the day width based on screen size
    val dayWidth = if (isSmallScreen) 36.dp else 40.dp

    // Animation for expanding/collapsing calendar
    val expandAnimation by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "expand"
    )
    
    Column {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = if (isSmallScreen) 12.dp else 16.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isSmallScreen) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$streak-day streak",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Flame with calendar icon
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Orange flame background
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color(0xFFFF9947),
                                shape = CircleShape
                            )
                    )
                    
                    // Calendar with day number
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(Color(0xFFFF4081))
                            )
                            
                            Text(
                                text = streak.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Month and year header (always visible)
                Text(
                    text = "$currentMonth $currentYear",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                
                // Show "Click to view calendar" hint when collapsed
                if (!expanded) {
                    Text(
                        text = "Click to view calendar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    // Down indicator showing there's more to expand
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Expand calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                    )
                }
                
                // Calendar section - only visible when expanded
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        // Weekday header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
                            weekDays.forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.width(dayWidth),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Calendar grid
                        for (week in 0 until weeksToShow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (dayOfWeek in 0 until 7) {
                                    val dayNumber = (week * 7) + dayOfWeek - firstDayOffset + 1
                                    val isCurrentMonth = dayNumber in 1..today.month.length(today.isLeapYear)
                                    val date = firstDayOfMonth.plusDays((dayNumber - 1).toLong())
                                    val isToday = isCurrentMonth && date.isEqual(today)
                                    
                                    // Simulate days that are part of streak
                                    // Here we assume past days in streak - in a real app you'd use actual login data
                                    val daysBeforeToday = today.dayOfMonth - date.dayOfMonth
                                    val isPartOfStreak = isCurrentMonth && 
                                                        daysBeforeToday >= 0 && 
                                                        daysBeforeToday < streak
                                    
                                    if (isCurrentMonth) {
                                        Box(
                                            modifier = Modifier
                                                .size(dayWidth)
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(
                                                    when {
                                                        isToday -> Color(0xFFFF9947) // Today's color
                                                        isPartOfStreak -> Color(0xFFFFD700).copy(alpha = 0.6f) // Streak days color
                                                        else -> Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isToday || isPartOfStreak) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = when {
                                                        isToday -> Color.White
                                                        isPartOfStreak -> Color.Black
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                
                                                if (isToday || isPartOfStreak) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 2.dp)
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isToday) Color.White else Color(0xFFFF9947)
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty box to maintain grid spacing
                                        Box(
                                            modifier = Modifier.size(dayWidth)
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
fun UserStats(userData: UserData?) {
    Column {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                StatItem(
                    title = "Total Study Hours", 
                    value = (userData?.totalStudyHours ?: 0).toString()
                )
                
                StatItem(
                    title = "Flashcards Created", 
                    value = (userData?.flashcardsCreated ?: 0).toString(),
                    showDivider = false
                )
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (showDivider) {
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun SignOutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Sign Out",
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 