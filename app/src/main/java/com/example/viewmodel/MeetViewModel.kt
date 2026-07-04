package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MeetViewModel : ViewModel() {

    // --- Navigation & Auth ---
    private val _currentScreen = MutableStateFlow("landing")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // --- Dashboard State ---
    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    private val _notifications = MutableStateFlow<List<MeetNotification>>(emptyList())
    val notifications: StateFlow<List<MeetNotification>> = _notifications.asStateFlow()

    private val _storageUsageGb = MutableStateFlow(3.8f)
    val storageUsageGb: StateFlow<Float> = _storageUsageGb.asStateFlow()

    // --- Join Preview Settings ---
    private val _previewMicEnabled = MutableStateFlow(true)
    val previewMicEnabled: StateFlow<Boolean> = _previewMicEnabled.asStateFlow()

    private val _previewCamEnabled = MutableStateFlow(true)
    val previewCamEnabled: StateFlow<Boolean> = _previewCamEnabled.asStateFlow()

    private val _backgroundEffect = MutableStateFlow("none") // "none", "blur", "office", "space", "beach"
    val backgroundEffect: StateFlow<String> = _backgroundEffect.asStateFlow()

    private val _selectedCamera = MutableStateFlow("Front Camera (HD)")
    val selectedCamera: StateFlow<String> = _selectedCamera.asStateFlow()

    private val _selectedMicrophone = MutableStateFlow("Internal Microphone (Noise Cancelling)")
    val selectedMicrophone: StateFlow<String> = _selectedMicrophone.asStateFlow()

    private val _selectedSpeaker = MutableStateFlow("Stereo Speakers (Dolby)")
    val selectedSpeaker: StateFlow<String> = _selectedSpeaker.asStateFlow()

    // --- Active Meeting Session ---
    private val _activeMeeting = MutableStateFlow<Meeting?>(null)
    val activeMeeting: StateFlow<Meeting?> = _activeMeeting.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _whiteboardStrokes = MutableStateFlow<List<WhiteboardStroke>>(emptyList())
    val whiteboardStrokes: StateFlow<List<WhiteboardStroke>> = _whiteboardStrokes.asStateFlow()

    private val _whiteboardNotes = MutableStateFlow<List<WhiteboardStickyNote>>(emptyList())
    val whiteboardNotes: StateFlow<List<WhiteboardStickyNote>> = _whiteboardNotes.asStateFlow()

    private val _polls = MutableStateFlow<List<MeetingPoll>>(emptyList())
    val polls: StateFlow<List<MeetingPoll>> = _polls.asStateFlow()

    private val _breakoutRooms = MutableStateFlow<List<BreakoutRoom>>(emptyList())
    val breakoutRooms: StateFlow<List<BreakoutRoom>> = _breakoutRooms.asStateFlow()

    // --- Hardware and Call Status ---
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _isScreenSharing = MutableStateFlow(false)
    val isScreenSharing: StateFlow<Boolean> = _isScreenSharing.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isChatDisabled = MutableStateFlow(false)
    val isChatDisabled: StateFlow<Boolean> = _isChatDisabled.asStateFlow()

    // --- AI Subtitles & Summaries ---
    private val _liveCaptionsEnabled = MutableStateFlow(false)
    val liveCaptionsEnabled: StateFlow<Boolean> = _liveCaptionsEnabled.asStateFlow()

    private val _liveCaptions = MutableStateFlow<List<String>>(emptyList())
    val liveCaptions: StateFlow<List<String>> = _liveCaptions.asStateFlow()

    private val _aiNotes = MutableStateFlow("")
    val aiNotes: StateFlow<String> = _aiNotes.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // --- Simulator variables ---
    private var simulationJob: Job? = null
    private var simulatedTranscripts = mutableListOf<String>()

    init {
        // Load default meetings and notifications
        loadInitialMockData()
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    // --- Authentication Actions ---
    fun login(email: String, username: String) {
        _currentUser.update {
            it.copy(
                email = email.ifEmpty { "alex.rivera@meetanywhere.ai" },
                username = username.ifEmpty { "Alex Rivera" }
            )
        }
        _isLoggedIn.value = true
        _currentScreen.value = "dashboard"
        addNotification("Login Successful", "Welcome back, ${_currentUser.value.username}!", "reminder")
    }

    fun loginWithPhone(phone: String, username: String) {
        _currentUser.update {
            it.copy(
                phone = phone,
                username = username.ifEmpty { "Guest User" },
                email = "phone.guest@meetanywhere.ai",
                organization = "Quick Space"
            )
        }
        _isLoggedIn.value = true
        _currentScreen.value = "dashboard"
        addNotification("Quick Sign In", "Welcome, ${_currentUser.value.username}!", "reminder")
    }

    fun signUp(email: String, username: String, org: String) {
        _currentUser.update {
            it.copy(
                email = email,
                username = username,
                organization = org.ifEmpty { "AI Studio Labs" }
            )
        }
        _isLoggedIn.value = true
        _currentScreen.value = "dashboard"
        addNotification("Registration Complete", "Your Meet Anywhere profile is fully configured.", "reminder")
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = UserProfile()
        _currentScreen.value = "landing"
    }

    fun updateProfile(username: String, bio: String, status: String, availability: String, phone: String) {
        _currentUser.update {
            it.copy(
                username = username,
                bio = bio,
                status = status,
                availability = availability,
                phone = phone
            )
        }
    }

    // --- Meeting Scheduling ---
    fun scheduleMeeting(title: String, description: String, date: String, time: String, isRecurring: Boolean, duration: Int) {
        val id = generateMeetingId()
        val password = (100000..999999).random().toString()
        val newMeeting = Meeting(
            id = id,
            title = title.ifEmpty { "Quick Sync" },
            description = description.ifEmpty { "A high-fidelity video meeting." },
            password = password,
            date = date.ifEmpty { "Today" },
            time = time.ifEmpty { "12:00 PM" },
            isRecurring = isRecurring,
            durationMinutes = duration
        )
        _meetings.update { listOf(newMeeting) + it }
        addNotification("Meeting Scheduled", "Successfully created '$title' for $date at $time.", "reminder")
    }

    // --- Join Preview and Hardware selection ---
    fun togglePreviewMic() {
        _previewMicEnabled.value = !_previewMicEnabled.value
        _isMuted.value = !_previewMicEnabled.value
    }

    fun togglePreviewCam() {
        _previewCamEnabled.value = !_previewCamEnabled.value
        _isCameraOn.value = _previewCamEnabled.value
    }

    fun setBackgroundEffect(effect: String) {
        _backgroundEffect.value = effect
    }

    fun updateDevices(camera: String, mic: String, speaker: String) {
        _selectedCamera.value = camera
        _selectedMicrophone.value = mic
        _selectedSpeaker.value = speaker
    }

    // --- Meeting Action Handlers ---
    fun enterMeetingRoom(meetingId: String) {
        // Search if meeting exists, otherwise create a dynamic one
        val matched = _meetings.value.find { it.id.replace("-", "") == meetingId.replace("-", "") }
            ?: Meeting(
                id = formatMeetingId(meetingId),
                title = "Collaborative Meet",
                description = "Ad-hoc secure video meeting.",
                date = "Today",
                time = "Now"
            )

        _activeMeeting.value = matched
        
        // Setup standard mock participants
        val pList = listOf(
            Participant("user_01", _currentUser.value.username + " (You)", Color(0xFF1A73E8), isMuted = _isMuted.value, isCameraOn = _isCameraOn.value),
            Participant("user_02", "Sarah Chen", Color(0xFFEA4335), isMuted = false, isCameraOn = true, isCoHost = true),
            Participant("user_03", "Marcus Vance", Color(0xFFFBBC05), isMuted = true, isCameraOn = true),
            Participant("user_04", "Elena Rostova", Color(0xFF34A853), isMuted = false, isCameraOn = false),
            Participant("user_05", "Daniel Kim", Color(0xFF8E24AA), isMuted = true, isCameraOn = false, isInWaitingRoom = true)
        )
        _participants.value = pList

        // Setup clear whiteboard and polls
        _whiteboardStrokes.value = emptyList()
        _whiteboardNotes.value = emptyList()
        _polls.value = listOf(
            MeetingPoll(
                id = "poll_01",
                question = "Should we adopt Compose Navigation as our primary router?",
                options = listOf("Yes, absolutely", "No, keep custom state", "Undecided"),
                votes = mapOf(0 to 4, 1 to 1, 2 to 0)
            )
        )

        // Initial messages
        _chatMessages.value = listOf(
            ChatMessage("msg_01", "user_02", "Sarah Chen", "Welcome everyone! Glad we could sync.", getCurrentTimeString()),
            ChatMessage("msg_02", "user_03", "Marcus Vance", "Hey! Ready for the demo. Here is the sample schema:", getCurrentTimeString(), codeBlock = "data class User(val id: String, val name: String)")
        )

        // Setup Breakout Rooms
        _breakoutRooms.value = listOf(
            BreakoutRoom("br_01", "Room Alpha - Design System", listOf("user_02", "user_03")),
            BreakoutRoom("br_02", "Room Beta - Architecture", listOf("user_04"))
        )

        _currentScreen.value = "call"
        startCallSimulation()
    }

    fun endActiveCall() {
        stopCallSimulation()
        _activeMeeting.value = null
        _participants.value = emptyList()
        _chatMessages.value = emptyList()
        _currentScreen.value = "dashboard"
        _isScreenSharing.value = false
        _isRecording.value = false
    }

    // --- Host Controls ---
    fun toggleLockMeeting() {
        _isLocked.value = !_isLocked.value
        _activeMeeting.update { it?.copy(isLocked = _isLocked.value) }
    }

    fun toggleDisableChat() {
        _isChatDisabled.value = !_isChatDisabled.value
    }

    fun muteEveryone() {
        _participants.update { list ->
            list.map { if (it.id != "user_01") it.copy(isMuted = true) else it }
        }
    }

    fun admitWaitingParticipant(id: String) {
        _participants.update { list ->
            list.map { if (it.id == id) it.copy(isInWaitingRoom = false) else it }
        }
    }

    fun removeParticipant(id: String) {
        _participants.update { list -> list.filter { it.id != id } }
    }

    fun toggleMuteSelf() {
        _isMuted.value = !_isMuted.value
        _participants.update { list ->
            list.map { if (it.id == "user_01") it.copy(isMuted = _isMuted.value) else it }
        }
    }

    fun toggleCameraSelf() {
        _isCameraOn.value = !_isCameraOn.value
        _participants.update { list ->
            list.map { if (it.id == "user_01") it.copy(isCameraOn = _isCameraOn.value) else it }
        }
    }

    fun toggleHandRaiseSelf() {
        _participants.update { list ->
            list.map {
                if (it.id == "user_01") {
                    it.copy(isHandRaised = !it.isHandRaised)
                } else it
            }
        }
    }

    fun toggleScreenSharing() {
        _isScreenSharing.value = !_isScreenSharing.value
        _participants.update { list ->
            list.map { if (it.id == "user_01") it.copy(isSharingScreen = _isScreenSharing.value) else it }
        }
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
        if (!_isRecording.value) {
            // Add to storage
            _storageUsageGb.update { minOf(it + 0.4f, 15.0f) }
            addNotification("Recording Saved", "Your video recording for '${_activeMeeting.value?.title}' is now ready in storage.", "recording")
        }
    }

    // --- Interactive Poll Action ---
    fun createPoll(question: String, options: List<String>, isAnonymous: Boolean) {
        val newPoll = MeetingPoll(
            id = "poll_${UUID.randomUUID()}",
            question = question,
            options = options.filter { it.isNotEmpty() },
            isAnonymous = isAnonymous
        )
        _polls.update { it + newPoll }
    }

    fun votePoll(pollId: String, optionIndex: Int) {
        _polls.update { list ->
            list.map { poll ->
                if (poll.id == pollId) {
                    val currentVotes = poll.votes.toMutableMap()
                    currentVotes[optionIndex] = (currentVotes[optionIndex] ?: 0) + 1
                    poll.copy(votes = currentVotes, hasVoted = true)
                } else poll
            }
        }
    }

    // --- Interactive Chat Actions ---
    fun sendChatMessage(text: String, attachmentUri: String? = null, attachmentType: String? = null) {
        if (_isChatDisabled.value) return
        val codeBlockText = if (text.startsWith("```") && text.endsWith("```")) {
            text.removeSurrounding("```")
        } else null

        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            senderId = "user_01",
            senderName = _currentUser.value.username,
            text = if (codeBlockText != null) "Check out my code snippet below:" else text,
            timestamp = getCurrentTimeString(),
            codeBlock = codeBlockText,
            fileAttachmentUri = attachmentUri,
            fileType = attachmentType
        )
        _chatMessages.update { it + newMsg }
        simulatedTranscripts.add("${_currentUser.value.username}: $text")
    }

    fun togglePinMessage(msgId: String) {
        _chatMessages.update { list ->
            list.map { if (it.id == msgId) it.copy(isPinned = !it.isPinned) else it }
        }
    }

    // --- Interactive Whiteboard Actions ---
    fun addWhiteboardStroke(stroke: WhiteboardStroke) {
        _whiteboardStrokes.update { it + stroke }
    }

    fun clearWhiteboard() {
        _whiteboardStrokes.value = emptyList()
        _whiteboardNotes.value = emptyList()
    }

    fun addWhiteboardStickyNote(text: String, x: Float, y: Float, color: Color) {
        val newNote = WhiteboardStickyNote(
            id = "note_${UUID.randomUUID()}",
            text = text,
            x = x,
            y = y,
            color = color
        )
        _whiteboardNotes.update { it + newNote }
    }

    // --- AI Summarization Features ---
    fun toggleLiveCaptions() {
        _liveCaptionsEnabled.value = !_liveCaptionsEnabled.value
        if (!_liveCaptionsEnabled.value) {
            _liveCaptions.value = emptyList()
        }
    }

    fun generateMeetingSummary() {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val transcriptsText = if (simulatedTranscripts.isNotEmpty()) {
                simulatedTranscripts.joinToString("\n")
            } else {
                "Alex: Let's focus on finishing our design specification today.\nSarah: Sounds great, I will build out the grid layouts.\nMarcus: I've verified our performance metrics on low-end devices."
            }

            val prompt = """
                You are an advanced AI Meeting Assistant. Summarize this videoconference transcripts:
                $transcriptsText
                
                Please structure the response nicely with:
                1. Executive Summary
                2. Key Discussion Topics
                3. Action Items (with assignees if mentioned).
            """.trimIndent()

            val systemInstruction = "You are a professional software architect summarising critical team meetings."
            
            val summary = GeminiClient.generateContent(prompt, systemInstruction)
            _aiNotes.value = summary
            _isAiGenerating.value = false
        }
    }

    // --- Simulated Call Transcripts Generator ---
    private fun startCallSimulation() {
        simulatedTranscripts.clear()
        _liveCaptions.value = emptyList()
        
        val talkingDialogues = listOf(
            "Sarah Chen" to "Let's review the mock-ups. I went ahead with a glassmorphic dark theme.",
            "Marcus Vance" to "I checked the network latency over the Socket.IO server, looks under 50ms.",
            "Elena Rostova" to "Awesome! We should also optimize the local database queries in Room.",
            "Sarah Chen" to "Definitely! I think setting touch targets to 48dp made a massive difference.",
            "Marcus Vance" to "Don't forget to test the Gemini API summary prompt offline fallback.",
            "Elena Rostova" to "Yes, I will handle configuring the FCM notifications for schedulers tomorrow."
        )

        simulationJob = viewModelScope.launch {
            var index = 0
            while (true) {
                delay(12000) // Trigger every 12 seconds
                if (_activeMeeting.value == null) break

                val (speaker, text) = talkingDialogues[index % talkingDialogues.size]
                
                // Add caption if enabled
                if (_liveCaptionsEnabled.value) {
                    _liveCaptions.update { (it + "$speaker: \"$text\"").takeLast(3) }
                }

                // Add to transcripts
                simulatedTranscripts.add("$speaker: $text")

                // Randomly add a chat message (1 in 2 chance)
                if ((0..1).random() == 1) {
                    val senderId = when (speaker) {
                        "Sarah Chen" -> "user_02"
                        "Marcus Vance" -> "user_03"
                        else -> "user_04"
                    }
                    _chatMessages.update { list ->
                        list + ChatMessage(
                            id = "msg_sim_${UUID.randomUUID()}",
                            senderId = senderId,
                            senderName = speaker,
                            text = text,
                            timestamp = getCurrentTimeString()
                        )
                    }
                }
                index++
            }
        }
    }

    private fun stopCallSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    // --- Helpers & Initializers ---
    private fun loadInitialMockData() {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val todayStr = formatter.format(Date())

        _meetings.value = listOf(
            Meeting("id_01", "Design System Sync", "Review Material 3 styles, rounded shapes, and accessibility guidelines.", "882194", todayStr, "10:30 AM", isRecurring = true),
            Meeting("id_02", "Engineering Architecture Huddle", "Technical discussion regarding low-latency WebRTC and signaling.", "551203", todayStr, "02:00 PM"),
            Meeting("id_03", "Weekly Client Demo", "Present full video-conferencing prototype with collaborative features.", "732115", "Tomorrow", "11:00 AM")
        )

        _notifications.value = listOf(
            MeetNotification("not_01", "Invitation Received", "Daniel Kim invited you to: Product Launch Sync.", "10 mins ago", "invitation"),
            MeetNotification("not_02", "Recording Rendered", "Your recording for 'UI Workshop' is processed and saved.", "2 hours ago", "recording"),
            MeetNotification("not_03", "Upcoming Meeting", "'Design System Sync' starts in 15 minutes.", "Just now", "reminder")
        )
    }

    private fun addNotification(title: String, body: String, type: String) {
        val newNot = MeetNotification(
            id = "not_${UUID.randomUUID()}",
            title = title,
            body = body,
            timestamp = "Just now",
            type = type
        )
        _notifications.update { listOf(newNot) + it }
    }

    private fun generateMeetingId(): String {
        val segment1 = ('a'..'z').shuffled().take(3).joinToString("")
        val segment2 = ('a'..'z').shuffled().take(4).joinToString("")
        val segment3 = ('a'..'z').shuffled().take(3).joinToString("")
        return "$segment1-$segment2-$segment3"
    }

    private fun formatMeetingId(raw: String): String {
        val clean = raw.lowercase().filter { it.isLetter() }
        if (clean.length < 10) return raw
        return "${clean.substring(0, 3)}-${clean.substring(3, 7)}-${clean.substring(7, 10)}"
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }
}
