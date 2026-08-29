package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.stringResourceLoc
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun AssessmentScreen(navController: NavController) {
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var capital by remember { mutableStateOf("") }

    // Initial map position: India
    val indiaLatLng = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(indiaLatLng, 4f)
    }

    // A simple mock effect to zoom when inputs change
    LaunchedEffect(state, district, mandal, pincode) {
        if (pincode.isNotBlank()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(18.5204, 73.8567), 12f) // Mock exact
        } else if (district.isNotBlank()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(19.7515, 75.7139), 8f) // Mock district
        } else if (state.isNotBlank()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(19.7515, 75.7139), 6f) // Mock state
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResourceLoc("new_assessment"), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mandal, onValueChange = { mandal = it }, label = { Text("Mandal / Block") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = businessType, onValueChange = { businessType = it }, label = { Text("Business Type *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = capital, onValueChange = { capital = it }, label = { Text("Available Own Capital *") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    val bType = if (businessType.isBlank()) "Unknown" else businessType
                    val bDesc = if (description.isBlank()) "Unknown" else description
                    val bCap = if (capital.isBlank()) "0" else capital
                    navController.navigate("result/$bType/$bDesc/$bCap") 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Analyze Business")
            }
        }
    }
}
