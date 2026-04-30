package com.iliyadev.springboot.netty

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.netty.protocol.*
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame
import io.netty.handler.timeout.IdleStateEvent
import org.slf4j.LoggerFactory
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 WebSocket Frame Handler - Processes binary FlatBuffer messages
// Handles: Authentication, Message routing, Typing events, Read receipts
// ═══════════════════════════════════════════════════════════════════════════════

class WebSocketFrameHandler(
    private val connectionManager: ConnectionManager,
    private val messagePipeline: MessagePipelineService,
    private val jwtTokenUtils: JwtTokenUtils
) : SimpleChannelInboundHandler<Envelope>() {
    private val logger = LoggerFactory.getLogger(WebSocketFrameHandler::class.java)
    private var isAuthenticated: Boolean = false
    private var userId: UUID? = null

    override fun channelRead0(ctx: ChannelHandlerContext, envelope: Envelope) {
        if (!isAuthenticated && envelope.opCode != OpCode.AUTH_REQUEST) {
            sendError(ctx, "Not authenticated")
            return
        }
        when (envelope.opCode) {
            OpCode.AUTH_REQUEST -> handleAuth(ctx, envelope.payload)
            OpCode.CHAT_MESSAGE -> handleChatMessage(ctx, envelope.payload)
            OpCode.TYPING_EVENT -> handleTypingEvent(envelope.payload)
            OpCode.READ_RECEIPT -> handleReadReceipt(envelope.payload)
            OpCode.CHAT_LIST_REQUEST -> handleChatListRequest(ctx, envelope.payload)
            OpCode.MESSAGE_LIST_REQUEST -> handleMessageListRequest(ctx, envelope.payload)
            OpCode.STORY_LIST_REQUEST -> handleStoryListRequest(ctx, envelope.payload)
            OpCode.PING -> handlePing(ctx)
            else -> logger.warn("Unknown opCode: ${envelope.opCode}")
        }
    }

    private fun handleAuth(ctx: ChannelHandlerContext, payload: ByteArray) {
        val authRequest: AuthRequestProto = AuthRequestProto.deserialize(BinaryReader(payload))
        try {
            if (jwtTokenUtils.validateToken(authRequest.token)) {
                val userIdStr: String = jwtTokenUtils.getUserIdFromToken(authRequest.token)
                userId = UUID.fromString(userIdStr)
                isAuthenticated = true
                connectionManager.registerConnection(userId!!, ctx.channel())
                val response = AuthResponseProto(isSuccess = true, userId = userId, errorMessage = null)
                sendEnvelope(ctx, OpCode.AUTH_RESPONSE, response.serialize(BinaryWriter()).toByteArray())
                logger.info("✅ Auth success: userId=$userId")
            } else {
                sendAuthFailure(ctx, "Invalid token")
            }
        } catch (e: Exception) {
            sendAuthFailure(ctx, "Auth error: ${e.message}")
        }
    }

    private fun handleChatMessage(ctx: ChannelHandlerContext, payload: ByteArray) {
        val message: ChatMessageProto = ChatMessageProto.deserialize(BinaryReader(payload))
        val senderId: UUID = userId ?: return
        val enrichedMessage: ChatMessageProto = message.copy(
            senderId = senderId,
            timestamp = if (message.timestamp == 0L) System.currentTimeMillis() else message.timestamp
        )
        messagePipeline.processMessage(enrichedMessage)
        val ackWriter = BinaryWriter(8)
        ackWriter.writeLong(enrichedMessage.messageId)
        sendEnvelope(ctx, OpCode.ACK, ackWriter.toByteArray())
    }

    private fun handleTypingEvent(payload: ByteArray) {
        val event: TypingEventProto = TypingEventProto.deserialize(BinaryReader(payload))
        messagePipeline.processTypingEvent(event.copy(userId = userId ?: return))
    }

    private fun handleReadReceipt(payload: ByteArray) {
        val receipt: ReadReceiptProto = ReadReceiptProto.deserialize(BinaryReader(payload))
        messagePipeline.processReadReceipt(receipt.copy(userId = userId ?: return))
    }

    private fun handleChatListRequest(ctx: ChannelHandlerContext, payload: ByteArray) {
        val request: CursorPageRequest = CursorPageRequest.deserialize(BinaryReader(payload))
        val uid: UUID = userId ?: return
        messagePipeline.fetchChatList(uid, request) { responseBytes ->
            sendEnvelope(ctx, OpCode.CHAT_LIST_RESPONSE, responseBytes)
        }
    }

    private fun handleMessageListRequest(ctx: ChannelHandlerContext, payload: ByteArray) {
        val request: CursorPageRequest = CursorPageRequest.deserialize(BinaryReader(payload))
        val uid: UUID = userId ?: return
        messagePipeline.fetchMessages(uid, request) { responseBytes ->
            sendEnvelope(ctx, OpCode.MESSAGE_LIST_RESPONSE, responseBytes)
        }
    }

    private fun handleStoryListRequest(ctx: ChannelHandlerContext, payload: ByteArray) {
        val request: CursorPageRequest = CursorPageRequest.deserialize(BinaryReader(payload))
        val uid: UUID = userId ?: return
        messagePipeline.fetchStories(uid, request) { responseBytes ->
            sendEnvelope(ctx, OpCode.STORY_LIST_RESPONSE, responseBytes)
        }
    }

    private fun handlePing(ctx: ChannelHandlerContext) {
        sendEnvelope(ctx, OpCode.PONG, ByteArray(0))
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            ctx.channel().writeAndFlush(PingWebSocketFrame())
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connectionManager.removeConnection(ctx.channel())
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("WebSocket error for user $userId: ${cause.message}")
        connectionManager.removeConnection(ctx.channel())
        ctx.close()
    }

    private fun sendEnvelope(ctx: ChannelHandlerContext, opCode: Byte, payload: ByteArray) {
        val envelope = Envelope(opCode, payload)
        val bytes: ByteArray = envelope.serialize()
        val buf = Unpooled.wrappedBuffer(bytes)
        ctx.writeAndFlush(BinaryWebSocketFrame(buf))
    }

    private fun sendError(ctx: ChannelHandlerContext, message: String) {
        val writer = BinaryWriter()
        writer.writeString(message)
        sendEnvelope(ctx, OpCode.ERROR, writer.toByteArray())
    }

    private fun sendAuthFailure(ctx: ChannelHandlerContext, message: String) {
        val response = AuthResponseProto(isSuccess = false, userId = null, errorMessage = message)
        sendEnvelope(ctx, OpCode.AUTH_RESPONSE, response.serialize(BinaryWriter()).toByteArray())
        ctx.close()
    }
}
