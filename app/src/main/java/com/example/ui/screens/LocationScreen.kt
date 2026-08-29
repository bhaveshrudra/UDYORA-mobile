package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.i18n.stringResourceLoc
import com.example.services.LocationHierarchyService
import com.example.services.MandalNode
import com.example.types.GpsState
import com.example.types.LocationSource
import com.example.ui.components.TopBarWithProgress
import com.example.ui.theme.ResponsiveLayout
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val gpsState by viewModel.gpsState.collectAsState()
    val locationError by viewModel.locationError.collectAsState()

    var showStateDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }
    var showMandalDialog by remember { mutableStateOf(false) }

    val horizontalPadding = ResponsiveLayout.horizontalPadding()
    val mapHeight = ResponsiveLayout.responsiveMapHeight()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.requestHardwareGpsLocation(context)
        } else {
            viewModel.setGpsState(GpsState.DENIED)
            Toast.makeText(context, "Location permission denied. Please select location manually.", Toast.LENGTH_LONG).show()
        }
    }

    fun startGpsFlow() {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
            viewModel.requestHardwareGpsLocation(context)
        } else {
            viewModel.setGpsState(GpsState.REQUESTING_PERMISSION)
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val indiaLatLng = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(indiaLatLng, 4f)
    }

    LaunchedEffect(currentLocation.latitude, currentLocation.longitude, currentLocation.districtName, currentLocation.stateName) {
        val lat = currentLocation.latitude
        val lng = currentLocation.longitude
        if (lat != null && lng != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 13f)
        } else if (!currentLocation.districtName.isNullOrBlank()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(19.7515, 75.7139), 8f)
        } else if (!currentLocation.stateName.isNullOrBlank()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(19.7515, 75.7139), 6f)
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(indiaLatLng, 4f)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopBarWithProgress(navController, currentStep = 3, totalSteps = 6, showBack = true)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 12.dp)
            ) {
                Text(
                    text = stringResourceLoc("where_located"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Provide your exact hierarchical business location in India",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { startGpsFlow() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentLocation.source == LocationSource.GPS) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        )
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Live GPS", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.useDemoLocation() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentLocation.isDemo) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        )
                    ) {
                        Text("Demo Scenario", fontWeight = FontWeight.SemiBold)
                    }
                }

                // GPS Loading / Status Indicator
                if (gpsState == GpsState.LOCATING || gpsState == GpsState.RESOLVING) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (gpsState == GpsState.LOCATING) "Acquiring hardware GPS signal..." else "Reverse geocoding location details...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (gpsState == GpsState.DENIED) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Location permission denied. Please select your location manually below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (locationError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = locationError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { startGpsFlow() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Hardware GPS")
                            }
                        }
                    }
                }

                // STEP 1: State Selection
                Text("State *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable { showStateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLocation.stateName.takeIf { !it.isNullOrBlank() } ?: "Search or select state...",
                            color = if (currentLocation.stateName.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (currentLocation.stateName.isNullOrBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // STEP 2: District Selection
                val isStateSelected = !currentLocation.stateName.isNullOrBlank()
                Text("District *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (!isStateSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable(enabled = isStateSelected) { showDistrictDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStateSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLocation.districtName.takeIf { !it.isNullOrBlank() } ?: (if (!isStateSelected) "Select state first" else "Search district..."),
                            color = if (currentLocation.districtName.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (currentLocation.districtName.isNullOrBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }

                // STEP 3: Mandal / Block Selection
                val isDistrictSelected = !currentLocation.districtName.isNullOrBlank()
                Text("Mandal / Block *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (!isDistrictSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable(enabled = isDistrictSelected) { showMandalDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDistrictSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLocation.mandalName.takeIf { !it.isNullOrBlank() } ?: (if (!isDistrictSelected) "Select district first" else "Search mandal/block..."),
                            color = if (currentLocation.mandalName.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (currentLocation.mandalName.isNullOrBlank()) FontWeight.Normal else FontWeight.Medium
                        )
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }

                // STEP 4: Pincode
                val isMandalSelected = !currentLocation.mandalName.isNullOrBlank()
                Text("Pincode *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (!isMandalSelected && !isStateSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = currentLocation.pincode ?: "",
                    onValueChange = { if (it.length <= 6) viewModel.updatePincode(it) },
                    placeholder = { Text("Enter 6-digit pincode") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = isMandalSelected || isStateSelected
                )

                // LOCATION DETECTED / CONFIRMATION CARD
                if (!currentLocation.mandalName.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LOCATION DETECTED",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (currentLocation.source) {
                                        LocationSource.GPS -> MaterialTheme.colorScheme.primaryContainer
                                        LocationSource.DEMO -> MaterialTheme.colorScheme.tertiaryContainer
                                        LocationSource.PINCODE -> MaterialTheme.colorScheme.secondaryContainer
                                        LocationSource.MANUAL -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = when (currentLocation.source) {
                                            LocationSource.GPS -> "Detected by GPS"
                                            LocationSource.DEMO -> "Demo Scenario"
                                            LocationSource.PINCODE -> "Resolved from Pincode"
                                            LocationSource.MANUAL -> "Selected Manually"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("State: ${currentLocation.stateName ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                            Text("District: ${currentLocation.districtName ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Mandal: ${currentLocation.mandalName ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Pincode: ${currentLocation.pincode ?: "-"}", style = MaterialTheme.typography.bodyMedium)

                            if (currentLocation.latitude != null && currentLocation.longitude != null) {
                                Text(
                                    text = "Coordinates: ${String.format("%.4f", currentLocation.latitude)}, ${String.format("%.4f", currentLocation.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentLocation.accuracyMeters != null && currentLocation.source == LocationSource.GPS) {
                                Text(
                                    text = "GPS Accuracy: ±${currentLocation.accuracyMeters?.toInt()} m",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.confirmLocation() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Use This Location")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearLocation() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Choose Another")
                                }
                            }
                        }
                    }
                }

                // Dynamic Aspect-Ratio Map View (Requirement 12)
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        if (currentLocation.latitude != null && currentLocation.longitude != null) {
                            Marker(
                                state = MarkerState(position = LatLng(currentLocation.latitude!!, currentLocation.longitude!!)),
                                title = currentLocation.mandalName ?: "Selected Location",
                                snippet = "${currentLocation.districtName}, ${currentLocation.stateName}"
                            )
                        }
                    }
                }
            }
            
            Box(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp)) {
                Button(
                    onClick = { navController.navigate("business") { launchSingleTop = true } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    enabled = currentLocation.isVerified
                ) {
                    Text(stringResourceLoc("continue"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // STATE SELECTOR DIALOG
    if (showStateDialog) {
        SearchableSelectionDialog(
            title = "Select State",
            items = LocationHierarchyService.states.map { it.stateId to it.stateName },
            onDismiss = { showStateDialog = false },
            onSelect = { id, name ->
                viewModel.selectState(id, name)
                showStateDialog = false
            }
        )
    }

    // DISTRICT SELECTOR DIALOG
    if (showDistrictDialog && !currentLocation.stateId.isNullOrBlank()) {
        val districts = LocationHierarchyService.searchDistricts(currentLocation.stateId!!, "")
        SearchableSelectionDialog(
            title = "Select District (${currentLocation.stateName})",
            items = districts.map { it.districtId to it.districtName },
            onDismiss = { showDistrictDialog = false },
            onSelect = { id, name ->
                viewModel.selectDistrict(id, name)
                showDistrictDialog = false
            }
        )
    }

    // MANDAL SELECTOR DIALOG
    if (showMandalDialog && !currentLocation.stateId.isNullOrBlank() && !currentLocation.districtId.isNullOrBlank()) {
        val mandals = LocationHierarchyService.searchMandals(currentLocation.stateId!!, currentLocation.districtId!!, "")
        SearchableSelectionDialog(
            title = "Select Mandal / Block (${currentLocation.districtName})",
            items = mandals.map { Triple(it.mandalId, it.mandalName, it) },
            onDismiss = { showMandalDialog = false },
            onSelectCustom = { item ->
                val mandal = item as MandalNode
                viewModel.selectMandal(mandal.mandalId, mandal.mandalName, mandal.lat, mandal.lng, mandal.locality)
                showMandalDialog = false
            }
        )
    }
}

@Composable
fun SearchableSelectionDialog(
    title: String,
    items: List<Any>,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit = { _, _ -> },
    onSelectCustom: (Any) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                val name = when (item) {
                    is Pair<*, *> -> item.second.toString()
                    is Triple<*, *, *> -> item.second.toString()
                    is MandalNode -> item.mandalName
                    else -> item.toString()
                }
                name.lowercase().contains(searchQuery.lowercase().trim())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
                )
            }
        },
        text = {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching location found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems) { item ->
                            val (id, name) = when (item) {
                                is Pair<*, *> -> item.first.toString() to item.second.toString()
                                is Triple<*, *, *> -> item.first.toString() to item.second.toString()
                                is MandalNode -> item.mandalId to item.mandalName
                                else -> "" to item.toString()
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item is MandalNode) {
                                            onSelectCustom(item)
                                        } else {
                                            onSelect(id, name)
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = name,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
