package com.example.data.responses

data class ChatDto(
    val chatId: String,
    val remoteUserId: String?,
    val remoteUsername: String?,
    val remoteUserProfilePictureUrl: String?,
    val online: Boolean?,
    val lastSeen: Long?,
    val lastMessage: String?,
    val timestamp: Long?
)
