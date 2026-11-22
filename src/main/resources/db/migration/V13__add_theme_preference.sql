-- Store the light/dark theme preference on both account types.
-- Anonymous visitors keep their choice in a cookie; signed-in users also get
-- it persisted here so it follows them to other devices.
ALTER TABLE customer ADD COLUMN IF NOT EXISTS theme VARCHAR(10) DEFAULT 'light';
ALTER TABLE employee ADD COLUMN IF NOT EXISTS theme VARCHAR(10) DEFAULT 'light';

CREATE INDEX IF NOT EXISTS idx_inventory_history_smartid
    ON inventory_history (SmartID, created_at DESC);