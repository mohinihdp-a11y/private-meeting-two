package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingScreen(
    viewModel: MeetViewModel,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(45) }
    var isRecurring by remember { mutableStateOf(false) }
    var allowCamera by remember { mutableStateOf(true) }
    var allowMicrophone by remember { mutableStateOf(true) }
    var allowChat by remember { mutableStateOf(true) }
    var enableWaitingRoom by remember { mutableStateOf(false) }

    // Result Generation State
    var generatedId by remember { mutableStateOf("") }
    var generatedPassword by remember { mutableStateOf("") }
    var isCreated by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up Video Conference") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isCreated) {
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting Title") },
                        placeholder = { Text("e.g., Weekly Standup Sync") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Agenda details and objectives...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("create_desc_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    // Duration row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Duration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (duration == mins) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { duration = mins }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "${mins}m",
                                        fontWeight = FontWeight.Bold,
                                        color = if (duration == mins) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // Recurring Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recurring Meeting", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Generates a permanent link for daily/weekly use.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            modifier = Modifier.testTag("create_recurring_switch")
                        )
                    }

                    Divider()

                    // HOST PRIVACY CONTROL HEADLINE
                    Text(
                        "Initial Host Permissions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Host Toggle List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HostToggleItem(
                            icon = Icons.Default.Mic,
                            title = "Allow Participant Microphones",
                            checked = allowMicrophone,
                            onCheckedChange = { allowMicrophone = it }
                        )
                        HostToggleItem(
                            icon = Icons.Default.Videocam,
                            title = "Allow Participant Cameras",
                            checked = allowCamera,
                            onCheckedChange = { allowCamera = it }
                        )
                        HostToggleItem(
                            icon = Icons.Default.Chat,
                            title = "Enable Real-time Chat Panel",
                            checked = allowChat,
                            onCheckedChange = { allowChat = it }
                        )
                        HostToggleItem(
                            icon = Icons.Default.Security,
                            title = "Enable Moderator Waiting Room",
                            checked = enableWaitingRoom,
                            onCheckedChange = { enableWaitingRoom = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create Action Button
                    Button(
                        onClick = {
                            // Generate instant details
                            val parts = listOf(
                                ('a'..'z').shuffled().take(3).joinToString(""),
                                ('a'..'z').shuffled().take(4).joinToString(""),
                                ('a'..'z').shuffled().take(3).joinToString("")
                            )
                            generatedId = parts.joinToString("-")
                            generatedPassword = (100000..999999).random().toString()
                            
                            viewModel.scheduleMeeting(
                                title = title,
                                description = description,
                                date = "Today",
                                time = "Now",
                                isRecurring = isRecurring,
                                duration = duration
                            )
                            isCreated = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("create_submit_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Video Space", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                } else {
                    // MEETING CREATED OUTCOME VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F9D58).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58), modifier = Modifier.size(40.dp))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Conference Created Successfully!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Share the connection link with co-hosts and attendees.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }

                        // Link Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LabelValueRow("Meeting Link", "https://meet.anywhere.ai/$generatedId")
                                LabelValueRow("Meeting ID", generatedId)
                                LabelValueRow("Room Password", generatedPassword)
                            }
                        }

                        // Share Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ShareActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.ContentCopy,
                                label = "Copy Link",
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("https://meet.anywhere.ai/$generatedId"))
                                    Toast.makeText(context, "Copied connection link!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            ShareActionButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Share,
                                label = "WhatsApp",
                                onClick = {
                                    Toast.makeText(context, "Redirecting to WhatsApp...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Visual Mock QR Code Placeholder
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Scan QR Code to Join", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            // Drawn Mock QR
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                // Dynamic grid layout representing QR blocks
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(10) { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            repeat(10) { col ->
                                                val isBlack = (row in 0..2 && col in 0..2) ||
                                                        (row in 7..9 && col in 0..2) ||
                                                        (row in 0..2 && col in 7..9) ||
                                                        ((row + col) % 3 == 0)
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .background(if (isBlack) Color.Black else Color.White)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Text("Room ID: $generatedId", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Start Now Button
                        Button(
                            onClick = {
                                viewModel.enterMeetingRoom(generatedId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("create_start_now_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Instant Call Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HostToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ShareActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
