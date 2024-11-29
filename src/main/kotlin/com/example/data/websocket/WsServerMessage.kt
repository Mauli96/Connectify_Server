package example.com.data.websocket

import com.example.data.models.Message

data class WsServerMessage(
    val fromId: String,
    val toId: String,
    val text: String,
    val timestamp: Long,
    val chatId: String?,
    val id: String
) {
    fun toMessage(): Message {
        return Message(
            fromId = fromId,
            toId = toId,
            text = text,
            timestamp = timestamp,
            chatId = chatId
        )
    }
}
