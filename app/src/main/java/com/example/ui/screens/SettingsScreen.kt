package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MeetViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var highContrastEnabled by remember { mutableStateOf(false) }
    var backgroundBlurLevel by remember { mutableFloatStateOf(0.5f) }
    var selectedLang by remember { mutableStateOf("English (United States)") }
    var showLangMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Preferences
            Text("General Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            ExposedDropdownRow("Default Language", selectedLang) { showLangMenu = true }
            DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                listOf("English (United States)", "Spanish (Español)", "French (Français)", "German (Deutsch)", "Japanese (日本語)").forEach { lang ->
                    DropdownMenuItem(text = { Text(lang) }, onClick = {
                        selectedLang = lang
                        showLangMenu = false
                    })
                }
            }

            Divider()

            // Accessibility Panel
            Text("Accessibility Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("High Contrast Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Enhances visual outlines and text weights.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = highContrastEnabled,
                    onCheckedChange = { highContrastEnabled = it },
                    modifier = Modifier.testTag("settings_highcontrast_switch")
                )
            }

            // Keyboard Shortcuts Reference
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Recommended Keyboard Shortcuts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    ShortcutRow("Ctrl + D", "Mute / Unmute microphone")
                    ShortcutRow("Ctrl + E", "Turn camera On / Off")
                    ShortcutRow("Ctrl + H", "Raise or lower hand")
                    ShortcutRow("Ctrl + L", "Toggle Live Subtitles / Captions")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save confirmation action button
            Button(
                onClick = {
                    Toast.makeText(context, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_save_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save and Return", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShortcutRow(keys: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(action, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(keys, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
        }
    }
}
