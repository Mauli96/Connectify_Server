package com.example.service.chat

import com.google.gson.Gson
import com.example.data.repository.chat.ChatRepository
import example.com.data.websocket.WsClientMessage
import example.com.data.websocket.WsServerMessage
import com.example.util.WebSocketObject
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ChatController(
    private val repository: ChatRepository
) {

    private val onlineUsers = ConcurrentHashMap<String, WebSocketSession>()

    fun onJoin(
        userId: String,
        socket: WebSocketSession
    ) {
        onlineUsers[userId] = socket
    }

    fun onDisconnect(userId: String) {
        if(onlineUsers.containsKey(userId)) {
            onlineUsers.remove(userId)
        }
    }

    suspend fun markUserOnline(userId: String) {
        repository.markUserOnline(userId)
    }

    suspend fun markUserOffline(userId: String) {
        repository.markUserOffline(userId)
    }

    suspend fun sendMessage(
        ownUserId: String,
        gson: Gson,
        message: WsClientMessage
    ) {
        val messageEntity = message.toMessage(ownUserId)
        val wsServerMessage = WsServerMessage(
            fromId = ownUserId,
            toId = message.toId,
            text = message.text,
            timestamp = System.currentTimeMillis(),
            chatId = message.chatId,
            id = messageEntity.id
        )
        val frameText = gson.toJson(wsServerMessage)
        onlineUsers[ownUserId]?.send(Frame.Text("${WebSocketObject.MESSAGE.ordinal}#$frameText"))
        onlineUsers[message.toId]?.send(Frame.Text("${WebSocketObject.MESSAGE.ordinal}#$frameText"))
        if(!repository.doesChatByUsersExist(ownUserId, message.toId)) {
            val chatId = repository.insertChat(ownUserId, message.toId, messageEntity.id)
            repository.insertMessage(messageEntity.copy(chatId = chatId))
        } else {
            repository.insertMessage(messageEntity)
            message.chatId?.let {
                repository.updateLastMessageIdForChat(message.chatId, messageEntity.id)
            }
        }
    }
}