package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun StartupAnimation(onAnimationComplete: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alphaText = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        alphaText.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        delay(600) 
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkNavy)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale.value)
                    .background(
                        color = com.example.ui.theme.DarkNavy,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(4.dp)
                    .background(com.example.ui.theme.DarkNavy, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "UDYORA",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaText.value)
            )
            Text(
                text = "Mobile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.alpha(alphaText.value)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "AI-Powered Business Advisory\nfor Rural Entrepreneurs",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.alpha(alphaText.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .alpha(alphaText.value),
                color = Color.White.copy(alpha = 0.9f),
                strokeWidth = 2.5.dp
            )
        }
    }
}
