-- Create cart table for persistent shopping cart
CREATE TABLE IF NOT EXISTS cart (
    CartID SERIAL PRIMARY KEY,
    CustID INTEGER,
    SessionID VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CustID) REFERENCES customer(CustID) ON DELETE CASCADE
);

-- Create cart_item table
CREATE TABLE IF NOT EXISTS cart_item (
    CartItemID SERIAL PRIMARY KEY,
    CartID INTEGER NOT NULL,
    SmartID VARCHAR(50) NOT NULL,
    Quantity INTEGER NOT NULL DEFAULT 1,
    PriceAtAdd DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CartID) REFERENCES cart(CartID) ON DELETE CASCADE,
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_cart_customer ON cart(CustID);
CREATE INDEX idx_cart_session ON cart(SessionID);
CREATE INDEX idx_cart_item_cart ON cart_item(CartID);
