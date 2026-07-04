package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MeetViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinMeetingScreen(
    viewModel: MeetViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var meetingIdInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val previewMic by viewModel.previewMicEnabled.collectAsState()
    val previewCam by viewModel.previewCamEnabled.collectAsState()
    val backgroundEffect by viewModel.backgroundEffect.collectAsState()

    val selectedCam by viewModel.selectedCamera.collectAsState()
    val selectedMic by viewModel.selectedMicrophone.collectAsState()
    val selectedSpk by viewModel.selectedSpeaker.collectAsState()

    var showCamMenu by remember { mutableStateOf(false) }
    var showMicMenu by remember { mutableStateOf(false) }
    var showSpkMenu by remember { mutableStateOf(false) }

    // Wave animation parameter for dynamic mock camera video
    var animationFrame by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(previewCam) {
        if (previewCam) {
            while (true) {
                kotlinx.coroutines.delay(50)
                animationFrame += 0.05f
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-Join Lobby") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Camera Feed Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(
                            if (previewCam) 1.5.dp else 1.dp,
                            if (previewCam) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewCam) {
                        // Apply Selected Background Effect
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (backgroundEffect == "blur") Modifier.blur(16.dp) else Modifier
                                )
                        ) {
                            when (backgroundEffect) {
                                "space" -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFF1E0034), Color(0xFF03001C))
                                            )
                                        )
                                )
                                "beach" -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFFFFCC80), Color(0xFF81D4FA))
                                            )
                                        )
                                )
                                "office" -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF37474F))
                                )
                                else -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF212121))
                                )
                            }
                        }

                        // Drawn Animated Waveform / Face Indicator
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val radius = 60.dp.toPx()

                            // Face Contour
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f),
                                radius = radius,
                                center = Offset(centerX, centerY)
                            )

                            // Animated eyes
                            val eyeOffset = 20.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = 8.dp.toPx(),
                                center = Offset(centerX - eyeOffset, centerY - 10.dp.toPx())
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = 8.dp.toPx(),
                                center = Offset(centerX + eyeOffset, centerY - 10.dp.toPx())
                            )

                            // Animated sine smile
                            val mouthWidth = 40.dp.toPx()
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(centerX - mouthWidth / 2, centerY + 15.dp.toPx())
                            
                            // Smile curvature driven by sine
                            val smileCurve = centerY + 25.dp.toPx() + (sin(animationFrame) * 3.dp.toPx())
                            path.quadraticTo(
                                centerX, smileCurve,
                                centerX + mouthWidth / 2, centerY + 15.dp.toPx()
                            )
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.7f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Filter tag overlay
                        if (backgroundEffect != "none") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Filter: ${backgroundEffect.uppercase()}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Camera is Off indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Text("Camera is turned off", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Toggles Bar over video container bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mic Toggle
                        FloatingActionButton(
                            onClick = { viewModel.togglePreviewMic() },
                            containerColor = if (previewMic) MaterialTheme.colorScheme.primary else Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("lobby_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (previewMic) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic toggle"
                            )
                        }

                        // Camera Toggle
                        FloatingActionButton(
                            onClick = { viewModel.togglePreviewCam() },
                            containerColor = if (previewCam) MaterialTheme.colorScheme.primary else Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("lobby_cam_toggle")
                        ) {
                            Icon(
                                imageVector = if (previewCam) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Camera toggle"
                            )
                        }
                    }
                }

                // Virtual Background Filters Panel
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Select Virtual Background", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "none" to "Off",
                            "blur" to "Blur",
                            "office" to "Office",
                            "space" to "Space",
                            "beach" to "Beach"
                        ).forEach { (effect, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (backgroundEffect == effect) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (backgroundEffect == effect) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setBackgroundEffect(effect) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider()

                // Join credentials fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Connection Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = meetingIdInput,
                        onValueChange = { meetingIdInput = it },
                        label = { Text("Meeting Link or 10-Digit ID") },
                        placeholder = { Text("e.g., abc-defg-hij") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lobby_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Meeting Password (If requested)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lobby_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Divider()

                // Audio / Video Peripheral Selection Dropdowns
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Audio and Video Devices", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    // Camera selection dropdown
                    ExposedDropdownRow("Camera Input", selectedCam) { showCamMenu = true }
                    DropdownMenu(expanded = showCamMenu, onDismissRequest = { showCamMenu = false }) {
                        listOf("Front Camera (HD)", "Back Camera (Ultra Wide)", "External USB Capture Card").forEach { cam ->
                            DropdownMenuItem(text = { Text(cam) }, onClick = {
                                viewModel.updateDevices(cam, selectedMic, selectedSpk)
                                showCamMenu = false
                            })
                        }
                    }

                    // Microphone selection dropdown
                    ExposedDropdownRow("Microphone Input", selectedMic) { showMicMenu = true }
                    DropdownMenu(expanded = showMicMenu, onDismissRequest = { showMicMenu = false }) {
                        listOf("Internal Microphone (Noise Cancelling)", "Wired Headset Mic", "Studio USB Condenser Mic").forEach { mic ->
                            DropdownMenuItem(text = { Text(mic) }, onClick = {
                                viewModel.updateDevices(selectedCam, mic, selectedSpk)
                                showMicMenu = false
                            })
                        }
                    }

                    // Speaker selection dropdown
                    ExposedDropdownRow("Speaker Output", selectedSpk) { showSpkMenu = true }
                    DropdownMenu(expanded = showSpkMenu, onDismissRequest = { showSpkMenu = false }) {
                        listOf("Stereo Speakers (Dolby)", "Headphone Port", "Bluetooth AirBuds").forEach { spk ->
                            DropdownMenuItem(text = { Text(spk) }, onClick = {
                                viewModel.updateDevices(selectedCam, selectedMic, spk)
                                showSpkMenu = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Enter Room Call to Action
                Button(
                    onClick = {
                        if (meetingIdInput.isNotEmpty()) {
                            viewModel.enterMeetingRoom(meetingIdInput)
                        } else {
                            Toast.makeText(context, "Please supply a Meeting ID to join.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("lobby_join_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Join Conference Room", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ExposedDropdownRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
}
