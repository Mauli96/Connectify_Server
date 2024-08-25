package example.com.data.repository.chat

import example.com.data.models.Chat
import example.com.data.models.Message
import example.com.data.responses.ChatDto

interface ChatRepository {

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

    suspend fun deleteMessagesFromChat(chatId: String)

    suspend fun deleteChat(chatId: String): Boolean
}