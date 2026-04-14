package com.applymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.applymate.app.data.PreferenceProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    currentProfile: PreferenceProfile?,
    onSave: (PreferenceProfile) -> Unit,
    onBack: () -> Unit
) {
    var fieldOfStudy by remember { mutableStateOf(currentProfile?.fieldOfStudy ?: "") }
    var locations by remember { mutableStateOf(currentProfile?.preferredLocations ?: "") }
    var keywords by remember { mutableStateOf(currentProfile?.keywords ?: "") }
    var internshipType by remember { mutableStateOf(currentProfile?.internshipType ?: "Remote") }
    var degreeLevel by remember { mutableStateOf(currentProfile?.degreeLevel ?: "Undergraduate") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSave(PreferenceProfile(
                            fieldOfStudy = fieldOfStudy,
                            preferredLocations = locations,
                            keywords = keywords,
                            internshipType = internshipType,
                            degreeLevel = degreeLevel
                        ))
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Personalize your Discovery Engine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = fieldOfStudy,
                onValueChange = { fieldOfStudy = it },
                label = { Text("Field of Study") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = locations,
                onValueChange = { locations = it },
                label = { Text("Preferred Locations (e.g. Japan, Germany)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text("Keywords (e.g. AI, Sustainability)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            Text("Internship Preference", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Remote", "On-site", "Hybrid").forEach { type ->
                    FilterChip(
                        selected = internshipType == type,
                        onClick = { internshipType = type },
                        label = { Text(type) }
                    )
                }
            }

            Text("Degree Level", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Undergraduate", "Masters", "PhD").forEach { level ->
                    FilterChip(
                        selected = degreeLevel == level,
                        onClick = { degreeLevel = level },
                        label = { Text(level) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    onSave(PreferenceProfile(
                        fieldOfStudy = fieldOfStudy,
                        preferredLocations = locations,
                        keywords = keywords,
                        internshipType = internshipType,
                        degreeLevel = degreeLevel
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Update Engine Preferences", fontWeight = FontWeight.Bold)
            }
        }
    }
}
