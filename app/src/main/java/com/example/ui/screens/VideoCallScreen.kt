package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.MeetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    viewModel: MeetViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val meeting by viewModel.activeMeeting.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val whiteboardStrokes by viewModel.whiteboardStrokes.collectAsState()
    val whiteboardNotes by viewModel.whiteboardNotes.collectAsState()
    val polls by viewModel.polls.collectAsState()
    val breakoutRooms by viewModel.breakoutRooms.collectAsState()

    val isMuted by viewModel.isMuted.collectAsState()
    val isCameraOn by viewModel.isCameraOn.collectAsState()
    val isSharingScreen by viewModel.isScreenSharing.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isChatDisabled by viewModel.isChatDisabled.collectAsState()

    val captionsEnabled by viewModel.liveCaptionsEnabled.collectAsState()
    val liveCaptions by viewModel.liveCaptions.collectAsState()
    val aiNotes by viewModel.aiNotes.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()

    // Active Drawer overlay: "none", "chat", "participants", "whiteboard", "polls", "breakout", "ai"
    var activeDrawer by remember { mutableStateOf("none") }

    // Whiteboard drawing parameters
    var currentColor by remember { mutableStateOf(Color.Blue) }
    var currentStrokeWidth by remember { mutableFloatStateOf(8f) }
    var points = remember { mutableStateListOf<Offset>() }

    // Chat Drawer parameters
    var chatInput by remember { mutableStateOf("") }

    // Poll creation parameters
    var showCreatePoll by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOption1 by remember { mutableStateOf("") }
    var pollOption2 by remember { mutableStateOf("") }

    // Dynamic grid layout speaker Spotlight
    var speakerSpotlightId by remember { mutableStateOf<String?>("user_02") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = meeting?.title ?: "Videoconference Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) Color.Red else Color.Gray)
                            )
                            Text(
                                text = if (isRecording) "REC | Room ID: ${meeting?.id}" else "Room ID: ${meeting?.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }
                },
                actions = {
                    if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFBBC05), modifier = Modifier.padding(end = 8.dp))
                    }
                    IconButton(onClick = { viewModel.toggleLiveCaptions() }) {
                        Icon(
                            imageVector = if (captionsEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                            contentDescription = "Captions",
                            tint = if (captionsEnabled) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMuteSelf() }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute self",
                            tint = if (isMuted) Color.Red else Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.toggleCameraSelf() }) {
                        Icon(
                            imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera self",
                            tint = if (isCameraOn) Color.White else Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // High-fidelity glowing bottom controls panel with 40dp rounded top corners
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                shadowElevation = 16.dp,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        icon = Icons.Default.Chat,
                        label = "Chat",
                        isActive = activeDrawer == "chat",
                        onClick = { activeDrawer = if (activeDrawer == "chat") "none" else "chat" },
                        tag = "call_chat_drawer_toggle"
                    )
                    ControlButton(
                        icon = Icons.Default.People,
                        label = "People",
                        isActive = activeDrawer == "participants",
                        onClick = { activeDrawer = if (activeDrawer == "participants") "none" else "participants" },
                        tag = "call_people_drawer_toggle"
                    )
                    ControlButton(
                        icon = Icons.Default.Gesture,
                        label = "Board",
                        isActive = activeDrawer == "whiteboard",
                        onClick = { activeDrawer = if (activeDrawer == "whiteboard") "none" else "whiteboard" },
                        tag = "call_board_drawer_toggle"
                    )
                    ControlButton(
                        icon = Icons.Default.Poll,
                        label = "Polls",
                        isActive = activeDrawer == "polls",
                        onClick = { activeDrawer = if (activeDrawer == "polls") "none" else "polls" },
                        tag = "call_polls_drawer_toggle"
                    )
                    ControlButton(
                        icon = Icons.Default.Psychology,
                        label = "AI",
                        isActive = activeDrawer == "ai",
                        onClick = { activeDrawer = if (activeDrawer == "ai") "none" else "ai" },
                        tag = "call_ai_drawer_toggle"
                    )
                    ControlButton(
                        icon = Icons.Default.CallEnd,
                        label = "Leave",
                        isActive = false,
                        color = Color.Red,
                        onClick = { viewModel.endActiveCall() },
                        tag = "call_end_btn"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212))
        ) {
            // Main Content Area: Left pane is Speaker Grid, Right pane is Active Drawer
            Row(modifier = Modifier.fillMaxSize()) {
                // Video Streams Grid Pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Waiting Room popover warning banner (visible if anyone isInWaitingRoom)
                    val waitingUser = participants.find { it.isInWaitingRoom }
                    if (waitingUser != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${waitingUser.name} is in the waiting room.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { viewModel.removeParticipant(waitingUser.id) }) {
                                        Text("Reject", color = Color.Red)
                                    }
                                    Button(
                                        onClick = { viewModel.admitWaitingParticipant(waitingUser.id) },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Admit")
                                    }
                                }
                            }
                        }
                    }

                    // Spotlight Screen share visualizer
                    val activeSharer = participants.find { it.isSharingScreen }
                    if (activeSharer != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E2E2E))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PresentToAll, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${activeSharer.name} is presenting their screen...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Participants grid (excl waiting room users)
                    val visibleParticipants = participants.filter { !it.isInWaitingRoom }
                    Box(modifier = Modifier.weight(1f)) {
                        GridFlowLayout(
                            visibleParticipants = visibleParticipants,
                            spotlightId = speakerSpotlightId,
                            onSpeakerClick = { id -> speakerSpotlightId = id }
                        )
                    }

                    // Dynamic Subtitles Overlay Layer (if captionsEnabled)
                    if (captionsEnabled && liveCaptions.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text("LIVE CAPTIONS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                liveCaptions.forEach { caption ->
                                    Text(
                                        text = caption,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // RIGHT PANEL DRAWER OVERLAY PANEL
                if (activeDrawer != "none") {
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .padding(top = 8.dp, bottom = 8.dp, end = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        when (activeDrawer) {
                            "chat" -> ChatDrawerContent(
                                messages = chatMessages,
                                inputVal = chatInput,
                                onValChange = { chatInput = it },
                                onSendMessage = {
                                    if (chatInput.isNotEmpty()) {
                                        viewModel.sendChatMessage(chatInput)
                                        chatInput = ""
                                    }
                                },
                                isChatDisabled = isChatDisabled
                            )
                            "participants" -> ParticipantsDrawerContent(
                                participants = participants,
                                onMuteEveryone = { viewModel.muteEveryone() },
                                onToggleLock = { viewModel.toggleLockMeeting() },
                                isLocked = isLocked,
                                onToggleDisableChat = { viewModel.toggleDisableChat() },
                                isChatDisabled = isChatDisabled,
                                onKick = { id -> viewModel.removeParticipant(id) }
                            )
                            "whiteboard" -> WhiteboardDrawerContent(
                                strokes = whiteboardStrokes,
                                notes = whiteboardNotes,
                                onAddStroke = { stroke -> viewModel.addWhiteboardStroke(stroke) },
                                onClear = { viewModel.clearWhiteboard() },
                                onAddSticky = { txt, col -> viewModel.addWhiteboardStickyNote(txt, 100f, 100f, col) }
                            )
                            "polls" -> PollsDrawerContent(
                                polls = polls,
                                onVote = { id, idx -> viewModel.votePoll(id, idx) },
                                showCreate = showCreatePoll,
                                onToggleCreate = { showCreatePoll = !showCreatePoll },
                                onCreatePoll = { q, o1, o2 ->
                                    if (q.isNotEmpty() && o1.isNotEmpty() && o2.isNotEmpty()) {
                                        viewModel.createPoll(q, listOf(o1, o2), false)
                                        showCreatePoll = false
                                        pollQuestion = ""
                                        pollOption1 = ""
                                        pollOption2 = ""
                                    }
                                },
                                q = pollQuestion,
                                onQChange = { pollQuestion = it },
                                o1 = pollOption1,
                                onO1Change = { pollOption1 = it },
                                o2 = pollOption2,
                                onO2Change = { pollOption2 = it }
                            )
                            "ai" -> AIDrawerContent(
                                notes = aiNotes,
                                isGenerating = isAiGenerating,
                                onGenerate = { viewModel.generateMeetingSummary() },
                                onToggleRecording = { viewModel.toggleRecording() },
                                isRecording = isRecording
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridFlowLayout(
    visibleParticipants: List<Participant>,
    spotlightId: String?,
    onSpeakerClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (spotlightId != null && visibleParticipants.any { it.id == spotlightId }) {
            // Speaker spotlight large pane
            val spotlightUser = visibleParticipants.first { it.id == spotlightId }
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF232323))
                    .border(
                        2.dp,
                        if (!spotlightUser.isMuted) Color(0xFF0F9D58) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSpeakerClick(spotlightUser.id) },
                contentAlignment = Alignment.Center
            ) {
                ParticipantVideoMock(participant = spotlightUser, isLarge = true)
            }

            // Other speakers grid list at bottom row
            val others = visibleParticipants.filter { it.id != spotlightId }
            if (others.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    others.forEach { participant ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF232323))
                                .border(
                                    1.5.dp,
                                    if (!participant.isMuted) Color(0xFF0F9D58) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSpeakerClick(participant.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            ParticipantVideoMock(participant = participant, isLarge = false)
                        }
                    }
                }
            }
        } else {
            // General multi-grid layout
            visibleParticipants.chunked(2).forEach { rowList ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowList.forEach { participant ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF232323))
                                .clickable { onSpeakerClick(participant.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            ParticipantVideoMock(participant = participant, isLarge = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantVideoMock(
    participant: Participant,
    isLarge: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (participant.isCameraOn) {
            // Canvas simulated video pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 40.dp.toPx()
                for (x in 0..size.width.toInt() step gridStep.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridStep.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }

            // Initials center circle representing talking user profile
            Box(
                modifier = Modifier
                    .size(if (isLarge) 84.dp else 48.dp)
                    .clip(CircleShape)
                    .background(participant.avatarColor)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLarge) 28.sp else 18.sp
                )
            }
        } else {
            // Camera Off placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(if (isLarge) 80.dp else 44.dp)
                            .clip(CircleShape)
                            .background(participant.avatarColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            participant.name.take(1).uppercase(),
                            color = participant.avatarColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLarge) 24.sp else 16.sp
                        )
                    }
                }
            }
        }

        // Overlay name label left bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (participant.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = null,
                tint = if (participant.isMuted) Color.Red else Color.Green,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = participant.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Dynamic Drawer Screen Sections ---

@Composable
fun ChatDrawerContent(
    messages: List<ChatMessage>,
    inputVal: String,
    onValChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isChatDisabled: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Meeting Chat", fontWeight = FontWeight.Bold, color = Color.White)
            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color.LightGray)
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        if (isChatDisabled) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Chat was disabled by meeting host.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.LightGray)
                            Text(msg.timestamp, fontSize = 9.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(msg.text, color = Color.White, fontSize = 12.sp)
                                if (msg.codeBlock != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black)
                                            .padding(6.dp)
                                    ) {
                                        Text(msg.codeBlock, color = Color.Green, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = onValChange,
                    placeholder = { Text("Message...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield"),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                IconButton(onClick = onSendMessage, modifier = Modifier.testTag("chat_send_btn")) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ParticipantsDrawerContent(
    participants: List<Participant>,
    onMuteEveryone: () -> Unit,
    onToggleLock: () -> Unit,
    isLocked: Boolean,
    onToggleDisableChat: () -> Unit,
    isChatDisabled: Boolean,
    onKick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Participants (${participants.size})", fontWeight = FontWeight.Bold, color = Color.White)
            Icon(Icons.Default.Group, contentDescription = null, tint = Color.LightGray)
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(onClick = onMuteEveryone, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Mute All", fontSize = 10.sp)
            }
            Button(onClick = onToggleLock, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (isLocked) Color.Green else Color.DarkGray)) {
                Text(if (isLocked) "Unlock" else "Lock Room", fontSize = 10.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(participants) { participant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(participant.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(participant.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(participant.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(if (participant.isCoHost) "Host" else "Attendee", color = Color.Gray, fontSize = 9.sp)
                        }
                    }

                    Row {
                        if (participant.id != "user_01") {
                            IconButton(onClick = { onKick(participant.id) }) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = "Kick", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhiteboardDrawerContent(
    strokes: List<WhiteboardStroke>,
    notes: List<WhiteboardStickyNote>,
    onAddStroke: (WhiteboardStroke) -> Unit,
    onClear: () -> Unit,
    onAddSticky: (String, Color) -> Unit
) {
    var strokesList = remember { mutableStateListOf<Offset>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Whiteboard Canvas", fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Board", tint = Color.Red)
            }
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            strokesList.clear()
                            strokesList.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            strokesList.add(change.position)
                            onAddStroke(WhiteboardStroke(strokesList.toList(), Color.Blue, 5f))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                strokes.forEach { stroke ->
                    val path = Path()
                    stroke.points.firstOrNull()?.let { start ->
                        path.moveTo(start.x, start.y)
                        stroke.points.forEach { point -> path.lineTo(point.x, point.y) }
                        drawPath(path, stroke.color, style = Stroke(stroke.strokeWidth))
                    }
                }
            }

            Text(
                "Draw on this canvas using swipe",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun PollsDrawerContent(
    polls: List<MeetingPoll>,
    onVote: (String, Int) -> Unit,
    showCreate: Boolean,
    onToggleCreate: () -> Unit,
    onCreatePoll: (String, String, String) -> Unit,
    q: String,
    onQChange: (String) -> Unit,
    o1: String,
    onO1Change: (String) -> Unit,
    o2: String,
    onO2Change: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Meeting Polls", fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = onToggleCreate) {
                Icon(
                    imageVector = if (showCreate) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Create Poll",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        if (showCreate) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = q,
                    onValueChange = onQChange,
                    label = { Text("Poll Question") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = o1,
                    onValueChange = onO1Change,
                    label = { Text("Option 1") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = o2,
                    onValueChange = onO2Change,
                    label = { Text("Option 2") },
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                Button(
                    onClick = { onCreatePoll(q, o1, o2) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publish Poll")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(polls) { poll ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(poll.question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            poll.options.forEachIndexed { idx, opt ->
                                val totalVotes = poll.votes.values.sum()
                                val optVotes = poll.votes[idx] ?: 0
                                val percent = if (totalVotes > 0) (optVotes.toFloat() / totalVotes.toFloat()) else 0f

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onVote(poll.id, idx) }
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(opt, color = Color.LightGray, fontSize = 11.sp)
                                        Text("$optVotes votes (${(percent * 100).toInt()}%)", color = Color.Gray, fontSize = 9.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percent },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = Color.Gray.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIDrawerContent(
    notes: String,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onToggleRecording: () -> Unit,
    isRecording: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gemini AI Engine", fontWeight = FontWeight.Bold, color = Color.White)
            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Recording Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cloud Archiver", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Record high fidelity audio feeds and generate automated transcripts.", color = Color.Gray, fontSize = 10.sp)
                    Button(
                        onClick = onToggleRecording,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isRecording) Color.White else Color.Red))
                            Text(if (isRecording) "Stop Recording" else "Record Session")
                        }
                    }
                }
            }

            // Summarizer Notes Section
            Button(
                onClick = onGenerate,
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI is compiling...")
                } else {
                    Text("Generate Smart Summary")
                }
            }

            if (notes.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = notes,
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No summary generated yet. Click above to analyze transcripts.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    color: Color = Color.White,
    onClick: () -> Unit,
    tag: String
) {
    val isLeave = label == "Leave" || icon == Icons.Default.CallEnd
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isLeave) Modifier.size(width = 64.dp, height = 48.dp).clip(RoundedCornerShape(24.dp))
                    else Modifier.size(48.dp).clip(CircleShape)
                )
                .background(
                    when {
                        isLeave -> Color(0xFFB3261E)
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    isLeave -> Color.White
                    isActive -> MaterialTheme.colorScheme.onPrimary
                    else -> Color.White
                },
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            label,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFFE3E2E6),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
