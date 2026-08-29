package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.types.FinancialOutput

@Composable
fun FeasibilityFactorBarChart(
    locationScore: Int,
    marketScore: Int,
    financeScore: Int,
    infraScore: Int = 70,
    competitionScore: Int = 65,
    riskScore: Int = 80
) {
    val factors = listOf(
        "Location Suitability" to locationScore,
        "Market Demand" to marketScore,
        "Financial Viability" to financeScore,
        "Infrastructure Access" to infraScore,
        "Competition Index" to competitionScore,
        "Risk Factor Score" to riskScore
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        factors.forEach { (label, score) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$score/100", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (score / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        score >= 75 -> MaterialTheme.colorScheme.primary
                        score >= 55 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun CapitalStructureChart(financials: FinancialOutput) {
    val equityPct = (financials.equityRatio * 100).toFloat()
    val debtPct = (financials.debtRatio * 100).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Capital Structure Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart Canvas
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val sweepEquity = (equityPct / 100f) * 360f
                        val sweepDebt = (debtPct / 100f) * 360f

                        drawArc(
                            color = Color(0xFF2B6CB0),
                            startAngle = -90f,
                            sweepAngle = sweepEquity,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        drawArc(
                            color = Color(0xFFE53E3E),
                            startAngle = -90f + sweepEquity,
                            sweepAngle = sweepDebt,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF2B6CB0), RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Own Equity: ${String.format("%.1f", equityPct)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFFE53E3E), RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Debt Loan: ${String.format("%.1f", debtPct)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialComparisonChart(financials: FinancialOutput) {
    val rev = financials.revenueMonthly ?: 0.0
    val opex = financials.operatingCostMonthly ?: 0.0
    val surplus = financials.operatingSurplusMonthly ?: 0.0

    if (rev <= 0.0) {
        Text("Insufficient data for financial comparison.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val maxVal = rev.coerceAtLeast(1.0)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Est. Monthly Economics Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Gross Revenue", style = MaterialTheme.typography.bodySmall)
            Text("₹${String.format("%,.0f", rev)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (rev / maxVal).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF319795)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Operating Cost", style = MaterialTheme.typography.bodySmall)
            Text("₹${String.format("%,.0f", opex)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (opex / maxVal).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFDD6B20)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Operating Surplus", style = MaterialTheme.typography.bodySmall)
            Text("₹${String.format("%,.0f", surplus)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (surplus / maxVal).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF3182CE)
        )
    }
}
