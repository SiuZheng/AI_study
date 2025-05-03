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
import androidx.navigation.compose.rememberNavController
import com.example.aistudy.ui.navigation.Navigation
import com.example.aistudy.ui.theme.AIStudyTheme
import com.example.aistudy.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel the Android way (not via composable)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.initializeStreakManager(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            AIStudyTheme {
                Navigation()
            }
        }
    }
}
