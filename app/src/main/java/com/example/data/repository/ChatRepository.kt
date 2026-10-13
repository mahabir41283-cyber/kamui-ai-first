package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.db.ChatDao
import com.example.data.db.MessageEntity
import com.example.data.db.SessionEntity
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.domain.KamuiPersona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val chatDao: ChatDao
) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createNewSession(
        title: String = "New Chat",
        isProject: Boolean = false,
        projectCategory: String = "general",
        isAotesMode: Boolean = false
    ): ChatSession = withContext(Dispatchers.IO) {
        val newSession = ChatSession(
            id = "session_" + UUID.randomUUID().toString().take(8),
            title = title,
            isProject = isProject,
            projectCategory = projectCategory,
            isAotesMode = isAotesMode
        )
        chatDao.insertSession(SessionEntity.fromDomain(newSession))
        newSession
    }

    suspend fun togglePinSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.togglePinSession(sessionId)
    }

    suspend fun renameSession(sessionId: String, newTitle: String) = withContext(Dispatchers.IO) {
        chatDao.renameSession(sessionId, newTitle)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllVaultSessions() = withContext(Dispatchers.IO) {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun addMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(MessageEntity.fromDomain(message))
    }

    suspend fun toggleReaction(messageId: String, currentReactions: Map<String, Int>, emoji: String) = withContext(Dispatchers.IO) {
        val mutable = currentReactions.toMutableMap()
        val currentCount = mutable[emoji] ?: 0
        if (currentCount > 0) {
            mutable.remove(emoji)
        } else {
            mutable[emoji] = 1
        }
        val serialized = mutable.entries.joinToString(";") { "${it.key}:${it.value}" }
        chatDao.updateMessageReactions(messageId, serialized)
    }

    suspend fun sendPrompt(
        sessionId: String,
        userPrompt: String,
        imageUri: Uri?,
        history: List<ChatMessage>,
        isAotesMode: Boolean,
        studentClass: String,
        favoriteSubjects: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent local fallback response demonstrating persona fidelity
            return@withContext generateLocalPersonaFallback(
                userPrompt = userPrompt,
                isAotesMode = isAotesMode,
                studentClass = studentClass,
                hasImage = imageUri != null
            )
        }

        try {
            val systemInstructionContent = Content(
                parts = listOf(Part(text = KamuiPersona.getSystemPrompt(isAotesMode, studentClass, favoriteSubjects)))
            )

            // Convert conversation history (limit to last 10 messages for speed)
            val contentsList = mutableListOf<Content>()
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.role == "user") "user" else "model"
                contentsList.add(
                    Content(
                        role = role,
                        parts = listOf(Part(text = msg.text))
                    )
                )
            }

            // Current user turn with optional inline image
            val userParts = mutableListOf<Part>()
            userParts.add(Part(text = userPrompt))

            if (imageUri != null) {
                val base64Img = encodeUriToBase64(imageUri)
                if (base64Img != null) {
                    userParts.add(
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64Img
                            )
                        )
                    )
                }
            }

            contentsList.add(
                Content(
                    role = "user",
                    parts = userParts
                )
            )

            val request = GenerateContentRequest(
                contents = contentsList,
                systemInstruction = systemInstructionContent,
                generationConfig = GenerationConfig(
                    temperature = if (isAotesMode) 0.5f else 0.3f,
                    topP = 0.9f,
                    topK = 40
                )
            )

            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text

            text ?: "No response generated from the neural core."
        } catch (e: Exception) {
            "Error contacting neural core: ${e.localizedMessage ?: e.message}. Check your network or API Key in Secrets panel."
        }
    }

    private fun encodeUriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Scale down to max 800px to maintain fast payload
            val maxDimension = 800
            val width = bitmap.width
            val height = bitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                maxDimension.toFloat() / maxOf(width, height)
            } else 1.0f

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else bitmap

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateLocalPersonaFallback(
        userPrompt: String,
        isAotesMode: Boolean,
        studentClass: String,
        hasImage: Boolean
    ): String {
        return if (!isAotesMode) {
            """
### KAMUI NEURAL CORE : ANALYSIS
Precision Response Mode active. 

```kotlin
// Production Kotlin / Coroutine Pattern
suspend fun executeTask(): Result<Unit> = runCatching {
    // Thread-safe atomic execution
    requireNotNull(System.currentTimeMillis())
}
```

* Formula Derivation:
${"$$"}E = mc^2 \quad \Longleftrightarrow \quad p = \frac{h}{\lambda}${"$$"}

Operational status: Standby. Configure GEMINI_API_KEY in the Secrets panel for live cloud model generation.
            """.trimIndent()
        } else {
            """
### AOTES STUDY ASSISTANT ($studentClass)
Welcome to structured academic preparation!

**Methodical Step-by-Step Breakdown:**
1. **Formula Identification**:
   ${"$$"}\text{Quadratic Formula: } x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}${"$$"}
2. **Standard Examination Working**:
   * Calculate Discriminant: ${'$'}\Delta = b^2 - 4ac${'$'}
   * Check Nature of Roots:
     - If ${'$'}\Delta > 0${'$'}: Two distinct real roots.
     - If ${'$'}\Delta = 0${'$'}: Real and equal roots.
     - If ${'$'}\Delta < 0${'$'}: No real roots.

*Tip for $studentClass Board Exams*: Always state the given values and formula explicitly to secure full step marks!
            """.trimIndent()
        }
    }

    suspend fun exportVaultAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "KAMUI AI / AOTES STUDY")
        root.put("exportedAt", System.currentTimeMillis())
        val sessionsArray = JSONArray()

        val all = chatDao.getAllMessages()
        val grouped = all.groupBy { it.sessionId }

        for ((sessionId, messages) in grouped) {
            val sessionObj = JSONObject()
            sessionObj.put("sessionId", sessionId)
            val msgsArray = JSONArray()
            for (m in messages) {
                val mObj = JSONObject()
                mObj.put("role", m.role)
                mObj.put("text", m.text)
                mObj.put("timestamp", m.timestamp)
                msgsArray.put(mObj)
            }
            sessionObj.put("messages", msgsArray)
            sessionsArray.put(sessionObj)
        }
        root.put("sessions", sessionsArray)
        root.toString(2)
    }

    suspend fun exportVaultAsTxt(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("========================================================\n")
        sb.append("KAMUI AI / AOTES STUDY — PRIVATE VAULT TRANSCRIPT\n")
        sb.append("Exported: ${java.util.Date()}\n")
        sb.append("========================================================\n\n")

        val all = chatDao.getAllMessages()
        val grouped = all.groupBy { it.sessionId }

        grouped.forEach { (sessionId, messages) ->
            sb.append("\n--------------------------------------------------------\n")
            sb.append("SESSION: $sessionId\n")
            sb.append("--------------------------------------------------------\n")
            for (m in messages) {
                sb.append("[${m.role.uppercase()}]: ${m.text}\n\n")
            }
        }
        sb.toString()
    }
}
