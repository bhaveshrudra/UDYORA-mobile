package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.stringResourceLoc
import com.example.ui.components.TopBarWithProgress

@Composable
fun CapitalScreen(navController: NavController, viewModel: SharedViewModel) {
    val capital by viewModel.capital.collectAsState()
    val financials by viewModel.financialOutput.collectAsState()

    val ownCapVal = capital.toDoubleOrNull() ?: 0.0
    val isValidCapital = ownCapVal > 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBarWithProgress(navController, currentStep = 5, totalSteps = 6, showBack = true)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResourceLoc("investment_amount"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Specify your available equity contribution to compute estimated project financing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Own Capital Input
                OutlinedTextField(
                    value = capital,
                    onValueChange = { viewModel.updateCapital(it) },
                    label = { Text("Available Own Capital (₹) *") },
                    placeholder = { Text("e.g. 100000") },
                    leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = capital.isNotBlank() && !isValidCapital
                )

                if (isValidCapital) {
                    Text(
                        text = "Calculated Financing & Debt Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Financial Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FinSummaryRow("Estimated Project Cost:", "₹${String.format("%,.0f", financials.projectCost)}")
                            FinSummaryRow("Own Equity Contribution:", "₹${String.format("%,.0f", financials.ownCapital)} (${String.format("%.1f", financials.equityRatio * 100)}%)")
                            FinSummaryRow("Derived Loan Amount:", "₹${String.format("%,.0f", financials.loanAmount)} (${String.format("%.1f", financials.debtRatio * 100)}%)")
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            FinSummaryRow("Estimated Monthly EMI (9.5% p.a.):", "₹${String.format("%,.0f", financials.monthlyEmi)}")
                            FinSummaryRow("Debt Service Coverage (DSCR):", financials.dscr?.let { String.format("%.2f", it) } ?: "INSUFFICIENT DATA")
                            FinSummaryRow("Promoter Margin Status:", financials.marginStatus)
                            FinSummaryRow("Financial Health Rating:", financials.financialStatus)
                        }
                    }

                    if (financials.operatingSurplusMonthly != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Monthly Economics Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                FinSummaryRow("Est. Monthly Revenue:", "₹${String.format("%,.0f", financials.revenueMonthly ?: 0.0)}")
                                FinSummaryRow("Est. Monthly Operating Cost:", "₹${String.format("%,.0f", financials.operatingCostMonthly ?: 0.0)}")
                                FinSummaryRow("Est. Monthly Operating Surplus:", "₹${String.format("%,.0f", financials.operatingSurplusMonthly ?: 0.0)}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { navController.navigate("review") { launchSingleTop = true } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    enabled = isValidCapital
                ) {
                    Text(stringResourceLoc("continue"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FinSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
