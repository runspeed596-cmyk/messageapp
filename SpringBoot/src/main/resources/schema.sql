-- Force add columns if they are missing
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT FALSE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS is_muted BOOLEAN DEFAULT FALSE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT FALSE;

ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT FALSE;
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS is_muted BOOLEAN DEFAULT FALSE;
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT FALSE;

ALTER TABLE chats ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT FALSE;
ALTER TABLE chats ADD COLUMN IF NOT EXISTS is_muted BOOLEAN DEFAULT FALSE;
ALTER TABLE chats ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT FALSE;

-- Add missing columns to elm_events table with nullable first
ALTER TABLE elm_events ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT FALSE;
ALTER TABLE elm_events ADD COLUMN IF NOT EXISTS submitted_by_user_id UUID;

-- Update any existing null values to default
UPDATE elm_events SET is_approved = FALSE WHERE is_approved IS NULL;

-- Fix university rank columns
ALTER TABLE universities ALTER COLUMN iran_rank DROP NOT NULL;
ALTER TABLE universities ALTER COLUMN world_rank DROP NOT NULL;
