package com.example.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.LanguageManager
import com.example.i18n.stringResourceLoc
import com.example.types.LocationSource
import com.example.ui.components.TopBarWithProgress

@Composable
fun ReviewScreen(navController: NavController, viewModel: SharedViewModel) {
    val name by viewModel.name.collectAsState()
    val mobile by viewModel.mobile.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val businessType by viewModel.businessType.collectAsState()
    val description by viewModel.description.collectAsState()
    val capital by viewModel.capital.collectAsState()

    val locationText = remember(currentLocation) {
        val parts = listOfNotNull(currentLocation.mandalName, currentLocation.districtName, currentLocation.stateName)
        val main = if (parts.isNotEmpty()) parts.joinToString(", ") else "Unspecified Location"
        val pin = currentLocation.pincode
        val sourceBadge = when (currentLocation.source) {
            LocationSource.GPS -> " [Detected by GPS]"
            LocationSource.DEMO -> " [Demo Scenario]"
            LocationSource.PINCODE -> " [Resolved from Pincode]"
            LocationSource.MANUAL -> " [Selected Manually]"
        }
        (if (!pin.isNullOrBlank()) "$main - $pin" else main) + sourceBadge
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBarWithProgress(navController, currentStep = 6, totalSteps = 6, showBack = true)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResourceLoc("review_plan"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Make sure everything is correct before starting multi-agent analysis.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                ReviewCard("Personal Information", "$name\nMobile: ${mobile.ifBlank { "Not provided" }}") { navController.navigate("profile") }
                ReviewCard("Location", locationText) { navController.navigate("location") }
                ReviewCard("Business Idea", "${viewModel.businessCategory.collectAsState().value}\n$description") { navController.navigate("business") }
                ReviewCard("Capital", "₹$capital") { navController.navigate("capital") }

                Spacer(modifier = Modifier.height(32.dp))
            }
            
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { 
                        // 1. Take snapshot of verified location & inputs
                        viewModel.getLocationSnapshot()
                        val currentLang = LanguageManager.currentLanguage.value.code
                        
                        // 2. Authoritative Single AnalysisRequest Creation
                        val req = viewModel.prepareAndStartAnalysis(currentLang)
                        Log.d("REVIEW_SCREEN", "[UDYORA ASSESSMENT CREATED] Starting analysis pipeline for ${req.requestId}")
                        
                        // 3. Deterministic Forward Navigation to Processing Screen
                        navController.navigate("processing") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("START ANALYSIS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(label: String, value: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
