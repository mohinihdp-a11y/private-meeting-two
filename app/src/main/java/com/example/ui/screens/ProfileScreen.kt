package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
fun ProfileScreen(
    viewModel: MeetViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var username by remember { mutableStateOf(currentUser.username) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var status by remember { mutableStateOf(currentUser.status) }
    var availability by remember { mutableStateOf(currentUser.availability) }
    var phone by remember { mutableStateOf(currentUser.phone) }

    var showAvailabilityMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Identity") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }

            Text(
                currentUser.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_username_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Bio description input
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Short Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("profile_bio_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Status note input
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Display Status Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_status_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Availability selection dropdown
            ExposedDropdownRow("Availability State", availability) { showAvailabilityMenu = true }
            DropdownMenu(expanded = showAvailabilityMenu, onDismissRequest = { showAvailabilityMenu = false }) {
                listOf("Available", "Busy", "Do Not Disturb", "In a meeting", "Away").forEach { state ->
                    DropdownMenuItem(text = { Text(state) }, onClick = {
                        availability = state
                        showAvailabilityMenu = false
                    })
                }
            }

            // Phone info field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Associated Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_phone_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save confirmation action button
            Button(
                onClick = {
                    viewModel.updateProfile(username, bio, status, availability, phone)
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_save_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Updates", fontWeight = FontWeight.Bold)
            }
        }
    }
}
