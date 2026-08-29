package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.i18n.Language
import com.example.i18n.LanguageManager
import com.example.i18n.stringResourceLoc
import com.example.ui.theme.ResponsiveLayout
import com.example.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val horizontalPadding = ResponsiveLayout.horizontalPadding()

    LaunchedEffect(Unit) {
        UserPreferences.getPreferredLanguage(context).collect { code ->
            if (!code.isNullOrBlank()) {
                LanguageManager.setLanguage(Language.fromCode(code))
            }
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
                .padding(horizontal = horizontalPadding, vertical = 16.dp)
        ) {
            Text(
                text = stringResourceLoc("choose_language"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            Text(
                text = stringResourceLoc("select_language_desc"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(Language.values()) { lang ->
                    val isSelected = currentLanguage == lang
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                LanguageManager.setLanguage(lang)
                                scope.launch {
                                    UserPreferences.setPreferredLanguage(context, lang.code)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.Transparent, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(lang) {
                                            Language.ENGLISH -> "EN"
                                            Language.HINDI -> "हिं"
                                            Language.MARATHI -> "म"
                                            Language.TELUGU -> "తె"
                                            Language.KANNADA -> "ಕ"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = lang.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("profile") { launchSingleTop = true } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(stringResourceLoc("continue"), fontWeight = FontWeight.Bold)
            }
        }
    }
}
