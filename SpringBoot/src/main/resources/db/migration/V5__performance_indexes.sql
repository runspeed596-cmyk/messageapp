-- ═══════════════════════════════════════════════════════════════════════════════
-- V5: Performance Indexes for High-Performance Messaging
-- Cursor-based pagination, chat list ordering, story listing
-- NEVER drops existing tables — additive only
-- ═══════════════════════════════════════════════════════════════════════════════

-- Messages: cursor pagination by chat_id + message_id DESC
CREATE INDEX IF NOT EXISTS idx_messages_chat_cursor
    ON messages (chat_id, id DESC);

-- Messages: time-based ordering within a chat
CREATE INDEX IF NOT EXISTS idx_messages_chat_time
    ON messages (chat_id, created_at DESC);

-- Messages: sender lookup for read receipts
CREATE INDEX IF NOT EXISTS idx_messages_sender
    ON messages (sender_id, created_at DESC);

-- Chats: order by last message time for chat list
CREATE INDEX IF NOT EXISTS idx_chats_last_msg_time
    ON chats (last_message_time DESC);

-- Chats: user's chats lookup
CREATE INDEX IF NOT EXISTS idx_chat_participants_user
    ON chat_participants (user_id, chat_id);

-- Group messages: cursor pagination
CREATE INDEX IF NOT EXISTS idx_group_messages_cursor
    ON group_messages (group_id, id DESC);

-- Group messages: time-based
CREATE INDEX IF NOT EXISTS idx_group_messages_time
    ON group_messages (group_id, created_at DESC);

-- Channel posts: cursor pagination
CREATE INDEX IF NOT EXISTS idx_channel_posts_cursor
    ON channel_posts (channel_id, id DESC);

-- Stories: user's active stories
CREATE INDEX IF NOT EXISTS idx_stories_user_time
    ON stories (user_id, created_at DESC);

-- Stories: expiration cleanup
CREATE INDEX IF NOT EXISTS idx_stories_expires
    ON stories (expires_at);

-- Group members: fan-out lookup
CREATE INDEX IF NOT EXISTS idx_group_members_group
    ON group_members (group_id, user_id);

-- Channel subscribers: fan-out lookup
CREATE INDEX IF NOT EXISTS idx_channel_subs_channel
    ON channel_subscribers (channel_id, user_id);
