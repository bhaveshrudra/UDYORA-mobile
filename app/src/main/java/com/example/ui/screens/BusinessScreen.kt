package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.LanguageManager
import com.example.i18n.stringResourceLoc
import com.example.types.*
import com.example.ui.components.TopBarWithProgress

@Composable
fun BusinessScreen(navController: NavController, viewModel: SharedViewModel) {
    val selectedCategory by viewModel.businessTypeEnum.collectAsState()
    val description by viewModel.description.collectAsState()
    
    val dairyInputs by viewModel.dairyInputs.collectAsState()
    val tailoringInputs by viewModel.tailoringInputs.collectAsState()
    val kiranaInputs by viewModel.kiranaInputs.collectAsState()
    val poultryInputs by viewModel.poultryInputs.collectAsState()

    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.updateDescription(if (description.isBlank()) spokenText else "$description $spokenText")
            }
        }
    }

    val isDescriptionValid = description.trim().length >= 5

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBarWithProgress(navController, currentStep = 4, totalSteps = 6, showBack = true)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResourceLoc("what_business"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Select your micro-enterprise category and key operational parameters",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Category Chips Selection
                Text("Select Category *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BusinessTypeEnum.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectBusinessType(cat) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Text("✓ Selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Business Description Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Business Description *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, LanguageManager.currentLanguage.value.code)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your business idea...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Speech recognition unavailable on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Describe your proposed business activity...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    isError = description.isNotBlank() && !isDescriptionValid
                )
                if (description.isNotBlank() && !isDescriptionValid) {
                    Text("Please enter at least 5 characters", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // DYNAMIC BUSINESS-SPECIFIC INPUT FORMS
                Text("Operational Inputs (${selectedCategory.displayName})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                when (selectedCategory) {
                    BusinessTypeEnum.DAIRY -> {
                        OutlinedTextField(
                            value = dairyInputs.numberOfAnimals.toString(),
                            onValueChange = { viewModel.updateDairyInputs(dairyInputs.copy(numberOfAnimals = it.toIntOrNull() ?: 1)) },
                            label = { Text("Number of Animals (Cows/Buffaloes) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = dairyInputs.milkYieldPerAnimal.toString(),
                            onValueChange = { viewModel.updateDairyInputs(dairyInputs.copy(milkYieldPerAnimal = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Milk Yield per Animal (Liters/Day) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = dairyInputs.sellingPricePerLiter.toString(),
                            onValueChange = { viewModel.updateDairyInputs(dairyInputs.copy(sellingPricePerLiter = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Selling Price per Liter (₹) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    BusinessTypeEnum.TAILORING -> {
                        OutlinedTextField(
                            value = tailoringInputs.numberOfMachines.toString(),
                            onValueChange = { viewModel.updateTailoringInputs(tailoringInputs.copy(numberOfMachines = it.toIntOrNull() ?: 1)) },
                            label = { Text("Number of Sewing Machines *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = tailoringInputs.expectedMonthlyOrders.toString(),
                            onValueChange = { viewModel.updateTailoringInputs(tailoringInputs.copy(expectedMonthlyOrders = it.toIntOrNull() ?: 0)) },
                            label = { Text("Expected Monthly Orders *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = tailoringInputs.avgOrderValue.toString(),
                            onValueChange = { viewModel.updateTailoringInputs(tailoringInputs.copy(avgOrderValue = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Average Order Value (₹) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    BusinessTypeEnum.KIRANA -> {
                        OutlinedTextField(
                            value = kiranaInputs.initialInventoryCost.toString(),
                            onValueChange = { viewModel.updateKiranaInputs(kiranaInputs.copy(initialInventoryCost = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Initial Stock / Inventory Investment (₹) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = kiranaInputs.expectedCustomersPerDay.toString(),
                            onValueChange = { viewModel.updateKiranaInputs(kiranaInputs.copy(expectedCustomersPerDay = it.toIntOrNull() ?: 0)) },
                            label = { Text("Expected Footfall (Customers / Day) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = kiranaInputs.avgBasketValue.toString(),
                            onValueChange = { viewModel.updateKiranaInputs(kiranaInputs.copy(avgBasketValue = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Average Basket / Purchase Value (₹) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    BusinessTypeEnum.POULTRY_AGRO -> {
                        OutlinedTextField(
                            value = poultryInputs.birdCapacity.toString(),
                            onValueChange = { viewModel.updatePoultryInputs(poultryInputs.copy(birdCapacity = it.toIntOrNull() ?: 0)) },
                            label = { Text("Bird Capacity per Batch *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = poultryInputs.expectedPricePerBird.toString(),
                            onValueChange = { viewModel.updatePoultryInputs(poultryInputs.copy(expectedPricePerBird = it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Expected Price per Bird (₹) *") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { navController.navigate("capital") { launchSingleTop = true } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    enabled = isDescriptionValid
                ) {
                    Text(stringResourceLoc("continue"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
