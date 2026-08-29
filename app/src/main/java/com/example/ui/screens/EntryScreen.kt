package com.example.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.i18n.Language
import com.example.i18n.LanguageManager
import com.example.ui.components.StartupAnimation
import com.example.utils.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun EntryScreen(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Scaffold { _ ->
        StartupAnimation(
            onAnimationComplete = {
                scope.launch {
                    val savedUserId = UserPreferences.getUserId(context).firstOrNull()
                    val savedLangCode = UserPreferences.getPreferredLanguage(context).firstOrNull()

                    val existingUser = if (!savedUserId.isNullOrBlank()) {
                        db.userDao().getUserById(savedUserId)
                    } else {
                        db.userDao().getLatestUser()
                    }

                    if (existingUser != null) {
                        val langCode = existingUser.languageCode.ifBlank { savedLangCode ?: "en" }
                        LanguageManager.setLanguage(Language.fromCode(langCode))
                        viewModel.loadUser(existingUser)

                        val pastAssessments = db.assessmentDao().getAssessmentsForUser(existingUser.id).firstOrNull() ?: emptyList()
                        if (pastAssessments.isNotEmpty()) {
                            // User has completed assessments -> Show personalized Welcome Back screen
                            navController.navigate("welcome_back/${existingUser.name}") {
                                popUpTo("entry") { inclusive = true }
                            }
                        } else {
                            // User exists but has not completed any assessments -> Go straight to Workspace Home
                            navController.navigate("home") {
                                popUpTo("entry") { inclusive = true }
                            }
                        }
                    } else {
                        if (!savedLangCode.isNullOrBlank()) {
                            LanguageManager.setLanguage(Language.fromCode(savedLangCode))
                        }
                        navController.navigate("language") {
                            popUpTo("entry") { inclusive = true }
                        }
                    }
                }
            }
        )
    }
}
