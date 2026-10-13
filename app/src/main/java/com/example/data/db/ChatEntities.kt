package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession

@Entity(tableName = "chat_sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isProject: Boolean,
    val projectCategory: String,
    val isPinned: Boolean,
    val isAotesMode: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain() = ChatSession(
        id = id,
        title = title,
        isProject = isProject,
        projectCategory = projectCategory,
        isPinned = isPinned,
        isAotesMode = isAotesMode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(session: ChatSession) = SessionEntity(
            id = session.id,
            title = session.title,
            isProject = session.isProject,
            projectCategory = session.projectCategory,
            isPinned = session.isPinned,
            isAotesMode = session.isAotesMode,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt
        )
    }
}

@Entity(tableName = "chat_messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val text: String,
    val imageUri: String?,
    val isAiEdited: Boolean,
    val isAotes: Boolean,
    val reactionData: String, // serialized emoji:count,emoji:count
    val timestamp: Long
) {
    fun toDomain(): ChatMessage {
        val reactionsMap = if (reactionData.isBlank()) {
            emptyMap()
        } else {
            reactionData.split(";")
                .mapNotNull { pair ->
                    val parts = pair.split(":")
                    if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 1) else null
                }
                .toMap()
        }

        return ChatMessage(
            id = id,
            sessionId = sessionId,
            role = role,
            text = text,
            imageUri = imageUri,
            isAiEdited = isAiEdited,
            isAotes = isAotes,
            reactions = reactionsMap,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): MessageEntity {
            val reactionStr = msg.reactions.entries.joinToString(";") { "${it.key}:${it.value}" }
            return MessageEntity(
                id = msg.id,
                sessionId = msg.sessionId,
                role = msg.role,
                text = msg.text,
                imageUri = msg.imageUri,
                isAiEdited = msg.isAiEdited,
                isAotes = msg.isAotes,
                reactionData = reactionStr,
                timestamp = msg.timestamp
            )
        }
    }
}
