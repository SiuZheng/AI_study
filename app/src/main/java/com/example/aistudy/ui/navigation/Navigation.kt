package com.example.aistudy.ui.navigation

import TestingScreen
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aistudy.ui.screen.StudyPlannerScreen
import com.example.aistudy.ui.screen.FlashcardsScreen
import com.example.aistudy.ui.screen.FlashcardDetailScreen
import com.example.aistudy.ui.screen.ProgressScreen
import com.example.aistudy.ui.screen.ProfileScreen
import com.example.aistudy.ui.screen.CreateFlashcardsScreen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.ViewModel
import com.example.aistudy.ui.viewmodel.FlashcardViewModel
import com.example.aistudy.ui.screen.SignupScreen
import com.example.aistudy.ui.screen.LoginScreen
import com.example.aistudy.ui.screen.SplashScreen

sealed class Screen(val route: String) {
    object StudyPlanner : Screen("study_planner")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Flashcards : Screen("flashcards")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object CreateFlashcards : Screen("create_flashcards")
    object Splash :Screen("splashscreen")
    object Testing :Screen("testing")
}

// Define route constants
private const val FLASHCARD_DETAIL_ROUTE = "flashcard_detail"

@Composable
fun NavGraph(navController: NavHostController,
             viewModel: ViewModel,
             authViewModel: AuthViewModel)

{
    // Create a shared FlashcardViewModel for use across screens
    val flashcardViewModel: FlashcardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.StudyPlanner.route) {
            StudyPlannerScreen(selectedIndex = 0, navController = navController)
        }
        composable(Screen.Flashcards.route) {
            FlashcardsScreen(selectedIndex = 1, navController = navController, viewModel = flashcardViewModel)
        }
        
        // Flashcard detail route with parameter
        composable("$FLASHCARD_DETAIL_ROUTE/{setId}") { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            FlashcardDetailScreen(
                setId = setId,
                navController = navController,
                viewModel = flashcardViewModel
            )
        }
        
        composable(Screen.Progress.route) {
            ProgressScreen(selectedIndex = 2, navController = navController)
        }
        composable(Screen.Login.route){
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Signup.route){
            SignupScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController,authViewModel = authViewModel)
        }
        composable(Screen.CreateFlashcards.route) {
            CreateFlashcardsScreen(selectedIndex = 1, navController = navController, viewModel = flashcardViewModel)
        }

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Testing.route) {
            TestingScreen(chatViewModel = viewModel,navController = navController)
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    NavGraph(
        navController = navController,
        viewModel = viewModel,
        authViewModel = authViewModel
    )
}