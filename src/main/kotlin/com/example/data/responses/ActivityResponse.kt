package com.example.data.responses

data class ActivityResponse(
    val timestamp: Long,
    val userId: String,
    val profilePictureUrl: String,
    val parentId: String,
    val type: Int,
    val username: String,
    val id: String
)
