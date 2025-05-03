package com.example.aistudy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aistudy.ui.screen.StudyPlannerScreen
import com.example.aistudy.ui.screen.FlashcardsScreen
import com.example.aistudy.ui.screen.ProgressScreen
import com.example.aistudy.ui.screen.ProfileScreen
import com.example.aistudy.ui.screen.CreateFlashcardsScreen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.ViewModel
import com.example.aistudy.ui.screen.SignupScreen
import com.example.aistudy.ui.screen.LoginScreen
import com.example.aistudy.ui.screen.TestingScreen

sealed class Screen(val route: String) {
    object StudyPlanner : Screen("study_planner")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Flashcards : Screen("flashcards")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object CreateFlashcards : Screen("create_flashcards")
    object Testing :Screen("testing")
}

@Composable
fun NavGraph(navController: NavHostController,
             viewModel: ViewModel,
             authViewModel: AuthViewModel)

{
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.StudyPlanner.route) {
            StudyPlannerScreen(selectedIndex = 0, navController = navController)
        }
        composable(Screen.Flashcards.route) {
            FlashcardsScreen(selectedIndex = 1, navController = navController)
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
            CreateFlashcardsScreen(selectedIndex = 1, navController = navController)
        }
        composable(Screen.Testing.route) {
            TestingScreen(chatViewModel = viewModel, navController = navController)
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

