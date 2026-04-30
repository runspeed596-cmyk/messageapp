package com.Kelasor.app.data.cache

import android.util.LruCache
import com.Kelasor.app.data.websocket.ChatListItemProto
import com.Kelasor.app.data.websocket.ChatMessageProto
import com.Kelasor.app.data.websocket.StoryItemProto
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 🧠 In-Memory LRU Cache - Fastest layer, before Room & Network
// Chat list: 200 items, Messages per chat: 100, Stories: 50
// Priority: RAM cache → Room DB → Network (Redis → PostgreSQL)
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class RamCacheManager @Inject constructor() {
    companion object {
        private const val CHAT_LIST_SIZE: Int = 200
        private const val MESSAGES_PER_CHAT: Int = 100
        private const val MAX_CHAT_ENTRIES: Int = 50
        private const val STORIES_SIZE: Int = 50
    }

    private val chatListCache: LruCache<String, List<ChatListItemProto>> = LruCache(CHAT_LIST_SIZE)
    private val messageCache: LruCache<UUID, MutableList<ChatMessageProto>> = LruCache(MAX_CHAT_ENTRIES)
    private val storyCache: LruCache<String, List<StoryItemProto>> = LruCache(STORIES_SIZE)
    private val unreadCounts: LruCache<UUID, Int> = LruCache(CHAT_LIST_SIZE)

    // ─── Chat List ───
    fun getChatList(userId: String): List<ChatListItemProto>? = chatListCache.get("chatlist:$userId")
    fun setChatList(userId: String, items: List<ChatListItemProto>) { chatListCache.put("chatlist:$userId", items) }
    fun clearChatList() { chatListCache.evictAll() }

    // ─── Messages ───
    fun getMessages(chatId: UUID): List<ChatMessageProto>? = messageCache.get(chatId)
    fun addMessage(chatId: UUID, message: ChatMessageProto) {
        val existing: MutableList<ChatMessageProto> = messageCache.get(chatId) ?: mutableListOf()
        val isDuplicate: Boolean = existing.any { it.messageId == message.messageId }
        if (!isDuplicate) {
            existing.add(0, message)
            if (existing.size > MESSAGES_PER_CHAT) existing.removeAt(existing.lastIndex)
            messageCache.put(chatId, existing)
        }
    }
    fun setMessages(chatId: UUID, messages: List<ChatMessageProto>) {
        messageCache.put(chatId, messages.toMutableList())
    }
    fun clearMessages(chatId: UUID) { messageCache.remove(chatId) }

    // ─── Stories ───
    fun getStories(key: String): List<StoryItemProto>? = storyCache.get(key)
    fun setStories(key: String, stories: List<StoryItemProto>) { storyCache.put(key, stories) }

    // ─── Unread Counts ───
    fun getUnreadCount(chatId: UUID): Int? = unreadCounts.get(chatId)
    fun setUnreadCount(chatId: UUID, count: Int) { unreadCounts.put(chatId, count) }
    fun incrementUnread(chatId: UUID) {
        val current: Int = unreadCounts.get(chatId) ?: 0
        unreadCounts.put(chatId, current + 1)
    }
    fun resetUnread(chatId: UUID) { unreadCounts.put(chatId, 0) }

    fun clearAll() {
        chatListCache.evictAll()
        messageCache.evictAll()
        storyCache.evictAll()
        unreadCounts.evictAll()
    }
}
