-- Fix iran_rank column to allow NULL values
ALTER TABLE universities ALTER COLUMN iran_rank DROP NOT NULL;

-- Rename scientific_rank_iran to iran_rank if it exists with wrong name
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'universities' AND column_name = 'scientific_rank_iran') THEN
        -- Column exists with new name, nothing to do for renaming
        NULL;
    END IF;
END $$;

-- Add missing columns to elm_events table if they don't exist
ALTER TABLE elm_events ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT FALSE;
ALTER TABLE elm_events ADD COLUMN IF NOT EXISTS submitted_by_user_id UUID;

-- Change expiryDate column type from timestamp to date for discounts if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'discounts' AND column_name = 'expiry_date' 
               AND data_type = 'timestamp with time zone') THEN
        ALTER TABLE discounts ALTER COLUMN expiry_date TYPE DATE USING expiry_date::DATE;
    END IF;
END $$;
