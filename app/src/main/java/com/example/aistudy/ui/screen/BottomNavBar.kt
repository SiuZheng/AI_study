package com.example.aistudy.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController

/**
 * Routes used for navigation within the app
 */
object Routes {
    const val STUDY_PLANNER = "study_planner"
    const val FLASHCARDS = "flashcards"
    const val CREATE_FLASHCARDS = "create_flashcards"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"
    const val TESTING = "testing"
    const val CHAT = "chat"
}

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    navController: NavController? = null,
    onItemSelected: (Int) -> Unit = {},
    onChatClicked: () -> Unit = {}
) {
    // Define nav items with filled and outlined icons
    val navItems = listOf(
        NavBarItem(
            label = "Study",
            filledIcon = Icons.Filled.DateRange,
            outlinedIcon = Icons.Outlined.DateRange,
            route = Routes.STUDY_PLANNER
        ),
        NavBarItem(
            label = "Flashcards",
            filledIcon = Icons.Filled.NoteAdd,
            outlinedIcon = Icons.Outlined.NoteAdd,
            route = Routes.FLASHCARDS
        ),
        NavBarItem(
            label = "Progress",
            filledIcon = Icons.Filled.FormatListBulleted,
            outlinedIcon = Icons.Outlined.FormatListBulleted,
            route = Routes.PROGRESS
        ),
        NavBarItem(
            label = "Profile",
            filledIcon = Icons.Filled.Person,
            outlinedIcon = Icons.Outlined.Person,
            route = Routes.PROFILE
        )
    )

    // Navigation bar
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First two items
            navItems.take(2).forEachIndexed { index, item ->
                NavItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { 
                        onItemSelected(index)
                        navController?.navigate(item.route) { 
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
            
            // Chat button in the middle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                IconButton(
                    onClick = { 
                        onChatClicked()
                        navController?.navigate(Routes.TESTING)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubble,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Last two items
            navItems.takeLast(2).forEachIndexed { index, item ->
                NavItem(
                    item = item,
                    isSelected = selectedIndex == index + 2,
                    onClick = { 
                        onItemSelected(index + 2)
                        navController?.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    item: NavBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                contentDescription = item.label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

data class NavBarItem(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val route: String
) 