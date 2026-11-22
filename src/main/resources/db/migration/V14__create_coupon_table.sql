-- Promotions: coupon codes that can be applied at checkout.
CREATE TABLE IF NOT EXISTS coupon (
    CouponID SERIAL PRIMARY KEY,
    Code VARCHAR(30) NOT NULL UNIQUE,
    Description VARCHAR(120),
    DiscountType VARCHAR(10) NOT NULL DEFAULT 'PERCENT', -- PERCENT or FIXED
    DiscountValue DECIMAL(10, 2) NOT NULL,
    MinOrderAmount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    Active BOOLEAN NOT NULL DEFAULT TRUE,
    ExpiryDate DATE,
    UsageCount INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_coupon_active ON coupon(Active);

-- Coupons applied to an order, so the discount stays visible after checkout.
ALTER TABLE shop_order ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(30);
ALTER TABLE shop_order ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0;