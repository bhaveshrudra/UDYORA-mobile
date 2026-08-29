package com.example.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.LanguageManager
import com.example.types.AgentStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnalysisProcessingScreen(
    navController: NavController,
    viewModel: SharedViewModel
) {
    val orchestrator = viewModel.orchestrator
    val orchestratorState by orchestrator.orchestratorState.collectAsState()
    val agentStates by orchestrator.agentStates.collectAsState()
    val activeRequest by viewModel.activeAnalysisRequest.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val orchestratorError by orchestrator.errorMessage.collectAsState()
    
    val scope = rememberCoroutineScope()
    var hasStarted by remember { mutableStateOf(false) }
    var isDebugExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(activeRequest) {
        if (activeRequest != null && !hasStarted && orchestratorState != AgentStatus.RUNNING) {
            hasStarted = true
            scope.launch {
                Log.d("ORCHESTRATOR", "[ORCHESTRATOR] Starting pipeline for Request ID: ${activeRequest?.requestId}")
                val currentLang = LanguageManager.currentLanguage.value.code
                val snapshot = viewModel.createAssessmentInputSnapshot(currentLang)
                orchestrator.runAssessmentPipeline(snapshot)
            }
        }
    }

    LaunchedEffect(orchestratorState) {
        if (orchestratorState == AgentStatus.SUCCESS) {
            viewModel.setAnalysisState(AnalysisState.COMPLETED)
            delay(500) // allow user to visibly see all checkmarks
            navController.navigate("result") {
                popUpTo("processing") { inclusive = true }
            }
        } else if (orchestratorState == AgentStatus.FAILED) {
            viewModel.setAnalysisState(AnalysisState.FAILED)
        }
    }

    val req = activeRequest
    val locText = req?.location?.displayText ?: viewModel.currentLocation.collectAsState().value.displayText
    val busText = req?.businessType?.displayName ?: viewModel.businessType.collectAsState().value
    val capText = req?.availableCapital?.let { "₹${String.format("%,.0f", it)}" } ?: "₹${viewModel.capital.collectAsState().value}"
    val langText = req?.languageCode ?: LanguageManager.currentLanguage.value.code

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "UDYORA MULTI-AGENT PIPELINE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Evaluating Your Proposal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "UDYORA specialized intelligence agents are performing multi-agent feasibility scoring and financial structuring.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Expandable AGENT DEBUG & AUDIT PANEL (Rule 16 & 26)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isDebugExpanded = !isDebugExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AGENT EXECUTION & AUDIT PANEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Icon(
                            imageVector = if (isDebugExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Debug",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = isDebugExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("REQUEST ID: ${req?.requestId ?: "REQ-INITIALIZING"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("LOCATION: $locText", style = MaterialTheme.typography.bodySmall)
                            Text("BUSINESS: $busText", style = MaterialTheme.typography.bodySmall)
                            Text("CAPITAL: $capText | LANG: $langText", style = MaterialTheme.typography.bodySmall)
                            Text("PIPELINE STATUS: ${orchestratorState.name}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (orchestratorState == AgentStatus.FAILED || analysisState == AnalysisState.FAILED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ANALYSIS COULD NOT BE COMPLETED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = orchestratorError ?: "Validation failed. Please verify your location and input capital.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    hasStarted = false
                                    viewModel.setAnalysisState(AnalysisState.RUNNING)
                                    val currentLang = LanguageManager.currentLanguage.value.code
                                    viewModel.prepareAndStartAnalysis(currentLang)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Analysis")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate("review") { popUpTo("processing") { inclusive = true } } }
                            ) {
                                Text("Back to Review")
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AgentExecutionItem("1. Business Analysis Agent", agentStates["business"] ?: AgentStatus.IDLE)
                AgentExecutionItem("2. Market Intelligence Agent", agentStates["market"] ?: AgentStatus.IDLE)
                AgentExecutionItem("3. Financial Structuring Engine", agentStates["financial"] ?: AgentStatus.IDLE)
                AgentExecutionItem("4. Government Scheme Agent", agentStates["scheme"] ?: AgentStatus.IDLE)
                AgentExecutionItem("5. Risk Analysis Agent", agentStates["risk"] ?: AgentStatus.IDLE)
                AgentExecutionItem("6. Evidence Validation Agent", agentStates["evidence"] ?: AgentStatus.IDLE)
                AgentExecutionItem("7. Final Advisory Agent", agentStates["final"] ?: AgentStatus.IDLE)
            }
        }
    }
}

@Composable
fun AgentExecutionItem(name: String, status: AgentStatus) {
    val isActive = status == AgentStatus.RUNNING
    val isCompleted = status == AgentStatus.SUCCESS
    val isError = status == AgentStatus.FAILED

    val bgColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isActive -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
            isCompleted -> Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color(0xFF38A169),
                modifier = Modifier.size(24.dp)
            )
            isError -> Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "Pending",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = when (status) {
                    AgentStatus.IDLE -> "Pending execution"
                    AgentStatus.RUNNING -> "Executing analysis..."
                    AgentStatus.SUCCESS -> "✓ Completed and validated"
                    AgentStatus.PARTIAL -> "Completed with warnings"
                    AgentStatus.FAILED -> "Failed validation"
                    AgentStatus.INSUFFICIENT_DATA -> "Insufficient local data"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
