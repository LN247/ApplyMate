package com.applymate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
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
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    val mainScreens = listOf("dashboard", "discovery", "vault", "history", "settings")
                    val showBottomBar = currentDestination?.route in mainScreens

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    val items = listOf(
                                        Triple("dashboard", Icons.Default.Dashboard, "Dashboard"),
                                        Triple("discovery", Icons.Default.Search, "Discovery"),
                                        Triple("vault", Icons.Default.Lock, "Vault"),
                                        Triple("history", Icons.Default.History, "History"),
                                        Triple("settings", Icons.Default.Settings, "Settings")
                                    )
                                    items.forEach { (route, icon, label) ->
                                        NavigationBarItem(
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                                            onClick = {
                                                navController.navigate(route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController, 
                            startDestination = "login",
                            modifier = Modifier.padding(innerPadding)
                        ) {
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
                            val userProfile by viewModel.userProfile.collectAsState()
                            DashboardScreen(
                                applications = applications,
                                userProfile = userProfile,
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
                            val userProfile by viewModel.userProfile.collectAsState()
                            DocumentVaultScreen(
                                documents = documents,
                                userProfile = userProfile,
                                onScanClick = { file, category, location ->
                                    viewModel.uploadDocument(file, category, location)
                                },
                                onHeadshotUpdate = { file ->
                                    viewModel.updateHeadshot(file)
                                },
                                onDeleteClick = { viewModel.deleteDocument(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("discovery") {
                            val isSearching by viewModel.isSearching.collectAsState()
                            DiscoveryFeedScreen(
                                opportunities = discovered,
                                isSearching = isSearching,
                                onSearchClick = { viewModel.searchOpportunities(it) },
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
