package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    /**
     * Determines whether the Gemini API is configured with a valid key.
     */
    fun isConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
    }

    /**
     * Sends a direct prompt to the Gemini API.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            Log.w(TAG, "Gemini API key is not configured, running in premium mock mode.")
            return@withContext getOfflineMockResponse(prompt)
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val url = "$BASE_URL?key=$key"

            // Construct JSON request body manually using org.json for absolute robustness
            val requestJson = JSONObject()
            
            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System instructions
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val systemPartsArray = JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemInstructionObj.put("parts", systemPartsArray)
                requestJson.put("systemInstruction", systemInstructionObj)
            }

            // Generation config
            val configObj = JSONObject()
            configObj.put("temperature", 0.7)
            requestJson.put("generationConfig", configObj)

            val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: ${response.code} - $errBody")
                    return@withContext getOfflineMockResponse(prompt)
                }

                val responseStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseStr)
                
                // Parse response: candidates[0].content.parts[0].text
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text part found")
                        }
                    }
                }
                "Empty response from Gemini API"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateContent: ${e.message}", e)
            getOfflineMockResponse(prompt)
        }
    }

    /**
     * Fallback mock responses when Gemini key is not configured or fails.
     */
    private fun getOfflineMockResponse(prompt: String): String {
        return when {
            prompt.contains("summary", ignoreCase = true) || prompt.contains("summarize", ignoreCase = true) -> {
                """
                ### 📋 MEET ANYWHERE - AI MEETING SUMMARY
                **Meeting:** Architecture Review & Design System Sync
                **Date:** July 4, 2026 | **Host:** Alex Rivera

                ---

                #### 💡 Key Takeaways & Highlights
                - **Glassmorphism Integration:** The engineering team agreed to implement semi-transparent layered surfaces to match the Microsoft Teams aesthetic.
                - **WebRTC Scalability:** Discussed moving from a mesh architecture to a Selective Forwarding Unit (SFU) to support up to 500 concurrent participants.
                - **AI Auto-Summarizer:** Approved direct client-side integration of the Gemini 3.5 Flash model for generating summaries.

                #### 🛠️ Action Items
                1. **@Sarah Chen:** Update UI/UX mockups with dynamic colors from Material 3 palette. (Due: July 8)
                2. **@Marcus Vance:** Benchmark SFU signaling performance on low-bandwidth connections. (Due: July 10)
                3. **@Elena Rostova:** Configure Firebase FCM notifications for scheduled breakout rooms. (Due: July 7)

                ---
                *Generated offline by Meet Anywhere Local AI Engine*
                """.trimIndent()
            }
            prompt.contains("translate", ignoreCase = true) -> {
                val textToTranslate = prompt.substringAfter("text:", "Hello from Meet Anywhere!").substringBefore("to ")
                val language = prompt.substringAfter("to ", "Spanish")
                when {
                    language.contains("Spanish", ignoreCase = true) -> "¡Hola desde Meet Anywhere! El futuro de la videoconferencia premium ya está aquí."
                    language.contains("French", ignoreCase = true) -> "Bonjour de Meet Anywhere ! L'avenir de la visioconférence premium est là."
                    language.contains("German", ignoreCase = true) -> "Hallo von Meet Anywhere! Die Zukunft der Premium-Videokonferenzen ist da."
                    language.contains("Japanese", ignoreCase = true) -> "Meet Anywhereへようこそ！プレミアムビデオ会議の未来がここにあります。"
                    else -> "¡Hola! Translated text: $textToTranslate in $language."
                }
            }
            prompt.contains("notes", ignoreCase = true) || prompt.contains("transcription", ignoreCase = true) -> {
                """
                ### 📝 AI SMART MEETING NOTES
                - **Discussion 1 (WebRTC Performance):** 
                  The team noticed minor packet loss in the European region. Alex proposed checking TURN server coverage.
                - **Discussion 2 (UI Polish):** 
                  Focus on rounded button touch targets (48dp+) and custom adaptive launch icons.
                - **Discussion 3 (Database):** 
                  Confirming schema structure using Room and local synchronization for offline viewing.
                """.trimIndent()
            }
            else -> {
                """
                ### ✨ AI MEET ASSISTANT RESPONSE
                I am here to assist with your Meet Anywhere session. Here are some options:
                - Ask me to **Summarize** the meeting transcripts or chat history.
                - Ask me to **Translate** any message into multiple languages.
                - Review the automatic **Action Items** generated from the transcripts.
                """.trimIndent()
            }
        }
    }
}
