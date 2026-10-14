package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyraVioletPrimary
import com.example.ui.theme.MyraVioletSecondary
import com.example.ui.viewmodel.AssistantViewModel

data class SkillItem(
    val category: String,
    val title: String,
    val description: String,
    val voicePrompt: String,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val skills = listOf(
        SkillItem(
            category = "Device Controls",
            title = "Device Flashlight",
            description = "Toggle your phone's camera torch instantaneously.",
            voicePrompt = "Turn on flashlight",
            icon = Icons.Default.FlashlightOn,
            iconColor = Color(0xFFFFD600)
        ),
        SkillItem(
            category = "Device Controls",
            title = "Battery & Power",
            description = "Check real-time battery percentage and charging state.",
            voicePrompt = "Check my battery level",
            icon = Icons.Default.BatteryChargingFull,
            iconColor = Color(0xFF00E676)
        ),
        SkillItem(
            category = "Device Controls",
            title = "Launch Camera",
            description = "Open the camera viewfinder instantly to take photos.",
            voicePrompt = "Open camera",
            icon = Icons.Default.CameraAlt,
            iconColor = Color(0xFFFF5252)
        ),
        SkillItem(
            category = "Device Controls",
            title = "Launch Calculator",
            description = "Open the device calculator or ask Myra to calculate directly.",
            voicePrompt = "Open calculator",
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFF448AFF)
        ),
        SkillItem(
            category = "Device Controls",
            title = "Device Settings",
            description = "Access Android system preferences and network settings.",
            voicePrompt = "Open device settings",
            icon = Icons.Default.Settings,
            iconColor = Color(0xFF90A4AE)
        ),
        SkillItem(
            category = "Media & Navigation",
            title = "YouTube Search & Play",
            description = "Play music, discover podcasts, or watch video channels.",
            voicePrompt = "Play chill lofi on YouTube",
            icon = Icons.Default.PlayArrow,
            iconColor = Color(0xFFFF1744)
        ),
        SkillItem(
            category = "Media & Navigation",
            title = "Google Maps Navigation",
            description = "Find nearby restaurants, coffee shops, or directions.",
            voicePrompt = "Where is the nearest coffee shop?",
            icon = Icons.Default.Map,
            iconColor = Color(0xFF00B0FF)
        ),
        SkillItem(
            category = "Media & Navigation",
            title = "Web Knowledge Search",
            description = "Search Google for live facts, current events, and answers.",
            voicePrompt = "Search Google for James Webb Telescope",
            icon = Icons.Default.Search,
            iconColor = MyraVioletSecondary
        ),
        SkillItem(
            category = "Productivity & Voice",
            title = "Voice Notes & Reminders",
            description = "Capture voice thoughts and store them in local database.",
            voicePrompt = "Take a note: Prepare project report for tomorrow",
            icon = Icons.Default.NoteAlt,
            iconColor = MyraVioletPrimary
        ),
        SkillItem(
            category = "Productivity & Voice",
            title = "Set System Timer",
            description = "Configure a countdown timer for tea, workouts, or study.",
            voicePrompt = "Set a 5-minute timer",
            icon = Icons.Default.Timer,
            iconColor = Color(0xFFFF9100)
        ),
        SkillItem(
            category = "Productivity & Voice",
            title = "Configure Alarm",
            description = "Set wake up alarms for tomorrow morning.",
            voicePrompt = "Set alarm for 7:30 AM",
            icon = Icons.Default.Alarm,
            iconColor = Color(0xFFE040FB)
        ),
        SkillItem(
            category = "Productivity & Voice",
            title = "Phone Calling",
            description = "Quickly dial a phone number or open phone dialer.",
            voicePrompt = "Dial 911",
            icon = Icons.Default.Phone,
            iconColor = Color(0xFF00C853)
        ),
        SkillItem(
            category = "Productivity & Voice",
            title = "SMS Messaging",
            description = "Compose and send text messages through your SMS app.",
            voicePrompt = "Send message to Mom: Running late for dinner",
            icon = Icons.Default.Sms,
            iconColor = Color(0xFF00B8D4)
        ),
        SkillItem(
            category = "AI Intelligence",
            title = "Conversational Gemini AI",
            description = "Deep reasoning, creative writing, science, philosophy & humor.",
            voicePrompt = "Tell me a mind-bending fact about black holes",
            icon = Icons.Default.AutoAwesome,
            iconColor = MyraVioletPrimary
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Agent Skills & Actions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("skills_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MyraVioletPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Myra Agent Architecture",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Myra combines voice synthesis, continuous speech recognition, real Android system intents, and Gemini 3.5 Flash intelligence. Tap 'Try Command' on any skill below to execute it immediately!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            items(skills) { skill ->
                SkillCard(
                    skill = skill,
                    onTry = {
                        viewModel.onUserSpoke(skill.voicePrompt)
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
fun SkillCard(
    skill: SkillItem,
    onTry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(skill.iconColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, skill.iconColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = skill.icon,
                            contentDescription = null,
                            tint = skill.iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = skill.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = skill.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onTry,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyraVioletPrimary.copy(alpha = 0.15f),
                        contentColor = MyraVioletPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("try_skill_${skill.title.take(6)}")
                ) {
                    Text("Try Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Say: ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MyraVioletSecondary
                    )
                    Text(
                        text = "\"${skill.voicePrompt}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
