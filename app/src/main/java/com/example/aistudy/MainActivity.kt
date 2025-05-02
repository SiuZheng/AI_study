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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.aistudy.ui.navigation.NavGraph
import com.example.aistudy.ui.theme.AIStudyTheme
import com.example.aistudy.ui.viewmodel.ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            AIStudyTheme {
                val navController = rememberNavController()
                val viewModel: ViewModel = viewModel()
                NavGraph(
                    navController = navController,
                    viewModel = viewModel
                )

            }
        }
    }
}
