package com.example.data.model

data class ChatSession(
    val id: String,
    val title: String,
    val isProject: Boolean = false,
    val projectCategory: String = "general", // general, code, study, image
    val isPinned: Boolean = false,
    val isAotesMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: String, // "user" or "ai"
    val text: String,
    val imageUri: String? = null,
    val isAiEdited: Boolean = false,
    val isAotes: Boolean = false,
    val reactions: Map<String, Int> = emptyMap(), // emoji -> count
    val timestamp: Long = System.currentTimeMillis()
)

data class StudentProfile(
    val studentClass: String = "Class 10 (Board Exam)",
    val favoriteSubjects: List<String> = listOf("Mathematics", "Physics", "Chemistry")
)

data class StudyPromptItem(
    val title: String,
    val prompt: String,
    val subject: String
)
