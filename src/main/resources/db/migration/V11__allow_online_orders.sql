-- Online orders are placed by customers without a staff member attached.
-- Make the employee reference optional for both orders and sales so that
-- self-service checkouts can be stored.
ALTER TABLE shop_order ALTER COLUMN EmpID DROP NOT NULL;
ALTER TABLE sale ALTER COLUMN EmpID DROP NOT NULL;
