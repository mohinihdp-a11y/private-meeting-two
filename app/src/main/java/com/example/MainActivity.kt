package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MeetViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MeetViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Main screen router mappings
          when (currentScreen) {
            "landing" -> LandingScreen(
              viewModel = viewModel,
              onLoginClick = { viewModel.setScreen("login") },
              onSignUpClick = { viewModel.setScreen("signup") }
            )
            "login" -> AuthScreen(
              viewModel = viewModel,
              initialSignUp = false,
              onBackToLanding = { viewModel.setScreen("landing") }
            )
            "signup" -> AuthScreen(
              viewModel = viewModel,
              initialSignUp = true,
              onBackToLanding = { viewModel.setScreen("landing") }
            )
            "dashboard" -> DashboardScreen(
              viewModel = viewModel,
              onNavigateToCreate = { viewModel.setScreen("create_meeting") },
              onNavigateToJoin = { viewModel.setScreen("join_meeting") },
              onNavigateToProfile = { viewModel.setScreen("profile") },
              onNavigateToSettings = { viewModel.setScreen("settings") }
            )
            "create_meeting" -> CreateMeetingScreen(
              viewModel = viewModel,
              onBack = { viewModel.setScreen("dashboard") }
            )
            "join_meeting" -> JoinMeetingScreen(
              viewModel = viewModel,
              onBack = { viewModel.setScreen("dashboard") }
            )
            "call" -> VideoCallScreen(
              viewModel = viewModel
            )
            "settings" -> SettingsScreen(
              viewModel = viewModel,
              onBack = { viewModel.setScreen("dashboard") }
            )
            "profile" -> ProfileScreen(
              viewModel = viewModel,
              onBack = { viewModel.setScreen("dashboard") }
            )
            else -> LandingScreen(
              viewModel = viewModel,
              onLoginClick = { viewModel.setScreen("login") },
              onSignUpClick = { viewModel.setScreen("signup") }
            )
          }
        }
      }
    }
  }
}
