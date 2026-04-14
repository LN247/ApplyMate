package com.applymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import com.applymate.app.data.DiscoveredOpportunity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFeedScreen(
    opportunities: List<DiscoveredOpportunity>,
    onSaveClick: (DiscoveredOpportunity) -> Unit,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedOpportunity by remember { mutableStateOf<DiscoveredOpportunity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showAutofillSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discovery Engine", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (opportunities.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Searching for opportunities...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(opportunities) { opp ->
                    DiscoveryCard(
                        opportunity = opp,
                        onApplyClick = {
                            selectedOpportunity = opp
                            showAutofillSheet = true
                        },
                        onSaveClick = { onSaveClick(opp) }
                    )
                }
            }
        }

        if (showAutofillSheet && selectedOpportunity != null) {
            ModalBottomSheet(
                onDismissRequest = { showAutofillSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AutofillAssistantContent(
                    opportunity = selectedOpportunity!!,
                    onFillClick = {
                        showAutofillSheet = false
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(context, Uri.parse(selectedOpportunity!!.link))
                    }
                )
            }
        }
    }
}

@Composable
fun DiscoveryCard(
    opportunity: DiscoveredOpportunity,
    onApplyClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(
                        text = "${opportunity.matchScore}% Match",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = opportunity.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(opportunity.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.organization, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Now")
                }
                OutlinedButton(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    enabled = !opportunity.isSaved
                ) {
                    Icon(
                        if (opportunity.isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (opportunity.isSaved) "Saved" else "Save")
                }
            }
        }
    }
}

@Composable
fun AutofillAssistantContent(
    opportunity: DiscoveredOpportunity,
    onFillClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = androidx.compose.foundation.shape.CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                Icons.Default.AutoFixHigh,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Autofill Co-pilot",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "I can help you fill the form for ${opportunity.organization}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Information to be filled:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AutofillItem("Full Name", "✓")
                AutofillItem("Email Address", "✓")
                AutofillItem("LinkedIn Profile", "✓")
                AutofillItem("Latest Resume", "✓")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFillClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Launch & Fill Form", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun AutofillItem(label: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(status, color = Color(0xFF34A853), fontWeight = FontWeight.Bold)
    }
}
