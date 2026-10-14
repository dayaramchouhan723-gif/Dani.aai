package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.AssistantPersona
import com.example.service.DaniAccessibilityService
import com.example.service.DaniForegroundService
import com.example.ui.theme.MyraVioletPrimary
import com.example.ui.theme.MyraVioletSecondary
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val persona by viewModel.preferences.persona.collectAsState()
    val speechRate by viewModel.preferences.speechRate.collectAsState()
    val speechPitch by viewModel.preferences.speechPitch.collectAsState()
    val autoSpeak by viewModel.preferences.autoSpeak.collectAsState()
    val hapticFeedback by viewModel.preferences.hapticFeedback.collectAsState()
    val audioChimes by viewModel.preferences.audioChimes.collectAsState()
    val continuousMode by viewModel.preferences.continuousMode.collectAsState()
    val isVipUnlocked by viewModel.preferences.isVipUnlocked.collectAsState()
    val wakeWordServiceActive by viewModel.preferences.wakeWordServiceActive.collectAsState()

    var secretCodeInput by remember { mutableStateOf("") }
    val hasGeminiApiKey = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (_: Exception) { false }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dani AI Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Secret OMG Code Unlock Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVipUnlocked) Color(0xFFFFD700).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isVipUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVipUnlocked) Icons.Default.CheckCircle else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isVipUnlocked) Color(0xFFFFD700) else Color(0xFFFF4081),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isVipUnlocked) "VIP Lifetime Free Unlocked!" else "Secret VIP Code (OMG)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isVipUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isVipUnlocked) "All 230 Dani AI features active: Kitsune GF Mode, 0-latency offline engine, unlimited access forever."
                        else "Enter secret master code 'OMG' to activate VIP Lifetime Free status and unlock special Kitsune GF effects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isVipUnlocked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = secretCodeInput,
                                onValueChange = { secretCodeInput = it },
                                placeholder = { Text("Code: OMG") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("secret_code_input")
                            )
                            Button(
                                onClick = {
                                    val success = viewModel.preferences.unlockVip(secretCodeInput)
                                    coroutineScope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar("VIP Lifetime Free Unlocked! 🎉")
                                        } else {
                                            snackbarHostState.showSnackbar("Invalid code. Hint: OMG")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                                modifier = Modifier.testTag("unlock_button")
                            ) {
                                Text("Unlock")
                            }
                        }
                    }
                }
            }

            // 1. Assistant Persona
            SettingsSectionHeader(title = "6 Dani Personas", icon = Icons.Default.Psychology, iconColor = Color(0xFFFF4081))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistantPersona.entries.forEach { p ->
                    val isSelected = p == persona
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.preferences.setPersona(p) }
                            .testTag("persona_${p.name}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFFF4081) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = p.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Voice Synthesis
            SettingsSectionHeader(title = "Voice Synthesis", icon = Icons.Default.RecordVoiceOver, iconColor = MyraVioletSecondary)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto Read Aloud", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                            Text(text = "Speak responses immediately", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.preferences.setAutoSpeak(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MyraVioletPrimary),
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Speaking Rate", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(text = "${String.format(java.util.Locale.US, "%.2f", speechRate)}x", color = MyraVioletPrimary)
                        }
                        Slider(
                            value = speechRate,
                            onValueChange = { viewModel.preferences.setSpeechRate(it) },
                            valueRange = 0.75f..1.5f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = MyraVioletPrimary, activeTrackColor = MyraVioletPrimary),
                            modifier = Modifier.testTag("speech_rate_slider")
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Voice Pitch", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(text = "${String.format(java.util.Locale.US, "%.2f", speechPitch)}x", color = Color(0xFFFF4081))
                        }
                        Slider(
                            value = speechPitch,
                            onValueChange = { viewModel.preferences.setSpeechPitch(it) },
                            valueRange = 0.75f..1.5f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF4081), activeTrackColor = Color(0xFFFF4081)),
                            modifier = Modifier.testTag("speech_pitch_slider")
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.replayMessage("Haan bolo jaan! Main Dani hoon, aapki sweet voice assistant.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081).copy(alpha = 0.15f), contentColor = Color(0xFFFF4081)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("test_voice_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Dani Voice", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 3. Android 11 Infinix Note 10 Automation & Services
            SettingsSectionHeader(title = "Android 11 Infinix Automation", icon = Icons.Default.Shield, iconColor = Color(0xFF00E676))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Background Foreground Service
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Always-on Background Service", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(text = "\"DANI is listening\" ongoing notification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = wakeWordServiceActive,
                            onCheckedChange = { active ->
                                viewModel.preferences.setWakeWordServiceActive(active)
                                if (active) {
                                    DaniForegroundService.start(context)
                                } else {
                                    DaniForegroundService.stop(context)
                                }
                            },
                            modifier = Modifier.testTag("fg_service_switch")
                        )
                    }

                    // Accessibility Service Status
                    val accessibilityEnabled = DaniAccessibilityService.isEnabled()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Accessibility Automation", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text(
                                text = if (accessibilityEnabled) "Enabled • Gestures active" else "Disabled • Tap to enable in Settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (accessibilityEnabled) Color(0xFF00E676) else Color(0xFFFF9100)
                            )
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.testTag("open_accessibility_btn")
                        ) {
                            Text("Setup", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }

                    // Floating Overlay Permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val hasOverlay = Settings.canDrawOverlays(context)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Floating Orb Overlay", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text(
                                    text = if (hasOverlay) "Granted • Can float over apps" else "Requires permission",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasOverlay) Color(0xFF00E676) else Color(0xFFFF9100)
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.testTag("open_overlay_btn")
                            ) {
                                Text("Allow", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}
