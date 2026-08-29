package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.AppDatabase
import com.example.data.UserEntity
import com.example.i18n.LanguageManager
import com.example.i18n.stringResourceLoc
import com.example.ui.components.TopBarWithProgress
import com.example.utils.UserPreferences
import com.example.utils.ValidationUtils
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, viewModel: SharedViewModel) {
    val name by viewModel.name.collectAsState()
    val mobile by viewModel.mobile.collectAsState()
    val email by viewModel.email.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var duplicateMobileError by remember { mutableStateOf<String?>(null) }
    var duplicateEmailError by remember { mutableStateOf<String?>(null) }
    var existingUserFound by remember { mutableStateOf<UserEntity?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    val isNameValid = ValidationUtils.isValidName(name)
    val isMobileValid = ValidationUtils.isValidMobile(mobile)
    val isEmailValid = ValidationUtils.isValidEmail(email)

    val isFormValid = isNameValid && isMobileValid && isEmailValid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBarWithProgress(navController, currentStep = 1, totalSteps = 6, showBack = true)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResourceLoc("tell_us_about"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = stringResourceLoc("few_details"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
                )

                // FULL NAME *
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        viewModel.updateName(it)
                        duplicateMobileError = null
                        duplicateEmailError = null
                        existingUserFound = null
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Enter your full name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = name.isNotBlank() && !isNameValid
                )
                if (name.isNotBlank() && !isNameValid) {
                    Text(
                        text = "Name must be between 2 and 50 characters",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // MOBILE NUMBER *
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { 
                        viewModel.updateMobile(it)
                        duplicateMobileError = null
                        duplicateEmailError = null
                        existingUserFound = null
                    },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("Enter 10-digit mobile number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = (mobile.isNotBlank() && !isMobileValid) || duplicateMobileError != null
                )
                if (mobile.isNotBlank() && !isMobileValid) {
                    Text(
                        text = "Enter a valid 10-digit Indian mobile number (e.g. 9876543210)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                if (duplicateMobileError != null) {
                    Text(
                        text = duplicateMobileError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // EMAIL ADDRESS (OPTIONAL)
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        viewModel.updateEmail(it)
                        duplicateMobileError = null
                        duplicateEmailError = null
                        existingUserFound = null
                    },
                    label = { Text("Email Address (Optional)") },
                    placeholder = { Text("Enter your email address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = (email.isNotBlank() && !isEmailValid) || duplicateEmailError != null
                )
                if (email.isNotBlank() && !isEmailValid) {
                    Text(
                        text = "Please enter a valid email address",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                if (duplicateEmailError != null) {
                    Text(
                        text = duplicateEmailError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (existingUserFound != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Account Found: ${existingUserFound!!.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You can log in and continue with your existing profile.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val usr = existingUserFound!!
                                    scope.launch {
                                        UserPreferences.setUserId(context, usr.id)
                                        UserPreferences.setPreferredLanguage(context, usr.languageCode)
                                        UserPreferences.setFirstLaunchCompleted(context, true)
                                        viewModel.loadUser(usr)
                                        navController.navigate("welcome_back/${usr.name}") {
                                            popUpTo("entry") { inclusive = true }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue as ${existingUserFound!!.name}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "We never share your personal information with anyone.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { 
                        if (isFormValid && !isChecking) {
                            isChecking = true
                            scope.launch {
                                val canonicalMobile = ValidationUtils.normalizeMobile(mobile)
                                val canonicalEmail = ValidationUtils.normalizeEmail(email)

                                val existingMobileUser = db.userDao().getUserByMobile(canonicalMobile)
                                if (existingMobileUser != null) {
                                    duplicateMobileError = "This mobile number is already registered."
                                    existingUserFound = existingMobileUser
                                    isChecking = false
                                    return@launch
                                }

                                if (canonicalEmail != null) {
                                    val existingEmailUser = db.userDao().getUserByEmail(canonicalEmail)
                                    if (existingEmailUser != null) {
                                        duplicateEmailError = "This email address is already registered."
                                        existingUserFound = existingEmailUser
                                        isChecking = false
                                        return@launch
                                    }
                                }

                                val userId = "usr_" + canonicalMobile
                                val currentLang = LanguageManager.currentLanguage.value.code
                                val userEntity = UserEntity(
                                    id = userId,
                                    name = name.trim(),
                                    mobile = canonicalMobile,
                                    email = canonicalEmail,
                                    languageCode = currentLang,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    status = "ACTIVE"
                                )

                                try {
                                    db.userDao().insertUser(userEntity)
                                    UserPreferences.setUserId(context, userId)
                                    UserPreferences.setPreferredLanguage(context, currentLang)
                                    UserPreferences.setFirstLaunchCompleted(context, true)
                                    viewModel.loadUser(userEntity)

                                    navController.navigate("transparency") { launchSingleTop = true }
                                } catch (e: Exception) {
                                    duplicateMobileError = "Error saving profile. Please try again."
                                } finally {
                                    isChecking = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(100.dp),
                    enabled = isFormValid && !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResourceLoc("continue"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
