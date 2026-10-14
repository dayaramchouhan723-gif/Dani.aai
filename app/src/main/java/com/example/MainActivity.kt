package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ScanOcrScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkillsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MyraVioletPrimary
import com.example.ui.viewmodel.AssistantViewModel

enum class AppDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Mic),
    SCAN_OCR("Vision", Icons.Default.DocumentScanner),
    MEMORIES("Memories", Icons.Default.NoteAlt),
    SKILLS("Skills", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DaniAiApp()
                }
            }
        }
    }
}

@Composable
fun DaniAiApp(viewModel: AssistantViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title,
                                tint = if (isSelected) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFF4081).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            label = "ScreenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                AppDestination.HOME -> {
                    AssistantScreen(
                        viewModel = viewModel,
                        onNavigateToSkills = { currentDestination = AppDestination.SKILLS },
                        onNavigateToNotes = { currentDestination = AppDestination.MEMORIES },
                        onNavigateToScanOcr = { currentDestination = AppDestination.SCAN_OCR },
                        onNavigateToSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }
                AppDestination.SCAN_OCR -> {
                    ScanOcrScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.MEMORIES -> {
                    NotesScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.SKILLS -> {
                    SkillsScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
            }
        }
    }
}
