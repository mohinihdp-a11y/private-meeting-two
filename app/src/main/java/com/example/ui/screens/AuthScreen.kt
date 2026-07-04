package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MeetViewModel,
    initialSignUp: Boolean = false,
    onBackToLanding: () -> Unit
) {
    var authTab by remember { mutableStateOf(if (initialSignUp) "register" else "login") }
    val isSignUpMode = authTab == "register"
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Two-Factor SMS Verification Challenge State
    var showTwoFactorChallenge by remember { mutableStateOf(false) }
    var smsCodeInput by remember { mutableStateOf("") }
    var smsError by remember { mutableStateOf("") }

    var showGoogleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(when(authTab) { "register" -> "Create Account"; "quick" -> "Quick Access"; else -> "Sign In" }) },
                navigationIcon = {
                    IconButton(onClick = onBackToLanding) {
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
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Logo icon - Custom Private Meeting Shield Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        modifier = Modifier.size(56.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Private Meeting Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = when(authTab) {
                        "register" -> "Create Account"
                        "quick" -> "Instant Phone Entry"
                        else -> "Welcome Back"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when(authTab) {
                        "register" -> "Configure your profile and collaboration space."
                        "quick" -> "Enter your name and phone to join instantly. No password required."
                        else -> "Enter your workspace details to start conferencing."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Segmented selector choice chips for Sign In / Sign Up / Quick Phone
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "login" to "Sign In",
                        "register" to "Sign Up",
                        "quick" to "Quick Phone"
                    )
                    tabs.forEach { (tabId, label) ->
                        val isSelected = authTab == tabId
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { authTab = tabId }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Full Name field (Sign Up & Quick Phone modes)
                if (authTab == "register" || authTab == "quick") {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_fullname_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Email Input (Sign Up & Login modes)
                if (authTab == "register" || authTab == "login") {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }

                // Phone Input (Sign Up & Quick Phone modes)
                if (authTab == "register" || authTab == "quick") {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (authTab == "quick") "Phone Number" else "Phone Number (Optional for 2FA)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                // Organization Input (Sign Up only)
                if (authTab == "register") {
                    OutlinedTextField(
                        value = organization,
                        onValueChange = { organization = it },
                        label = { Text("Organization / Workspace") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_org_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Password Input (Sign Up & Login modes)
                if (authTab == "register" || authTab == "login") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Error indicator if any
                if (smsError.isNotEmpty() && authTab == "quick") {
                    Text(
                        text = smsError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Forgot Password link (Login mode only)
                if (authTab == "login") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .testTag("auth_forgot_password")
                                .clickable { }
                        )
                    }
                }

                // Primary submit button
                Button(
                    onClick = {
                        if (authTab == "quick") {
                            if (username.trim().isEmpty() || phone.trim().isEmpty()) {
                                smsError = "Please enter both name and phone number."
                            } else {
                                smsError = ""
                                viewModel.loginWithPhone(phone, username)
                            }
                        } else if (email.isNotEmpty()) {
                            if (isSignUpMode && phone.isNotEmpty()) {
                                // Trigger 2FA challenge
                                showTwoFactorChallenge = true
                            } else {
                                if (isSignUpMode) {
                                    viewModel.signUp(email, username, organization)
                                } else {
                                    viewModel.login(email, username)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_primary_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when(authTab) {
                            "register" -> "Register"
                            "quick" -> "Quick Enter"
                            else -> "Login"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Social Sign-in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SocialAuthButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Android, // Represents Google identity
                        label = "Google",
                        onClick = { showGoogleDialog = true }
                    )
                    SocialAuthButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Code, // Represents GitHub coding identity
                        label = "GitHub",
                        onClick = { viewModel.login("github.coder@github.com", "GitHub Contributor") }
                    )
                }

                // Toggle Auth Mode Link
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(authTab) {
                            "register" -> "Already have an account? "
                            "quick" -> "Prefer full credentials? "
                            else -> "Don't have an account? "
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when(authTab) {
                            "register" -> "Sign In"
                            "quick" -> "Sign In"
                            else -> "Sign Up"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .testTag("auth_mode_toggle")
                            .clickable {
                                authTab = when(authTab) {
                                    "register" -> "login"
                                    "quick" -> "login"
                                    else -> "register"
                                }
                            }
                    )
                }
            }

            // Glassmorphic SMS 2FA Challenge Popover
            AnimatedVisibility(
                visible = showTwoFactorChallenge,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Two-Factor Authentication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "We've sent a 6-digit passcode to $phone. Please enter it below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = smsCodeInput,
                                onValueChange = {
                                    smsCodeInput = it.take(6)
                                    smsError = ""
                                },
                                label = { Text("6-Digit Code") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sms_verification_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 4.sp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            if (smsError.isNotEmpty()) {
                                Text(
                                    text = smsError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showTwoFactorChallenge = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        if (smsCodeInput == "123456" || smsCodeInput.length == 6) {
                                            viewModel.signUp(email, username, organization)
                                            showTwoFactorChallenge = false
                                        } else {
                                            smsError = "Invalid verification code. Try 123456"
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sms_confirm_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Verify")
                                }
                            }
                        }
                    }
                }
            }

            // Google Account Picker Dialog
            if (showGoogleDialog) {
                AlertDialog(
                    onDismissRequest = { showGoogleDialog = false },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showGoogleDialog = false }) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Choose an account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "to continue to Meet Anywhere",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            AccountRow(
                                name = "Mohini",
                                email = "mohinihdp@gmail.com",
                                initial = "M",
                                color = Color(0xFFE8F0FE),
                                onClick = {
                                    viewModel.login("mohinihdp@gmail.com", "Mohini")
                                    showGoogleDialog = false
                                }
                            )
                            AccountRow(
                                name = "Google Guest",
                                email = "google.user@gmail.com",
                                initial = "G",
                                color = Color(0xFFE6F4EA),
                                onClick = {
                                    viewModel.login("google.user@gmail.com", "Google Guest")
                                    showGoogleDialog = false
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun AccountRow(
    name: String,
    email: String,
    initial: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SocialAuthButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label Icon",
                modifier = Modifier.size(18.dp)
            )
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}
