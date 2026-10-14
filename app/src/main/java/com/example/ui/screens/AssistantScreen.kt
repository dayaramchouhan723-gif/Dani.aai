package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AssistantPersona
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.ui.components.AnimatedVoiceOrb
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.MyraVioletPrimary
import com.example.ui.theme.MyraVioletSecondary
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onNavigateToSkills: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToScanOcr: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    val state by viewModel.assistantState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val rmsLevel by viewModel.liveRmsDb.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val persona by viewModel.preferences.persona.collectAsState()
    val continuousMode by viewModel.preferences.continuousMode.collectAsState()
    val isVipUnlocked by viewModel.preferences.isVipUnlocked.collectAsState()

    var showTextInputDialog by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }
    var isChatHistoryExpanded by remember { mutableStateOf(false) }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Microphone permission is required for voice commands.")
            }
        }
    }

    val onMicClicked = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val quickCommands = listOf(
        "Check battery",
        "Volume badhao",
        "Torch on karo",
        "WhatsApp kholo",
        "Play music on YouTube",
        "Open camera",
        "Take a note: Meeting tomorrow",
        "Set a 5-minute timer",
        "Tell me a joke"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (state) {
                                        AssistantState.LISTENING -> Color(0xFFFF1744)
                                        AssistantState.THINKING -> Color(0xFF00E5FF)
                                        AssistantState.SPEAKING -> Color(0xFFE040FB)
                                        else -> if (persona == AssistantPersona.GF_MODE) Color(0xFFFF4081) else MyraVioletPrimary
                                    }
                                )
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Dani AI",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                if (isVipUnlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                                    ) {
                                        Text(
                                            text = "VIP PRO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${persona.title} • v2.1.0 Free",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToScanOcr, modifier = Modifier.testTag("nav_scan_ocr")) {
                        Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = "Scan OCR", tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = onNavigateToSkills, modifier = Modifier.testTag("skills_button")) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Skills", tint = MyraVioletSecondary)
                    }
                    IconButton(onClick = onNavigateToNotes, modifier = Modifier.testTag("notes_button")) {
                        Icon(imageVector = Icons.Default.NoteAlt, contentDescription = "Memories", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Header Chips (Weather, Today, Energy, Mood)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatusPill(icon = Icons.Default.WbSunny, text = "29° Sunny 70%", color = Color(0xFFFFD600))
                }
                item {
                    StatusPill(icon = Icons.Default.Today, text = "14 Wed Oct", color = Color(0xFF00E5FF))
                }
                item {
                    StatusPill(icon = Icons.Default.Bolt, text = "Energy 3/3 Unlimited", color = Color(0xFF00E676))
                }
                item {
                    StatusPill(icon = Icons.Default.Mood, text = "Mood: Happy Upbeat", color = Color(0xFFFF4081))
                }
            }

            // Orb & Status Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isChatHistoryExpanded) 0.85f else 1.25f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.clickable { onMicClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedVoiceOrb(
                            state = state,
                            rmsLevel = rmsLevel,
                            persona = persona,
                            size = if (isChatHistoryExpanded) 155.dp else 205.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 20-bar live Waveform Visualizer
                    WaveformVisualizer(
                        isWaveActive = state == AssistantState.LISTENING || state == AssistantState.SPEAKING,
                        amplitude = rmsLevel,
                        barColor = when (state) {
                            AssistantState.LISTENING -> Color(0xFFFF1744)
                            AssistantState.SPEAKING -> Color(0xFFE040FB)
                            AssistantState.THINKING -> Color(0xFF00E5FF)
                            else -> if (persona == AssistantPersona.GF_MODE) Color(0xFFFF4081) else MyraVioletPrimary
                        },
                        barCount = 20,
                        modifier = Modifier.height(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (state) {
                                AssistantState.LISTENING -> Color(0xFFFF1744).copy(alpha = 0.7f)
                                AssistantState.SPEAKING -> Color(0xFFE040FB).copy(alpha = 0.7f)
                                AssistantState.THINKING -> Color(0xFF00E5FF).copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (state == AssistantState.THINKING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            Text(
                                text = when (state) {
                                    AssistantState.LISTENING -> "DANI is listening..."
                                    AssistantState.THINKING -> "Thinking & reasoning..."
                                    AssistantState.SPEAKING -> "Speaking to you..."
                                    AssistantState.ERROR -> "Error occurred"
                                    AssistantState.IDLE -> "VIRTUAL ASSISTANT ONLINE • READY"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Live transcription
                    AnimatedVisibility(visible = partialTranscript.isNotBlank()) {
                        Text(
                            text = "\"$partialTranscript\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick Suggestions Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(quickCommands) { command ->
                    AssistChip(
                        onClick = { viewModel.onUserSpoke(command) },
                        label = { Text(command, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("quick_command_${command.take(8)}")
                    )
                }
            }

            // Conversation History Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isChatHistoryExpanded) 1.35f else 0.85f)
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isChatHistoryExpanded = !isChatHistoryExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (messages.isEmpty()) "Activity & History" else "History (${messages.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isChatHistoryExpanded) "▾ Collapse" else "▴ Expand",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (messages.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearChat() },
                                modifier = Modifier.size(28.dp).testTag("clear_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Chat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = persona.sampleGreeting,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Say \"Volume badhao\", \"Torch on karo\", \"WhatsApp kholo\" or ask anything",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                MessageBubble(
                                    message = msg,
                                    onReplay = { viewModel.replayMessage(msg.text) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(msg.text))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Copied to clipboard")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Keyboard / Type Input Button
                IconButton(
                    onClick = { showTextInputDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("keyboard_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Type a command",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Giant Central Mic FAB (Hold to voice chat / Tap)
                Box(contentAlignment = Alignment.Center) {
                    if (state == AssistantState.LISTENING) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF1744).copy(alpha = 0.28f))
                        )
                    }

                    FloatingActionButton(
                        onClick = onMicClicked,
                        shape = CircleShape,
                        containerColor = when (state) {
                            AssistantState.LISTENING -> Color(0xFFFF1744)
                            AssistantState.SPEAKING -> Color(0xFFE040FB)
                            AssistantState.THINKING -> Color(0xFF00E5FF)
                            else -> if (persona == AssistantPersona.GF_MODE) Color(0xFFFF4081) else MyraVioletPrimary
                        },
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("main_mic_button")
                    ) {
                        Icon(
                            imageVector = when {
                                state == AssistantState.SPEAKING -> Icons.Default.Stop
                                state == AssistantState.LISTENING -> Icons.Default.Stop
                                else -> Icons.Default.Mic
                            },
                            contentDescription = if (state == AssistantState.LISTENING) "Stop Listening" else "Start Listening",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Hands-free continuous toggle or Stop speaking
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.preferences.setContinuousMode(!continuousMode)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (!continuousMode) "Hands-free continuous mode enabled"
                                    else "Continuous mode disabled"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (continuousMode) MyraVioletSecondary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("hands_free_button")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Headset,
                        contentDescription = "Hands-free toggle",
                        tint = if (continuousMode) MyraVioletSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text(
                    text = "Message Dani AI",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = textInputQuery,
                    onValueChange = { textInputQuery = it },
                    placeholder = { Text("Ask or command Dani...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("text_input_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInputQuery.isNotBlank()) {
                                viewModel.sendTextMessage(textInputQuery.trim())
                                textInputQuery = ""
                                showTextInputDialog = false
                            }
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textInputQuery.isNotBlank()) {
                            viewModel.sendTextMessage(textInputQuery.trim())
                            textInputQuery = ""
                            showTextInputDialog = false
                        }
                    },
                    modifier = Modifier.testTag("text_input_send_button")
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onReplay: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val timeString = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) "You" else "Dani AI",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFFF4081)
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (!isUser && message.actionType != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (message.actionType) {
                                "BATTERY" -> Icons.Default.BatteryFull
                                "TORCH" -> Icons.Default.FlashlightOn
                                "OPEN_YOUTUBE" -> Icons.Default.PlayArrow
                                "SAVE_NOTE" -> Icons.Default.NoteAlt
                                else -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = message.actionDetail ?: "Action: ${message.actionType}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )

            if (!isUser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier.size(24.dp).testTag("replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp).testTag("copy_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
