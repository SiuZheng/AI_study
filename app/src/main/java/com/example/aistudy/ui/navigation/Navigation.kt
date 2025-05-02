package com.example.aistudy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aistudy.ui.screen.TestingScreen
import com.example.aistudy.ui.viewmodel.ViewModel

sealed class  Screen (val route:String){
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
    viewModel: ViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Testing.route
    ){
        composable(Screen.Testing.route){
            TestingScreen(chatViewModel=viewModel)
        }
    }

}

