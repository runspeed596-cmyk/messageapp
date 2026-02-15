-- Fix: Add SCHEDULED to the allowed values for messages.status CHECK constraint
-- The constraint was auto-created by Hibernate before SCHEDULED was added to the enum

-- Drop the old constraint if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'messages_status_check' AND table_name = 'messages'
    ) THEN
        ALTER TABLE messages DROP CONSTRAINT messages_status_check;
    END IF;
END $$;

-- Recreate the constraint with SCHEDULED included
ALTER TABLE messages ADD CONSTRAINT messages_status_check
    CHECK (status IN ('SENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'SCHEDULED'));
