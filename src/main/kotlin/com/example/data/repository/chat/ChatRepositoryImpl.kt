package com.example.data.repository.chat

import com.mongodb.client.model.Sorts
import com.example.data.models.Chat
import com.example.data.models.Message
import com.example.data.models.Participant
import com.example.data.models.User
import com.example.data.responses.ChatDto
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineDatabase

class ChatRepositoryImpl(
    db: CoroutineDatabase
): ChatRepository {

    private val chats = db.getCollection<Chat>()
    private val users = db.getCollection<User>()
    private val messages = db.getCollection<Message>()

    override suspend fun markUserOnline(userId: String) {
        chats.updateMany(
            Chat::participants / Participant::userId eq userId,
            set(
                Chat::participants.posOp / Participant::online setTo true,
                Chat::participants.posOp / Participant::lastSeen setTo System.currentTimeMillis()
            )
        )
    }

    override suspend fun markUserOffline(userId: String) {
        chats.updateMany(
            Chat::participants / Participant::userId eq userId,
            set(
                Chat::participants.posOp / Participant::online setTo false,
                Chat::participants.posOp / Participant::lastSeen setTo System.currentTimeMillis()
            )
        )
    }

    override suspend fun getMessagesForChat(
        chatId: String,
        page: Int,
        pageSize: Int
    ): List<Message> {
        return messages.find(Message::chatId eq chatId)
            .skip(page * pageSize)
            .limit(pageSize)
            .ascendingSort(Message::timestamp)
            .toList()
    }

    override suspend fun getChatsForUser(ownUserId: String): List<ChatDto> {
        return chats.find(Chat::participants elemMatch(Participant::userId eq ownUserId))
            .descendingSort(Chat::timestamp)
            .toList()
            .map { chat ->
                val otherParticipant = chat.participants.find { it.userId != ownUserId }
                val user = users.findOneById(otherParticipant?.userId ?: "")
                val message = messages.findOneById(chat.lastMessageId)
                ChatDto(
                    chatId = chat.id,
                    remoteUserId = user?.id,
                    remoteUsername = user?.username,
                    remoteUserProfilePictureUrl = user?.profileImageUrl,
                    online = otherParticipant?.online,
                    lastSeen = otherParticipant?.lastSeen,
                    lastMessage = message?.text,
                    timestamp = message?.timestamp
                )
            }
    }

    override suspend fun doesChatBelongToUser(
        chatId: String,
        userId: String
    ): Boolean {
        return chats.findOneById(chatId)?.participants?.any { it.userId == userId } == true
    }

    override suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }

    override suspend fun insertChat(
        userId1: String,
        userId2: String,
        messageId: String
    ): String {
        val chat = Chat(
            participants = listOf(
                Participant(
                    userId = userId1,
                    online = false,
                    lastSeen = System.currentTimeMillis()
                ),
                Participant(
                    userId = userId2,
                    online = false,
                    lastSeen = System.currentTimeMillis()
                )
            ),
            lastMessageId = messageId,
            timestamp = System.currentTimeMillis()
        )
        val chatId = chats.insertOne(chat).insertedId?.asObjectId().toString()
        messages.updateOneById(messageId, setValue(Message::chatId, chatId))
        return chat.id
    }

    override suspend fun doesChatByUsersExist(
        userId1: String,
        userId2: String
    ): Boolean {
        return chats.find(
            and(
                Chat::participants elemMatch (Participant::userId eq userId1),
                Chat::participants elemMatch (Participant::userId eq userId2)
            )
        ).first() != null
    }

    override suspend fun updateLastMessageIdForChat(
        chatId: String,
        lastMessageId: String
    ) {
        chats.updateOneById(chatId, setValue(Chat::lastMessageId, lastMessageId))
    }

    override suspend fun getMessageById(messageId: String): Message? {
        return messages.findOneById(messageId)
    }

    override suspend fun deleteMessage(messageId: String): Boolean {
        return messages.deleteOneById(messageId).wasAcknowledged()
    }

    override suspend fun getChatFomLastMessageId(messageId: String): Chat? {
        return chats.findOne(Chat::lastMessageId eq messageId)
    }

    override suspend fun getNewLastMessageId(chatId: String): String? {
        return messages.find(Message::chatId eq chatId)
            .sort(Sorts.descending("timestamp"))
            .first()?.id
    }

    override suspend fun deleteMessagesFromChat(chatId: String) {
        messages.deleteMany(Message::chatId eq chatId)
    }

    override suspend fun deleteChat(chatId: String): Boolean {
        return chats.deleteOneById(chatId).wasAcknowledged()
    }
}