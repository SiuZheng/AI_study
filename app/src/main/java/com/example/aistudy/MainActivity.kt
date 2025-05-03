package com.example.aistudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.aistudy.ui.navigation.NavGraph
import com.example.aistudy.ui.theme.AIStudyTheme
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.ViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize ViewModels
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Initialize streak manager with application context
        authViewModel.initializeStreakManager(applicationContext)
        
        // Check and update streak when app opens
        authViewModel.updateStreak()
        
        enableEdgeToEdge()
        setContent {
            AIStudyTheme {
                val navController = rememberNavController()
                val viewModel: ViewModel = viewModel()
                
                // Pass the authViewModel to the NavGraph
                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
