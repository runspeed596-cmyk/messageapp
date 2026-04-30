package com.Kelasor.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.Kelasor.app.data.cache.RamCacheManager
import com.Kelasor.app.data.local.dao.CachedMessageDao
import com.Kelasor.app.data.local.dao.PaginationRemoteKeyDao
import com.Kelasor.app.data.local.entity.CachedMessageEntity
import com.Kelasor.app.data.local.entity.PaginationRemoteKey
import com.Kelasor.app.data.websocket.BinaryReader
import com.Kelasor.app.data.websocket.BinaryWsClient
import com.Kelasor.app.data.websocket.ChatMessageProto
import com.Kelasor.app.data.websocket.OpCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

// ═══════════════════════════════════════════════════════════════════════════════
// 🔄 Message Remote Mediator - Paging 3 + Binary WebSocket
// Flow: RAM Cache → Room DB → WebSocket (via server cache: Redis → PostgreSQL)
// Cursor-based: always uses messageId as cursor, NEVER offset
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val chatId: String,
    private val wsClient: BinaryWsClient,
    private val messageDao: CachedMessageDao,
    private val remoteKeyDao: PaginationRemoteKeyDao,
    private val ramCache: RamCacheManager
) : RemoteMediator<Int, CachedMessageEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CachedMessageEntity>
    ): MediatorResult {
        val cursorId: Long = when (loadType) {
            LoadType.REFRESH -> 0L
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey: PaginationRemoteKey? = remoteKeyDao.getRemoteKey("message", chatId)
                remoteKey?.nextCursor ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }
        return try {
            val pageSize: Int = state.config.pageSize
            val chatUUID: java.util.UUID = java.util.UUID.fromString(chatId)
            wsClient.requestMessages(chatUUID, cursorId, pageSize)
            val messages: List<ChatMessageProto>? = withTimeoutOrNull(5000L) {
                var result: List<ChatMessageProto>? = null
                wsClient.incomingMessages.collect { envelope ->
                    if (envelope.opCode == OpCode.MESSAGE_LIST_RESPONSE) {
                        val reader = BinaryReader(envelope.payload)
                        val count: Int = reader.readInt()
                        val list = mutableListOf<ChatMessageProto>()
                        repeat(count) { list.add(ChatMessageProto.deserialize(reader)) }
                        result = list
                        return@collect
                    }
                }
                result
            }
            if (messages == null) return MediatorResult.Error(Exception("Timeout fetching messages"))
            if (loadType == LoadType.REFRESH) {
                messageDao.clearChatMessages(chatId)
            }
            val entities: List<CachedMessageEntity> = messages.map { msg ->
                CachedMessageEntity(
                    id = msg.messageId,
                    chatId = msg.chatId.toString(),
                    senderId = msg.senderId.toString(),
                    messageType = msg.messageType.toInt(),
                    content = msg.content,
                    mediaId = msg.mediaId,
                    thumbnailUrl = msg.thumbnailUrl,
                    replyToId = if (msg.replyToId > 0) msg.replyToId else null,
                    timestamp = msg.timestamp,
                    editedAt = if (msg.editedAt > 0) msg.editedAt else null
                )
            }
            messageDao.insertMessages(entities)
            ramCache.setMessages(chatUUID, messages)
            val nextCursor: Long? = if (messages.size < pageSize) null else messages.lastOrNull()?.messageId
            remoteKeyDao.insertRemoteKey(
                PaginationRemoteKey(
                    entityType = "message",
                    entityId = chatId,
                    nextCursor = nextCursor,
                    prevCursor = null
                )
            )
            MediatorResult.Success(endOfPaginationReached = messages.size < pageSize)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
