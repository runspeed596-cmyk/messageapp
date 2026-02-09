-- Migration script to add new columns for Features 3, 4, 6, and 7
-- Run this script in PostgreSQL before restarting the backend

-- Feature 3 & 4: Add bio channels and premium status to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio_channel_id1 UUID NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio_channel_id2 UUID NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_premium BOOLEAN DEFAULT FALSE;
UPDATE users SET is_premium = FALSE WHERE is_premium IS NULL;
ALTER TABLE users ALTER COLUMN is_premium SET NOT NULL;

-- Feature 6: Add admin permissions to group_members table
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS can_edit_info BOOLEAN DEFAULT FALSE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS can_post_story BOOLEAN DEFAULT FALSE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS can_add_members BOOLEAN DEFAULT FALSE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS can_remove_members BOOLEAN DEFAULT FALSE;
UPDATE group_members SET can_edit_info = FALSE WHERE can_edit_info IS NULL;
UPDATE group_members SET can_post_story = FALSE WHERE can_post_story IS NULL;
UPDATE group_members SET can_add_members = FALSE WHERE can_add_members IS NULL;
UPDATE group_members SET can_remove_members = FALSE WHERE can_remove_members IS NULL;
ALTER TABLE group_members ALTER COLUMN can_edit_info SET NOT NULL;
ALTER TABLE group_members ALTER COLUMN can_post_story SET NOT NULL;
ALTER TABLE group_members ALTER COLUMN can_add_members SET NOT NULL;
ALTER TABLE group_members ALTER COLUMN can_remove_members SET NOT NULL;

-- Feature 6: Add admin permissions to channel_subscribers table
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS can_edit_info BOOLEAN DEFAULT FALSE;
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS can_post_story BOOLEAN DEFAULT FALSE;
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS can_add_members BOOLEAN DEFAULT FALSE;
ALTER TABLE channel_subscribers ADD COLUMN IF NOT EXISTS can_remove_members BOOLEAN DEFAULT FALSE;
UPDATE channel_subscribers SET can_edit_info = FALSE WHERE can_edit_info IS NULL;
UPDATE channel_subscribers SET can_post_story = FALSE WHERE can_post_story IS NULL;
UPDATE channel_subscribers SET can_add_members = FALSE WHERE can_add_members IS NULL;
UPDATE channel_subscribers SET can_remove_members = FALSE WHERE can_remove_members IS NULL;
ALTER TABLE channel_subscribers ALTER COLUMN can_edit_info SET NOT NULL;
ALTER TABLE channel_subscribers ALTER COLUMN can_post_story SET NOT NULL;
ALTER TABLE channel_subscribers ALTER COLUMN can_add_members SET NOT NULL;
ALTER TABLE channel_subscribers ALTER COLUMN can_remove_members SET NOT NULL;

-- Feature 7: Add channel/group references to stories table
ALTER TABLE stories ADD COLUMN IF NOT EXISTS channel_id UUID NULL;
ALTER TABLE stories ADD COLUMN IF NOT EXISTS group_id UUID NULL;
ALTER TABLE stories ADD CONSTRAINT fk_stories_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE SET NULL;
ALTER TABLE stories ADD CONSTRAINT fk_stories_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL;

-- Done! Restart the backend after running this script.
