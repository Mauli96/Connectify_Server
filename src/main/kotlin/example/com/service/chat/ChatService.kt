package example.com.service.chat

import example.com.data.models.Message
import example.com.data.repository.chat.ChatRepository
import example.com.data.responses.ChatDto

class ChatService(
    private val chatRepository: ChatRepository
) {

    suspend fun doesChatBelongToUser(
        chatId: String,
        userId: String
    ): Boolean {
        return chatRepository.doesChatBelongToUser(chatId, userId)
    }

    suspend fun getMessagesForChat(
        chatId: String,
        page: Int,
        pageSize: Int
    ): List<Message> {
        return chatRepository.getMessagesForChat(chatId, page, pageSize)
    }

    suspend fun getChatsForUser(ownUserId: String): List<ChatDto> {
        return chatRepository.getChatsForUser(ownUserId)
    }

    suspend fun getMessageById(messageId: String): Message? {
        return chatRepository.getMessageById(messageId)
    }

    suspend fun deleteMessage(messageId: String): Boolean {
        val chat = chatRepository.getChatFomLastMessageId(messageId)
        if(chat?.lastMessageId == messageId) {
            val newLastMessageId = chatRepository.getNewLastMessageId(chat.id)
            if(newLastMessageId != null) {
                chatRepository.updateLastMessageIdForChat(chat.id, newLastMessageId)
            } else {
                chatRepository.deleteChat(chat.id)
            }
        }
        return chatRepository.deleteMessage(messageId)
    }

    suspend fun deleteMessagesFromChat(chatId: String) {
        return chatRepository.deleteMessagesFromChat(chatId)
    }

    suspend fun deleteChat(chatId: String): Boolean {
        return chatRepository.deleteChat(chatId)
    }
}