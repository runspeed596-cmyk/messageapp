package com.iliyadev.springboot.netty.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 Binary Protocol - FlatBuffer-style compact binary serialization
// All messages use little-endian byte order for maximum efficiency.
// Wire format: [1 byte opCode] [4 bytes payload length] [N bytes payload]
// ═══════════════════════════════════════════════════════════════════════════════

object OpCode {
    const val CHAT_MESSAGE: Byte = 0x01
    const val CHAT_LIST_REQUEST: Byte = 0x02
    const val CHAT_LIST_RESPONSE: Byte = 0x03
    const val MESSAGE_LIST_REQUEST: Byte = 0x04
    const val MESSAGE_LIST_RESPONSE: Byte = 0x05
    const val TYPING_EVENT: Byte = 0x06
    const val READ_RECEIPT: Byte = 0x07
    const val STORY_LIST_REQUEST: Byte = 0x08
    const val STORY_LIST_RESPONSE: Byte = 0x09
    const val STORY_ITEM: Byte = 0x0A
    const val MEDIA_META: Byte = 0x0B
    const val ACK: Byte = 0x0C
    const val AUTH_REQUEST: Byte = 0x0D
    const val AUTH_RESPONSE: Byte = 0x0E
    const val UNREAD_COUNT: Byte = 0x0F
    const val ONLINE_STATUS: Byte = 0x10
    const val ERROR: Byte = 0x11
    const val PING: Byte = 0x12
    const val PONG: Byte = 0x13
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binary Writer - Zero-allocation reusable buffer builder
// ═══════════════════════════════════════════════════════════════════════════════

class BinaryWriter(initialCapacity: Int = 256) {
    private var buffer: ByteBuffer = ByteBuffer.allocate(initialCapacity).order(ByteOrder.LITTLE_ENDIAN)
    fun ensureCapacity(needed: Int) {
        if (buffer.remaining() < needed) {
            val newCap = maxOf(buffer.capacity() * 2, buffer.position() + needed)
            val newBuf = ByteBuffer.allocate(newCap).order(ByteOrder.LITTLE_ENDIAN)
            buffer.flip()
            newBuf.put(buffer)
            buffer = newBuf
        }
    }
    fun writeByte(value: Byte): BinaryWriter { ensureCapacity(1); buffer.put(value); return this }
    fun writeInt(value: Int): BinaryWriter { ensureCapacity(4); buffer.putInt(value); return this }
    fun writeLong(value: Long): BinaryWriter { ensureCapacity(8); buffer.putLong(value); return this }
    fun writeUUID(value: UUID): BinaryWriter { writeLong(value.mostSignificantBits); writeLong(value.leastSignificantBits); return this }
    fun writeString(value: String?): BinaryWriter {
        if (value == null) { writeInt(-1); return this }
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeInt(bytes.size)
        ensureCapacity(bytes.size)
        buffer.put(bytes)
        return this
    }
    fun writeBytes(value: ByteArray?): BinaryWriter {
        if (value == null) { writeInt(-1); return this }
        writeInt(value.size)
        ensureCapacity(value.size)
        buffer.put(value)
        return this
    }
    fun toByteArray(): ByteArray {
        val arr = ByteArray(buffer.position())
        buffer.flip()
        buffer.get(arr)
        return arr
    }
    fun toByteBuffer(): ByteBuffer {
        val copy = ByteBuffer.allocate(buffer.position()).order(ByteOrder.LITTLE_ENDIAN)
        buffer.flip()
        copy.put(buffer)
        copy.flip()
        return copy
    }
    fun reset() { buffer.clear() }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binary Reader - Zero-copy deserialization from ByteBuffer
// ═══════════════════════════════════════════════════════════════════════════════

class BinaryReader(private val buffer: ByteBuffer) {
    init { buffer.order(ByteOrder.LITTLE_ENDIAN) }
    constructor(bytes: ByteArray) : this(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    fun readByte(): Byte = buffer.get()
    fun readInt(): Int = buffer.getInt()
    fun readLong(): Long = buffer.getLong()
    fun readUUID(): UUID = UUID(buffer.getLong(), buffer.getLong())
    fun readString(): String? {
        val len = buffer.getInt()
        if (len < 0) return null
        val bytes = ByteArray(len)
        buffer.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }
    fun readBytes(): ByteArray? {
        val len = buffer.getInt()
        if (len < 0) return null
        val bytes = ByteArray(len)
        buffer.get(bytes)
        return bytes
    }
    fun hasRemaining(): Boolean = buffer.hasRemaining()
}

// ═══════════════════════════════════════════════════════════════════════════════
// Protocol Message Types
// ═══════════════════════════════════════════════════════════════════════════════

data class ChatMessageProto(
    val messageId: Long,
    val chatId: UUID,
    val senderId: UUID,
    val messageType: Byte,
    val content: String?,
    val mediaId: String?,
    val thumbnailUrl: String?,
    val replyToId: Long,
    val timestamp: Long,
    val editedAt: Long
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeLong(messageId)
        writer.writeUUID(chatId)
        writer.writeUUID(senderId)
        writer.writeByte(messageType)
        writer.writeString(content)
        writer.writeString(mediaId)
        writer.writeString(thumbnailUrl)
        writer.writeLong(replyToId)
        writer.writeLong(timestamp)
        writer.writeLong(editedAt)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): ChatMessageProto = ChatMessageProto(
            messageId = reader.readLong(),
            chatId = reader.readUUID(),
            senderId = reader.readUUID(),
            messageType = reader.readByte(),
            content = reader.readString(),
            mediaId = reader.readString(),
            thumbnailUrl = reader.readString(),
            replyToId = reader.readLong(),
            timestamp = reader.readLong(),
            editedAt = reader.readLong()
        )
    }
}

data class ChatListItemProto(
    val chatId: UUID,
    val chatType: Byte,
    val name: String,
    val avatarUrl: String?,
    val lastMessageContent: String?,
    val lastMessageSenderId: UUID?,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeUUID(chatId)
        writer.writeByte(chatType)
        writer.writeString(name)
        writer.writeString(avatarUrl)
        writer.writeString(lastMessageContent)
        if (lastMessageSenderId != null) { writer.writeByte(1); writer.writeUUID(lastMessageSenderId) }
        else { writer.writeByte(0) }
        writer.writeLong(lastMessageTime)
        writer.writeInt(unreadCount)
        writer.writeByte(if (isPinned) 1 else 0)
        writer.writeByte(if (isMuted) 1 else 0)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): ChatListItemProto {
            val chatId = reader.readUUID()
            val chatType = reader.readByte()
            val name = reader.readString()!!
            val avatarUrl = reader.readString()
            val lastMessageContent = reader.readString()
            val hasSender = reader.readByte() == 1.toByte()
            val senderId = if (hasSender) reader.readUUID() else null
            val lastMessageTime = reader.readLong()
            val unreadCount = reader.readInt()
            val isPinned = reader.readByte() == 1.toByte()
            val isMuted = reader.readByte() == 1.toByte()
            return ChatListItemProto(chatId, chatType, name, avatarUrl, lastMessageContent, senderId, lastMessageTime, unreadCount, isPinned, isMuted)
        }
    }
}

data class TypingEventProto(
    val chatId: UUID,
    val userId: UUID,
    val isTyping: Boolean
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeUUID(chatId)
        writer.writeUUID(userId)
        writer.writeByte(if (isTyping) 1 else 0)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): TypingEventProto = TypingEventProto(
            chatId = reader.readUUID(),
            userId = reader.readUUID(),
            isTyping = reader.readByte() == 1.toByte()
        )
    }
}

data class ReadReceiptProto(
    val chatId: UUID,
    val userId: UUID,
    val lastReadMessageId: Long,
    val timestamp: Long
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeUUID(chatId)
        writer.writeUUID(userId)
        writer.writeLong(lastReadMessageId)
        writer.writeLong(timestamp)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): ReadReceiptProto = ReadReceiptProto(
            chatId = reader.readUUID(),
            userId = reader.readUUID(),
            lastReadMessageId = reader.readLong(),
            timestamp = reader.readLong()
        )
    }
}

data class StoryItemProto(
    val storyId: Long,
    val userId: UUID,
    val mediaType: Byte,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val caption: String?,
    val timestamp: Long,
    val expiresAt: Long,
    val viewCount: Int
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeLong(storyId)
        writer.writeUUID(userId)
        writer.writeByte(mediaType)
        writer.writeString(mediaUrl)
        writer.writeString(thumbnailUrl)
        writer.writeString(caption)
        writer.writeLong(timestamp)
        writer.writeLong(expiresAt)
        writer.writeInt(viewCount)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): StoryItemProto = StoryItemProto(
            storyId = reader.readLong(),
            userId = reader.readUUID(),
            mediaType = reader.readByte(),
            mediaUrl = reader.readString(),
            thumbnailUrl = reader.readString(),
            caption = reader.readString(),
            timestamp = reader.readLong(),
            expiresAt = reader.readLong(),
            viewCount = reader.readInt()
        )
    }
}

data class CursorPageRequest(
    val chatId: UUID?,
    val cursorId: Long,
    val limit: Int
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        if (chatId != null) { writer.writeByte(1); writer.writeUUID(chatId) }
        else { writer.writeByte(0) }
        writer.writeLong(cursorId)
        writer.writeInt(limit)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): CursorPageRequest {
            val hasChatId = reader.readByte() == 1.toByte()
            val chatId = if (hasChatId) reader.readUUID() else null
            return CursorPageRequest(chatId, reader.readLong(), reader.readInt())
        }
    }
}

data class AuthRequestProto(
    val token: String
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeString(token)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): AuthRequestProto = AuthRequestProto(
            token = reader.readString()!!
        )
    }
}

data class AuthResponseProto(
    val isSuccess: Boolean,
    val userId: UUID?,
    val errorMessage: String?
) {
    fun serialize(writer: BinaryWriter): BinaryWriter {
        writer.writeByte(if (isSuccess) 1 else 0)
        if (userId != null) { writer.writeByte(1); writer.writeUUID(userId) }
        else { writer.writeByte(0) }
        writer.writeString(errorMessage)
        return writer
    }
    companion object {
        fun deserialize(reader: BinaryReader): AuthResponseProto {
            val isSuccess = reader.readByte() == 1.toByte()
            val hasUserId = reader.readByte() == 1.toByte()
            val userId = if (hasUserId) reader.readUUID() else null
            val errorMessage = reader.readString()
            return AuthResponseProto(isSuccess, userId, errorMessage)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Envelope: wraps all protocol messages
// Wire format: [opCode: 1B] [payloadLen: 4B] [payload: NB]
// ═══════════════════════════════════════════════════════════════════════════════

data class Envelope(
    val opCode: Byte,
    val payload: ByteArray
) {
    fun serialize(): ByteArray {
        val writer = BinaryWriter(1 + 4 + payload.size)
        writer.writeByte(opCode)
        writer.writeInt(payload.size)
        writer.writeBytes(payload)
        return writer.toByteArray()
    }
    companion object {
        fun deserialize(reader: BinaryReader): Envelope {
            val opCode = reader.readByte()
            val payload = reader.readBytes()!!
            return Envelope(opCode, payload)
        }
        fun deserialize(bytes: ByteArray): Envelope = deserialize(BinaryReader(bytes))
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Envelope) return false
        return opCode == other.opCode && payload.contentEquals(other.payload)
    }
    override fun hashCode(): Int = 31 * opCode.hashCode() + payload.contentHashCode()
}
