package com.example.aistudy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aistudy.ui.screen.LoginScreen
import com.example.aistudy.ui.screen.ProfileScreen
import com.example.aistudy.ui.screen.SignupScreen
import com.example.aistudy.ui.screen.TestingScreen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.ViewModel

sealed class  Screen (val route:String){
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home :Screen("home")
    object Chatbot : Screen("chatbot")
    object Profile : Screen("profile")
    object Flashcard : Screen("flashcard")
    object Progress : Screen ("progress")
    object Testing :Screen("testing")
}
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ViewModel,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ){
        composable(Screen.Login.route){
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.Signup.route){
            SignupScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController,authViewModel = authViewModel)
        }
        
        composable(Screen.Testing.route){
            TestingScreen(chatViewModel=viewModel)
        }
    }
}

