-- Add credentials for the customer portal.
-- Existing customers keep a NULL password and cannot sign in until they register.
ALTER TABLE customer ADD COLUMN IF NOT EXISTS password VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_customer_email_lower ON customer(LOWER(email));
