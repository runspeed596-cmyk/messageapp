-- V4: Convert showInSpecial boolean to displayMode enum (VARCHAR)
-- Groups table
ALTER TABLE groups ADD COLUMN IF NOT EXISTS display_mode VARCHAR(20) DEFAULT 'SPECIAL';
UPDATE groups SET display_mode = CASE WHEN show_in_special = true THEN 'SPECIAL' ELSE 'TAB' END WHERE display_mode IS NULL OR display_mode = 'SPECIAL';
ALTER TABLE groups DROP COLUMN IF EXISTS show_in_special;

-- Channels table
ALTER TABLE channels ADD COLUMN IF NOT EXISTS display_mode VARCHAR(20) DEFAULT 'SPECIAL';
UPDATE channels SET display_mode = CASE WHEN show_in_special = true THEN 'SPECIAL' ELSE 'TAB' END WHERE display_mode IS NULL OR display_mode = 'SPECIAL';
ALTER TABLE channels DROP COLUMN IF EXISTS show_in_special;
