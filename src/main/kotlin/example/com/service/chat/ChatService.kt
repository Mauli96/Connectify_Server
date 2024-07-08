package example.com.service.chat

import example.com.data.models.Message
import example.com.data.repository.chat.ChatRepository
import example.com.data.responses.ChatDto

class ChatService(
    private val chatRepository: ChatRepository
) {

    suspend fun doesChatBelongToUser(chatId: String, userId: String): Boolean {
        return chatRepository.doesChatBelongToUser(chatId, userId)
    }

    suspend fun getMessagesForChat(chatId: String, page: Int, pageSize: Int): List<Message> {
        return chatRepository.getMessagesForChat(chatId, page, pageSize)
    }

    suspend fun getChatsForUser(ownUserId: String): List<ChatDto> {
        return chatRepository.getChatsForUser(ownUserId)
    }
}