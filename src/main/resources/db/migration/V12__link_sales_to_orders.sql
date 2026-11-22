-- Link completed sales back to the order that produced them so that
-- cancelling an order can also reverse the reported revenue.
ALTER TABLE sale ADD COLUMN IF NOT EXISTS OrderID INTEGER;

CREATE INDEX IF NOT EXISTS idx_sale_order ON sale(OrderID);
