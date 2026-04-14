package com.applymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.applymate.app.data.ApplicationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    applications: List<ApplicationEntity>,
    onBack: () -> Unit
) {
    val historyApps = applications.filter { it.status != "Pending" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (historyApps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No History Yet", style = MaterialTheme.typography.titleMedium)
                Text("Completed applications will appear here", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyApps) { app ->
                    HistoryItem(app)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(app: ApplicationEntity) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(app.organization, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = if (app.status == "Accepted") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(
                        text = app.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (app.status == "Accepted") Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
