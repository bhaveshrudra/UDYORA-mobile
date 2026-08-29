package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.i18n.LanguageManager
import com.example.services.SpeechSessionState
import com.example.services.TextToSpeechService
import com.example.ui.theme.ResponsiveLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(navController: NavController, viewModel: SharedViewModel) {
    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isChatbotThinking.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val horizontalPadding = ResponsiveLayout.horizontalPadding()

    var input by remember { mutableStateOf("") }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startSpeechInput(context) { text -> input = text }
        } else {
            Toast.makeText(context, "Microphone permission required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        TextToSpeechService.init(context) {}
        onDispose {
            TextToSpeechService.stop()
        }
    }

    val sampleQuestions = listOf(
        "What is UDYORA?",
        "Why is my feasibility score calculated?",
        "How did you calculate EMI?",
        "What government scheme can I use?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UDYORA Business Advisor AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = horizontalPadding)
        ) {
            // Chat Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ask Me Anything About Your Business Proposal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("I can explain your feasibility score, location suitability, EMI math, risks, and government schemes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Suggested Questions:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                sampleQuestions.forEach { q ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable { viewModel.sendChatMessage(q) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(q, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                items(chatMessages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp
                                )
                                if (!isUser) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                TextToSpeechService.speak(
                                                    text = msg.text,
                                                    language = LanguageManager.currentLanguage.value,
                                                    onVoiceUnavailable = {
                                                        Toast.makeText(context, "Voice output for this language is unavailable on this device.", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Listen", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Speech State Banner
            if (speechState == SpeechSessionState.LISTENING) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Listening in ${LanguageManager.currentLanguage.value.displayName}...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.stopSpeechInput() }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Input Bar with IME padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask your advisor...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (speechState == SpeechSessionState.LISTENING) {
                            viewModel.stopSpeechInput()
                        } else {
                            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startSpeechInput(context) { text -> input = text }
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (speechState == SpeechSessionState.LISTENING) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (speechState == SpeechSessionState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (speechState == SpeechSessionState.LISTENING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            viewModel.sendChatMessage(input)
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    enabled = input.isNotBlank() && !isThinking
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
