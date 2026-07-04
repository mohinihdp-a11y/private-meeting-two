package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

// Authentication & User Profile
data class UserProfile(
    val id: String = "user_01",
    val username: String = "Alex Rivera",
    val email: String = "alex.rivera@meetanywhere.ai",
    val phone: String = "+1 (555) 019-2834",
    val organization: String = "AI Studio Labs",
    val bio: String = "Senior UX Architect & Technology Enthusiast",
    val status: String = "In a meeting",
    val availability: String = "Busy",
    val profilePictureUri: String? = null
)

// Meeting representation
data class Meeting(
    val id: String,
    val title: String,
    val description: String,
    val password: String = "123456",
    val date: String,
    val time: String,
    val timeZone: String = "GMT-07:00",
    val isRecurring: Boolean = false,
    val durationMinutes: Int = 45,
    val hostId: String = "user_01",
    val allowMic: Boolean = true,
    val allowCamera: Boolean = true,
    val allowChat: Boolean = true,
    val allowScreenShare: Boolean = true,
    val enableWaitingRoom: Boolean = false,
    val isLocked: Boolean = false
)

// Active Meeting Participant
data class Participant(
    val id: String,
    val name: String,
    val avatarColor: Color,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isHandRaised: Boolean = false,
    val isCoHost: Boolean = false,
    val isSharingScreen: Boolean = false,
    val isInWaitingRoom: Boolean = false,
    val networkQuality: String = "Excellent", // Excellent, Good, Poor
    val deviceType: String = "Android Tablet"
)

// Real-time Meeting Chat Message
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isPinned: Boolean = false,
    val isRead: Boolean = true,
    val codeBlock: String? = null,
    val fileAttachmentUri: String? = null,
    val fileType: String? = null, // "image", "pdf", "code"
    val isEdited: Boolean = false
)

// Interactive Interactive Whiteboard Stroke
data class WhiteboardStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val toolType: String = "pen" // "pen", "laser", "shapes_rect", "shapes_circle"
)

// Interactive Whiteboard Text & Sticky Notes
data class WhiteboardStickyNote(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color
)

// Dynamic Collaborative Polls
data class MeetingPoll(
    val id: String,
    val question: String,
    val options: List<String>,
    val votes: Map<Int, Int> = emptyMap(), // Option index to vote count
    val isAnonymous: Boolean = false,
    val isLive: Boolean = true,
    val hasVoted: Boolean = false
)

// Breakout Room representation
data class BreakoutRoom(
    val id: String,
    val name: String,
    val participants: List<String> // Participant IDs
)

// Notification Item
data class MeetNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val type: String // "invitation", "reminder", "recording"
)
