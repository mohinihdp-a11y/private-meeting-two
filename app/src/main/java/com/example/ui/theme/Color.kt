package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Professional Polish Design Theme Colors
val PolishBg = Color(0xFF111318)           // Rich slate-black background
val PolishSurface = Color(0xFF1E1F20)      // Dark slate-gray surface
val PolishPrimary = Color(0xFFA8C7FA)      // Glowing primary light blue
val PolishPrimaryContainer = Color(0xFF3F4759) // Slate gray for active state/containers
val PolishOnPrimary = Color(0xFF003354)    // Deep blue contrast for light-blue primary text
val PolishOnPrimaryContainer = Color(0xFFD3E3FD) // Light blue-gray text
val PolishOnBg = Color(0xFFE3E2E6)         // Light neutral text
val PolishOnSurface = Color(0xFFE3E2E6)    // Light neutral text on surfaces
val PolishOnSurfaceVariant = Color(0xFFC4C6D0) // Medium-light gray for labels and details
val PolishSurfaceVariant = Color(0xFF43474E) // Muted container color
val PolishError = Color(0xFFB3261E)        // Material 3 Red for end call/errors
val PolishSuccess = Color(0xFF4FBE86)      // Polish green for recordings and positive indicators

// Polish Light Scheme Colors - Professional Light Aesthetic
val LightBg = Color(0xFFF8F9FC)             // Very clean off-white
val LightSurface = Color(0xFFFFFFFF)        // Pure white for cards & surfaces
val LightPrimary = Color(0xFF1A73E8)        // Professional vibrant primary blue
val LightPrimaryContainer = Color(0xFFE8F0FE) // Light soft blue for active container backgrounds
val LightOnPrimary = Color(0xFFFFFFFF)      // White text on primary blue
val LightOnPrimaryContainer = Color(0xFF174EA6) // Contrast text for container
val LightOnBg = Color(0xFF111318)           // Rich dark slate text
val LightOnSurface = Color(0xFF111318)      // Rich dark text on surface
val LightOnSurfaceVariant = Color(0xFF5F6368) // Medium gray for subtext/labels
val LightSurfaceVariant = Color(0xFFF1F3F4)  // Light gray background
val LightError = Color(0xFFD93025)          // Clean warning/danger red
val LightSuccess = Color(0xFF188038)        // Clear success green

// Legacy / backup mappings for compatibility
val BlueMeet = LightPrimary
val BlueAccent = LightPrimary
val DarkSlate = LightBg
val SurfaceDark = LightSurface
val GrayDark = LightSurface
val OffWhite = LightOnBg
val TextDark = LightOnBg
val TextLight = LightOnBg

val PrimaryMeet = LightPrimary
val SecondaryMeet = LightPrimaryContainer
val TertiaryMeet = LightSuccess
val DarkBackground = LightBg
val DarkSurface = LightSurface
val LightBackground = LightBg
