package com.applymate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.applymate.app.ui.screens.*
import com.applymate.app.ui.theme.ApplyMateTheme
import com.applymate.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            ApplyMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val applications by viewModel.applications.collectAsState(initial = emptyList())
                    val documents by viewModel.documents.collectAsState(initial = emptyList())
                    val preferences by viewModel.preferenceProfile.collectAsState(initial = null)
                    val discovered by viewModel.discoveredOpportunities.collectAsState(initial = emptyList())
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { 
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignup = { navController.navigate("signup") }
                            )
                        }
                        composable("signup") {
                            SignupScreen(
                                onSignupSuccess = { 
                                    navController.navigate("dashboard") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                applications = applications,
                                onAddClick = { navController.navigate("add") },
                                onDeleteClick = { viewModel.deleteApplication(it) },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onVaultClick = { navController.navigate("vault") },
                                onDiscoveryClick = { navController.navigate("discovery") },
                                onSettingsClick = { navController.navigate("settings") },
                                onHistoryClick = { navController.navigate("history") }
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
                        composable("vault") {
                            DocumentVaultScreen(
                                documents = documents,
                                onScanClick = { file, category, location ->
                                    viewModel.uploadDocument(file, category, location)
                                },
                                onDeleteClick = { viewModel.deleteDocument(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("discovery") {
                            DiscoveryFeedScreen(
                                opportunities = discovered,
                                onSaveClick = { viewModel.saveDiscoveredOpportunity(it) },
                                onApplyConfirm = { opp, loc -> viewModel.onOpportunityApplied(opp, loc) },
                                onSettingsClick = { navController.navigate("search_settings") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("search_settings") {
                            SearchSettingsScreen(
                                currentProfile = preferences,
                                onSave = { 
                                    viewModel.updatePreferences(it)
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateToSearchSettings = { navController.navigate("search_settings") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                applications = applications,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
