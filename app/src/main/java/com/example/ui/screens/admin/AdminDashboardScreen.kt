package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.AdminRole
import com.example.data.AdminUserEntity
import com.example.data.AppDatabase
import com.example.data.AssessmentEntity
import com.example.data.UserEntity
import com.example.services.AdminAuthService
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    if (!AdminAuthService.isAuthenticatedAdmin()) {
        LaunchedEffect(Unit) {
            navController.navigate("admin_auth") {
                popUpTo("admin_auth") { inclusive = true }
            }
        }
        return
    }

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var currentRole by remember { mutableStateOf(AdminAuthService.currentAdmin.role) }

    var totalUsers by remember { mutableStateOf(0) }
    var activeUsers by remember { mutableStateOf(0) }
    var suspendedUsers by remember { mutableStateOf(0) }
    var totalAssessments by remember { mutableStateOf(0) }

    LaunchedEffect(currentRole) {
        val users: List<UserEntity> = db.userDao().getAllUsers().firstOrNull() ?: emptyList()
        val assessments: List<AssessmentEntity> = db.assessmentDao().getAllAssessments().firstOrNull() ?: emptyList()

        totalUsers = users.size
        activeUsers = users.count { it.status == "ACTIVE" }
        suspendedUsers = users.count { it.status == "SUSPENDED" }
        totalAssessments = assessments.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UDYORA Administration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        AdminAuthService.logoutAdmin()
                        navController.navigate("home") { popUpTo("home") { inclusive = true } } 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to App")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val newRole = if (currentRole == AdminRole.CHIEF_ADMINISTRATOR.name) {
                                        AdminRole.EDITORIAL_CONTENT_OFFICER.name
                                    } else {
                                        AdminRole.CHIEF_ADMINISTRATOR.name
                                    }
                                    currentRole = newRole
                                    AdminAuthService.currentAdmin = AdminAuthService.currentAdmin.copy(role = newRole)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentRole == AdminRole.CHIEF_ADMINISTRATOR.name) "CHIEF ADMIN" else "EDITORIAL OFFICER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Role Banner Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (AdminAuthService.isChiefAdmin()) Icons.Default.Security else Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (AdminAuthService.isChiefAdmin()) "Chief Administrator Mode" else "Editorial Content Officer Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (AdminAuthService.isChiefAdmin()) "Full control over participants, assessments, reports, and system settings." else "Restricted access for managing schemes, evidence sources, and translations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Database KPIs 2x2 Grid (Responsive Fix for 360-412dp screen widths)
            Text("Database Key Indicators", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiMetricCard("Participants", totalUsers.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    KpiMetricCard("Active Users", activeUsers.toString(), Color(0xFF38A169), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiMetricCard("Suspended", suspendedUsers.toString(), Color(0xFFE53E3E), Modifier.weight(1f))
                    KpiMetricCard("Assessments", totalAssessments.toString(), MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
            }

            // Quick Actions Navigation
            Text("Management Sections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            
            if (AdminAuthService.isChiefAdmin()) {
                AdminNavCard("Participants & Reports", "Search, view, print reports, suspend or reactivate participants", Icons.Default.Group) {
                    navController.navigate("admin_participants")
                }
            }

            AdminNavCard("Government Schemes", "Manage official schemes, eligibility, funding limits, and versions", Icons.Default.AccountBalance) {
                navController.navigate("admin_schemes")
            }

            if (AdminAuthService.isChiefAdmin()) {
                AdminNavCard("Audit Log Stream", "View append-only security logs of all administrator actions", Icons.Default.History) {
                    navController.navigate("admin_audit")
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AdminNavCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
