package com.Kelasor.app.data.websocket

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import net.jpountz.lz4.LZ4Factory
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 🔌 Binary WebSocket Client - OkHttp + FlatBuffers + LZ4
// Auto-reconnect with exponential backoff
// Emits decoded messages via SharedFlow
// ═══════════════════════════════════════════════════════════════════════════════

private const val TAG: String = "BinaryWsClient"
private const val MIN_COMPRESS_SIZE: Int = 64
private const val MAX_RECONNECT_DELAY_MS: Long = 30000L
private const val INITIAL_RECONNECT_DELAY_MS: Long = 1000L

@Singleton
class BinaryWsClient @Inject constructor() {
    private val lz4Factory: LZ4Factory = LZ4Factory.fastestInstance()
    private val compressor = lz4Factory.fastCompressor()
    private val decompressor = lz4Factory.fastDecompressor()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null
    private var serverUrl: String = ""
    private var authToken: String = ""
    private var reconnectDelay: Long = INITIAL_RECONNECT_DELAY_MS
    private var isManualClose: Boolean = false

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingMessages = MutableSharedFlow<Envelope>(extraBufferCapacity = 256)
    val incomingMessages: SharedFlow<Envelope> = _incomingMessages

    private val _chatMessages = MutableSharedFlow<ChatMessageProto>(extraBufferCapacity = 256)
    val chatMessages: SharedFlow<ChatMessageProto> = _chatMessages

    private val _typingEvents = MutableSharedFlow<TypingEventProto>(extraBufferCapacity = 64)
    val typingEvents: SharedFlow<TypingEventProto> = _typingEvents

    private val _readReceipts = MutableSharedFlow<ReadReceiptProto>(extraBufferCapacity = 64)
    val readReceipts: SharedFlow<ReadReceiptProto> = _readReceipts

    sealed class ConnectionState {
        data object Disconnected : ConnectionState()
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data object Authenticated : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun connect(url: String, token: String) {
        serverUrl = url
        authToken = token
        isManualClose = false
        doConnect()
    }

    private fun doConnect() {
        _connectionState.value = ConnectionState.Connecting
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        val request: Request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, createListener())
    }

    private fun createListener(): WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WebSocket connected")
            _connectionState.value = ConnectionState.Connected
            reconnectDelay = INITIAL_RECONNECT_DELAY_MS
            sendAuth()
        }
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            try {
                val decompressed: ByteArray = decompressIfNeeded(bytes.toByteArray())
                val envelope: Envelope = Envelope.deserialize(decompressed)
                scope.launch { routeMessage(envelope) }
            } catch (e: Exception) {
                Log.e(TAG, "Decode error: ${e.message}")
            }
        }
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            handleDisconnect()
        }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
            _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
            handleDisconnect()
        }
    }

    private suspend fun routeMessage(envelope: Envelope) {
        _incomingMessages.emit(envelope)
        when (envelope.opCode) {
            OpCode.AUTH_RESPONSE -> handleAuthResponse(envelope.payload)
            OpCode.CHAT_MESSAGE -> {
                val msg = ChatMessageProto.deserialize(BinaryReader(envelope.payload))
                _chatMessages.emit(msg)
            }
            OpCode.TYPING_EVENT -> {
                val evt = TypingEventProto.deserialize(BinaryReader(envelope.payload))
                _typingEvents.emit(evt)
            }
            OpCode.READ_RECEIPT -> {
                val receipt = ReadReceiptProto.deserialize(BinaryReader(envelope.payload))
                _readReceipts.emit(receipt)
            }
        }
    }

    private fun handleAuthResponse(payload: ByteArray) {
        val response: AuthResponseProto = AuthResponseProto.deserialize(BinaryReader(payload))
        if (response.isSuccess) {
            _connectionState.value = ConnectionState.Authenticated
            Log.i(TAG, "Authenticated: userId=${response.userId}")
        } else {
            _connectionState.value = ConnectionState.Error("Auth failed: ${response.errorMessage}")
        }
    }

    private fun sendAuth() {
        val auth = AuthRequestProto(authToken)
        val writer = BinaryWriter()
        auth.serialize(writer)
        sendEnvelope(OpCode.AUTH_REQUEST, writer.toByteArray())
    }

    fun sendMessage(message: ChatMessageProto) {
        val writer = BinaryWriter()
        message.serialize(writer)
        sendEnvelope(OpCode.CHAT_MESSAGE, writer.toByteArray())
    }

    fun sendTypingEvent(chatId: UUID, userId: UUID, isTyping: Boolean) {
        val event = TypingEventProto(chatId, userId, isTyping)
        val writer = BinaryWriter()
        event.serialize(writer)
        sendEnvelope(OpCode.TYPING_EVENT, writer.toByteArray())
    }

    fun sendReadReceipt(chatId: UUID, userId: UUID, lastReadMessageId: Long) {
        val receipt = ReadReceiptProto(chatId, userId, lastReadMessageId, System.currentTimeMillis())
        val writer = BinaryWriter()
        receipt.serialize(writer)
        sendEnvelope(OpCode.READ_RECEIPT, writer.toByteArray())
    }

    fun requestChatList(cursorId: Long = 0, limit: Int = 20) {
        val request = CursorPageRequest(null, cursorId, limit)
        val writer = BinaryWriter()
        request.serialize(writer)
        sendEnvelope(OpCode.CHAT_LIST_REQUEST, writer.toByteArray())
    }

    fun requestMessages(chatId: UUID, cursorId: Long = 0, limit: Int = 20) {
        val request = CursorPageRequest(chatId, cursorId, limit)
        val writer = BinaryWriter()
        request.serialize(writer)
        sendEnvelope(OpCode.MESSAGE_LIST_REQUEST, writer.toByteArray())
    }

    fun requestStories(cursorId: Long = 0, limit: Int = 20) {
        val request = CursorPageRequest(null, cursorId, limit)
        val writer = BinaryWriter()
        request.serialize(writer)
        sendEnvelope(OpCode.STORY_LIST_REQUEST, writer.toByteArray())
    }

    private fun sendEnvelope(opCode: Byte, payload: ByteArray) {
        val envelope = Envelope(opCode, payload)
        val bytes: ByteArray = envelope.serialize()
        val compressed: ByteArray = compressIfNeeded(bytes)
        webSocket?.send(compressed.toByteString())
    }

    private fun compressIfNeeded(data: ByteArray): ByteArray {
        if (data.size < MIN_COMPRESS_SIZE) return data
        val maxLen: Int = compressor.maxCompressedLength(data.size)
        val compressed = ByteArray(maxLen)
        val compressedLen: Int = compressor.compress(data, 0, data.size, compressed, 0, maxLen)
        if (compressedLen >= data.size) return data
        val result = ByteBuffer.allocate(4 + compressedLen).order(ByteOrder.LITTLE_ENDIAN)
        result.putInt(data.size)
        result.put(compressed, 0, compressedLen)
        return result.array()
    }

    private fun decompressIfNeeded(data: ByteArray): ByteArray {
        if (data.size < 4) return data
        val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val originalSize: Int = buf.getInt()
        if (originalSize <= 0 || originalSize > 10_000_000) {
            return data
        }
        return try {
            val compressedBytes = ByteArray(data.size - 4)
            buf.get(compressedBytes)
            val decompressed = ByteArray(originalSize)
            decompressor.decompress(compressedBytes, 0, decompressed, 0, originalSize)
            decompressed
        } catch (e: Exception) {
            data
        }
    }

    private fun handleDisconnect() {
        if (isManualClose) return
        scope.launch {
            delay(reconnectDelay)
            reconnectDelay = minOf(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS)
            Log.i(TAG, "Reconnecting in ${reconnectDelay}ms...")
            doConnect()
        }
    }

    fun disconnect() {
        isManualClose = true
        webSocket?.close(1000, "Client disconnect")
        _connectionState.value = ConnectionState.Disconnected
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }
}
