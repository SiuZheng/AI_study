package com.example.aistudy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

sealed class  Screen (val route:String){
    object Home :Screen("home")
    object Chatbot : Screen("chatbot")
    object Profile : Screen("profile")
    object Flashcard : Screen("flashcard")
    object Progress : Screen ("progress")
}
@Composable
fun NavGraph(
    navController: NavHostController,
) {

}