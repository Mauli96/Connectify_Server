package com.example.routes

import com.google.gson.Gson
import example.com.data.websocket.WsClientMessage
import com.example.service.chat.ChatController
import com.example.service.chat.ChatService
import com.example.util.Constants
import com.example.util.QueryParams
import com.example.util.WebSocketObject
import com.example.util.fromJsonOrNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.koin.java.KoinJavaComponent.inject

fun Route.getMessagesForChat(chatService: ChatService) {
    authenticate {
        get("/api/chat/messages") {
            val chatId = call.parameters[QueryParams.PARAM_CHAT_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            if(!chatService.doesChatBelongToUser(chatId, call.userId)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val messages = chatService.getMessagesForChat(chatId, page, pageSize)
            call.respond(
                HttpStatusCode.OK,
                messages
            )
        }
    }
}

fun Route.getChatsForUser(chatService: ChatService) {
    authenticate {
        get("/api/chats") {
            val chats = chatService.getChatsForUser(call.userId)
            call.respond(
                HttpStatusCode.OK,
                chats
            )
        }
    }
}

fun Route.chatWebSocket(chatController: ChatController) {
    authenticate {
        webSocket("/api/chat/websocket") {
            println("Connecting via web socket")
            chatController.onJoin(call.userId, this)
            try {
                chatController.markUserOnline(call.userId)
                incoming.consumeEach { frame ->
                    kotlin.run {
                        when(frame) {
                            is Frame.Text -> {
                                val frameText = frame.readText()
                                val delimiterIndex = frameText.indexOf("#")
                                if(delimiterIndex == -1) {
                                    println("No delimiter found")
                                    return@run
                                }
                                val type = frameText.substring(0, delimiterIndex).toIntOrNull()
                                if(type == null) {
                                    println("Invalid format")
                                    return@run
                                }
                                val json = frameText.substring(delimiterIndex + 1, frameText.length)
                                handleWebSocket(call.userId, chatController, type, json)
                            }
                            else -> Unit
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                println("Disconnecting ${call.userId}")
                chatController.onDisconnect(call.userId)
                chatController.markUserOffline(call.userId)
            }
        }
    }
}

suspend fun handleWebSocket(
    ownUserId: String,
    chatController: ChatController,
    type: Int,
    json: String
) {
    val gson by inject<Gson>(Gson::class.java)
    when(type) {
        WebSocketObject.MESSAGE.ordinal -> {
            val message = gson.fromJsonOrNull(json, WsClientMessage::class.java) ?: return
            chatController.sendMessage(ownUserId, gson, message)
        }
    }
}

fun Route.deleteChat(chatService: ChatService) {
    authenticate {
        delete("/api/chat/delete") {
            val chatId = call.parameters[QueryParams.PARAM_CHAT_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val doesChatBelongsToUser = chatService.doesChatBelongToUser(chatId, call.userId)

            if(!doesChatBelongsToUser) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }
            val deleted = chatService.deleteChat(chatId)
            if(deleted) {
                chatService.deleteMessagesFromChat(chatId)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

fun Route.deleteMessage(chatService: ChatService) {
    authenticate {
        delete("/api/chat/message/delete") {
            val messageId = call.parameters[QueryParams.PARAM_MESSAGE_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val message = chatService.getMessageById(messageId)

            if(message?.fromId != call.userId) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }
            val deleted = chatService.deleteMessage(messageId)
            if(deleted) {
                chatService.deleteMessage(messageId)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}