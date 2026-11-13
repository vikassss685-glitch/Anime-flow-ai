package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String = "General",
    val language: String = "English",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String, // "user" or "assistant"
    val text: String,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "image", "document", "voice"
    val isSampleResponse: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_creations")
data class SavedCreationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "IMAGE", "VIDEO", "MUSIC", "CODE", "DOCUMENT", "STUDY"
    val title: String,
    val prompt: String,
    val outputSummary: String,
    val modelTag: String,
    val isSampleResponse: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
