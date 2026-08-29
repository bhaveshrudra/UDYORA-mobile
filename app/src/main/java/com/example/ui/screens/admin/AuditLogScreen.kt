package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.services.AdminAuthService
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(navController: NavController) {
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
    var logs by remember { mutableStateOf<List<AuditLogEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        logs = db.auditLogDao().getAllAuditLogs().firstOrNull() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Append-Only Audit Log Stream", fontWeight = FontWeight.Bold) },
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (logs.isEmpty()) {
                    item {
                        Text("No audit log entries recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                items(logs) { log ->
                    val dateStr = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.action, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Admin: ${log.adminUserId} (${log.role})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Target: ${log.targetType} [${log.targetId}] | Reason: ${log.reason.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
