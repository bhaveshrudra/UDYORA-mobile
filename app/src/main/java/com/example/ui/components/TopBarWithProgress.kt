package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TopBarWithProgress(
    navController: NavController,
    currentStep: Int,
    totalSteps: Int = 6,
    showBack: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // Progress dots
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentStep) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= currentStep) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                    if (i < totalSteps) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Text(
            text = "Step $currentStep of $totalSteps",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = if (showBack) 48.dp else 0.dp)
        )
    }
}
