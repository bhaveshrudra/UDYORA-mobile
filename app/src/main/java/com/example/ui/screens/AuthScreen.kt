package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.i18n.stringResourceLoc
import com.example.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    
    fun authenticateWithBiometrics() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    scope.launch {
                        UserPreferences.setUserId(context, email.ifBlank { "user_default" })
                        navController.navigate("home") {
                            popUpTo("entry") { inclusive = true }
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })
            
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("UDYORA Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use Account Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
    
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResourceLoc("login"),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResourceLoc("email")) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { authenticateWithBiometrics() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResourceLoc("continue"))
            }
        }
    }
}
