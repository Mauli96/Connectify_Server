package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Participant(
    val userId: String,
    val online: Boolean,
    val lastSeen: Long
)

data class Chat(
    val participants: List<Participant>,
    val lastMessageId: String,
    val timestamp: Long,
    @BsonId
    val id: String = ObjectId().toString(),
)
