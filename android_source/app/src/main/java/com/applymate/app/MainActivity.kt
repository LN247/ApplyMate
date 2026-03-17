package com.applymate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.applymate.app.ui.screens.AddOpportunityScreen
import com.applymate.app.ui.screens.DashboardScreen
import com.applymate.app.ui.theme.ApplyMateTheme
import com.applymate.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApplyMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val applications by viewModel.applications.collectAsState(initial = emptyList())
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                applications = applications,
                                onAddClick = { navController.navigate("add") },
                                onDeleteClick = { viewModel.deleteApplication(it) }
                            )
                        }
                        composable("add") {
                            AddOpportunityScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { title, org, status, deadline ->
                                    viewModel.addApplication(title, org, status, deadline)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
