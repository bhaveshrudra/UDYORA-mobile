package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.AssessmentEntity
import com.example.data.UserEntity
import com.example.services.AdminAuthService
import com.example.services.FinancialCalculator
import com.example.types.*
import com.example.utils.PdfReportGenerator
import com.example.utils.ReportSharingUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantManagementScreen(navController: NavController) {
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
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var usersList by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var userAssessments by remember { mutableStateOf<List<AssessmentEntity>>(emptyList()) }

    var showSuspendModal by remember { mutableStateOf(false) }
    var suspendReason by remember { mutableStateOf("") }

    fun refreshUsers() {
        scope.launch {
            val list = if (searchQuery.isBlank()) {
                db.userDao().getAllUsers().firstOrNull() ?: emptyList()
            } else {
                db.userDao().searchUsers(searchQuery).firstOrNull() ?: emptyList()
            }
            usersList = when (selectedStatusFilter) {
                "ACTIVE" -> list.filter { it.status == "ACTIVE" }
                "SUSPENDED" -> list.filter { it.status == "SUSPENDED" }
                "REMOVED" -> list.filter { it.status == "REMOVED" }
                else -> list
            }
        }
    }

    LaunchedEffect(searchQuery, selectedStatusFilter) {
        refreshUsers()
    }

    LaunchedEffect(selectedUser) {
        if (selectedUser != null) {
            userAssessments = db.assessmentDao().getAssessmentsForUser(selectedUser!!.id).firstOrNull() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Participant Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            // Search Bar & Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name, Mobile, Email...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "ACTIVE", "SUSPENDED", "REMOVED").forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedUser == null) {
                // Users List View
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (usersList.isEmpty()) {
                        item {
                            Text("No participants found matching criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    items(usersList) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUser = user },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Mobile: ${user.mobile} | Language: ${user.languageCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (user.status) {
                                        "ACTIVE" -> Color(0xFFC6F6D5)
                                        "SUSPENDED" -> Color(0xFFFED7D7)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ) {
                                    Text(
                                        text = user.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = when (user.status) {
                                            "ACTIVE" -> Color(0xFF22543D)
                                            "SUSPENDED" -> Color(0xFF742A2A)
                                            else -> Color(0xFF4A5568)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Selected User Profile View
                val u = selectedUser!!
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Participant Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { selectedUser = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Profile")
                            }
                        }
                        Text("Name: ${u.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Mobile: ${u.mobile} | Email: ${u.email ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                        Text("Status: ${u.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (u.status == "ACTIVE") {
                                Button(
                                    onClick = { showSuspendModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Suspend Participant")
                                }
                            } else if (u.status == "SUSPENDED") {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                AdminAuthService.reactivateParticipant(db, u.id)
                                                Toast.makeText(context, "Participant reactivated.", Toast.LENGTH_SHORT).show()
                                                selectedUser = u.copy(status = "ACTIVE")
                                                refreshUsers()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Text("Reactivate Participant")
                                }
                            }
                        }
                    }
                }

                Text("Assessment History (${userAssessments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(userAssessments) { asm ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("${asm.businessType} Proposal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Run ID: ${asm.id} | Score: ${asm.feasibilityScore}/100", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                val loc = CanonicalLocation(stateName = "Telangana", districtName = "Rangareddy", mandalName = "Shamshabad", pincode = "501218")
                                                val bus = BusinessSnapshot(type = BusinessTypeEnum.DAIRY, description = asm.description)
                                                val fin = FinancialCalculator.calculateFinancials(ownCapitalInput = asm.availableCapital, businessType = BusinessTypeEnum.DAIRY)
                                                val snapshot = AssessmentInputSnapshot(assessmentRunId = asm.id, userId = u.id, languageCode = u.languageCode, locationSnapshot = loc, businessSnapshot = bus, financialSnapshot = fin)
                                                val file = PdfReportGenerator.generatePdfReport(context, snapshot, u.name, null, asm.feasibilityScore, "EVALUATED")
                                                ReportSharingUtils.printPdfReport(context, file)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Print Report")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuspendModal && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showSuspendModal = false },
            title = { Text("Suspend Participant") },
            text = {
                Column {
                    Text("Are you sure you want to suspend ${selectedUser!!.name}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = suspendReason,
                        onValueChange = { suspendReason = it },
                        label = { Text("Reason for suspension *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (suspendReason.isNotBlank()) {
                            scope.launch {
                                try {
                                    AdminAuthService.suspendParticipant(db, selectedUser!!.id, suspendReason)
                                    Toast.makeText(context, "Participant suspended.", Toast.LENGTH_SHORT).show()
                                    selectedUser = selectedUser!!.copy(status = "SUSPENDED")
                                    showSuspendModal = false
                                    suspendReason = ""
                                    refreshUsers()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Suspension")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendModal = false }) { Text("Cancel") }
            }
        )
    }
}
