package com.example.data.repository.chat

import com.example.data.models.Chat
import com.example.data.models.Message
import com.example.data.responses.ChatDto

interface ChatRepository {

    suspend fun markUserOnline(userId: String)

    suspend fun markUserOffline(userId: String)

    suspend fun getMessagesForChat(
        chatId: String,
        page: Int,
        pageSize: Int
    ): List<Message>

    suspend fun getChatsForUser(ownUserId: String): List<ChatDto>

    suspend fun doesChatBelongToUser(
        chatId: String,
        userId: String
    ): Boolean

    suspend fun insertMessage(message: Message)

    suspend fun insertChat(
        userId1: String,
        userId2: String,
        messageId: String
    ): String

    suspend fun doesChatByUsersExist(
        userId1: String,
        userId2: String
    ): Boolean

    suspend fun updateLastMessageIdForChat(
        chatId: String,
        lastMessageId: String
    )

    suspend fun getMessageById(messageId: String): Message?

    suspend fun deleteMessage(messageId: String): Boolean

    suspend fun getChatFomLastMessageId(messageId: String): Chat?

    suspend fun getNewLastMessageId(chatId: String): String?

    suspend fun deleteMessagesFromChat(chatId: String)

    suspend fun deleteChat(chatId: String): Boolean
}