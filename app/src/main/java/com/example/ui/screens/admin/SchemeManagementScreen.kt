package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.GovernmentSchemeEntity
import com.example.services.AdminAuthService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeManagementScreen(navController: NavController) {
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

    var schemes by remember { mutableStateOf<List<GovernmentSchemeEntity>>(emptyList()) }
    var selectedScheme by remember { mutableStateOf<GovernmentSchemeEntity?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Editor Form Fields
    var schemeName by remember { mutableStateOf("") }
    var nodalAgency by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf("") }
    var officialSource by remember { mutableStateOf("") }

    fun refreshSchemes() {
        scope.launch {
            isLoading = true
            var list = db.governmentSchemeDao().getAllSchemes().firstOrNull() ?: emptyList()
            if (list.isEmpty()) {
                // Initial Seeding of standard Indian government schemes
                val seedSchemes = listOf(
                    GovernmentSchemeEntity(
                        id = "SCHEME-001",
                        name = "National Livestock Mission (NLM)",
                        nodalAgency = "Department of Animal Husbandry & Dairying (DAHD)",
                        sector = "Agriculture / Animal Husbandry",
                        description = "Capital subsidy scheme for establishing rural dairy cattle breeding & fodder units.",
                        eligibility = "Farmers, Rural Entrepreneurs, Cooperatives with land or lease proof",
                        eligibleBusinesses = "Dairy Farming, Poultry & Agro",
                        benefit = "25% to 33.33% Capital Subsidy up to ₹50 Lakhs",
                        requiredDocuments = "Aadhaar Card, Land Records, Bank Account Passbook",
                        officialSource = "https://nlm.udyamimitra.in",
                        version = 1,
                        status = "PUBLISHED"
                    ),
                    GovernmentSchemeEntity(
                        id = "SCHEME-002",
                        name = "PM Vishwakarma Scheme",
                        nodalAgency = "Ministry of Micro, Small and Medium Enterprises",
                        sector = "MSME / Artisans",
                        description = "Holistic support scheme for traditional artisans & tailors providing skill training, toolkit incentive, and collateral-free loan.",
                        eligibility = "Artisans, Tailors, Weavers engaged in traditional trade",
                        eligibleBusinesses = "Tailoring Unit, Handicrafts",
                        benefit = "₹3 Lakh Enterprise Loan @ 5% interest + ₹15,000 Toolkit E-Voucher",
                        requiredDocuments = "Skill Verification Certificate, Aadhaar, Bank Details",
                        officialSource = "https://pmvishwakarma.gov.in",
                        version = 1,
                        status = "PUBLISHED"
                    ),
                    GovernmentSchemeEntity(
                        id = "SCHEME-003",
                        name = "PM Mudra Yojana (PMMY)",
                        nodalAgency = "Department of Financial Services, Ministry of Finance",
                        sector = "Retail / Micro Business",
                        description = "Collateral-free working capital loan for micro-enterprises and Kirana retail outlets under Shishu, Kishor, and Tarun categories.",
                        eligibility = "Non-corporate, non-farm small/micro enterprises",
                        eligibleBusinesses = "Kirana Retail, Tailoring Unit",
                        benefit = "Collateral-free loan up to ₹10 Lakhs",
                        requiredDocuments = "Business License / Udyam Registration, PAN Card, Identity Proof",
                        officialSource = "https://www.mudra.org.in",
                        version = 1,
                        status = "PUBLISHED"
                    )
                )
                seedSchemes.forEach { db.governmentSchemeDao().insertScheme(it) }
                list = seedSchemes
            }
            schemes = list
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshSchemes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Government Scheme Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedScheme = null
                            schemeName = ""
                            nodalAgency = ""
                            sector = ""
                            description = ""
                            eligibility = ""
                            officialSource = ""
                            isEditing = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Scheme")
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading government scheme records...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (!isEditing) {
                if (schemes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No government scheme records available yet.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedScheme = null
                                    schemeName = ""
                                    nodalAgency = ""
                                    sector = ""
                                    description = ""
                                    eligibility = ""
                                    officialSource = ""
                                    isEditing = true
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Government Scheme")
                            }
                        }
                    }
                } else {
                    // Schemes List
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(schemes) { sch ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(sch.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                            Text("v${sch.version} | ${sch.status}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Nodal Agency: ${sch.nodalAgency} | Sector: ${sch.sector}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Official Source: ${sch.officialSource.ifBlank { "Unspecified" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                selectedScheme = sch
                                                schemeName = sch.name
                                                nodalAgency = sch.nodalAgency
                                                sector = sch.sector
                                                description = sch.description
                                                eligibility = sch.eligibility
                                                officialSource = sch.officialSource
                                                isEditing = true
                                            }
                                        ) {
                                            Text("Edit Scheme")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Scheme Editor Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(if (selectedScheme == null) "Create New Scheme" else "Edit Scheme (v${selectedScheme!!.version})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = schemeName, onValueChange = { schemeName = it }, label = { Text("Scheme Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nodalAgency, onValueChange = { nodalAgency = it }, label = { Text("Nodal Agency / Ministry *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sector, onValueChange = { sector = it }, label = { Text("Sector (Agriculture / MSME) *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = eligibility, onValueChange = { eligibility = it }, label = { Text("Eligibility Conditions") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = officialSource, onValueChange = { officialSource = it }, label = { Text("Official Source / Reference URL *") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val sId = selectedScheme?.id ?: ("SCHEME-" + System.currentTimeMillis())
                                val sVer = (selectedScheme?.version ?: 0) + 1
                                val entity = GovernmentSchemeEntity(
                                    id = sId,
                                    name = schemeName,
                                    nodalAgency = nodalAgency,
                                    sector = sector,
                                    description = description,
                                    eligibility = eligibility,
                                    eligibleBusinesses = "Dairy, Tailoring, Kirana, Poultry",
                                    benefit = "Subsidies & Credit Guarantees",
                                    requiredDocuments = "Udyam Certificate, Aadhaar, Bank Statement",
                                    officialSource = officialSource,
                                    version = sVer,
                                    status = "PUBLISHED"
                                )
                                scope.launch {
                                    try {
                                        AdminAuthService.publishGovernmentScheme(db, entity)
                                        Toast.makeText(context, "Scheme published successfully.", Toast.LENGTH_SHORT).show()
                                        isEditing = false
                                        refreshSchemes()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publish Scheme")
                        }

                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
