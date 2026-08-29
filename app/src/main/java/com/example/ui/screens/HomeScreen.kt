package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.AssessmentEntity
import com.example.data.UserEntity
import com.example.i18n.stringResourceLoc
import com.example.services.AdminAuthService
import com.example.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    
    val userName by viewModel.name.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var activeUserId by remember { mutableStateOf(currentUserId) }
    var currentUserEntity by remember { mutableStateOf<UserEntity?>(null) }
    var logoTapCount by remember { mutableStateOf(0) }

    LaunchedEffect(currentUserId) {
        val savedId = activeUserId ?: UserPreferences.getUserId(context).firstOrNull()
        if (!savedId.isNullOrBlank()) {
            activeUserId = savedId
            val usr = db.userDao().getUserById(savedId)
            if (usr != null) {
                currentUserEntity = usr
                viewModel.loadUser(usr)
            }
        }
    }

    val userIdToQuery = activeUserId ?: "user_default"
    val assessmentsFlow = remember(userIdToQuery) { db.assessmentDao().getAssessmentsForUser(userIdToQuery) }
    val assessments by assessmentsFlow.collectAsState(initial = emptyList())

    val isSuspended = currentUserEntity?.status == "SUSPENDED"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(
                        modifier = Modifier.clickable {
                            logoTapCount++
                            if (logoTapCount >= 5) {
                                logoTapCount = 0
                                navController.navigate("admin_auth")
                            }
                        }
                    ) {
                        Text(
                            text = if (userName.isNotBlank() && assessments.isNotEmpty()) "Welcome back, $userName" else if (userName.isNotBlank()) "Welcome, $userName" else "Welcome to UDYORA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Business Advisory Workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (AdminAuthService.isAuthenticatedAdmin()) {
                        IconButton(onClick = { navController.navigate("admin_dashboard") }) {
                            Icon(Icons.Default.Lock, contentDescription = "Admin Portal", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            if (isSuspended) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Your UDYORA account is currently suspended. You cannot start new assessments. Please contact support.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (assessments.isEmpty()) {
                // First-Time Entrepreneur Home Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start Your First Business Assessment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Evaluate your micro-enterprise proposal using GPS location intelligence and multi-agent feasibility analysis under SIH26091.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isSuspended) {
                            Toast.makeText(context, "Your UDYORA account is currently suspended.", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.clearLocation()
                            navController.navigate("location")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuspended) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START BUSINESS ASSESSMENT",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                // Entrepreneur Past Assessments List
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Past Business Proposals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            if (isSuspended) {
                                Toast.makeText(context, "Your UDYORA account is currently suspended.", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.clearLocation()
                                navController.navigate("location")
                            }
                        },
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Assessment", fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assessments) { assessment ->
                        val parsedLocation = try {
                            val obj = JSONObject(assessment.locationJson)
                            val mandal = obj.optString("mandal", "")
                            val dist = obj.optString("district", "")
                            if (mandal.isNotBlank() && dist.isNotBlank()) "$mandal, $dist" else "Location Set"
                        } catch (e: Exception) {
                            "Location Set"
                        }

                        val dateStr = try {
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            sdf.format(Date(assessment.date))
                        } catch (e: Exception) {
                            ""
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadSessionFromAssessmentEntity(assessment)
                                    val currentLang = com.example.i18n.LanguageManager.currentLanguage.value.code
                                    viewModel.prepareAndStartAnalysis(currentLang)
                                    navController.navigate("processing")
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = assessment.businessType,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Score: ${assessment.feasibilityScore}/100",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Location: $parsedLocation",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Capital: ₹${String.format("%,.0f", assessment.availableCapital)} • Date: $dateStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("View Advisory Report", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
