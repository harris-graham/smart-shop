-- Create wishlist table
CREATE TABLE IF NOT EXISTS wishlist (
    WishlistID SERIAL PRIMARY KEY,
    CustID INTEGER NOT NULL,
    SmartID VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CustID) REFERENCES customer(CustID) ON DELETE CASCADE,
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE,
    UNIQUE(CustID, SmartID)
);

-- Create indexes
CREATE INDEX idx_wishlist_customer ON wishlist(CustID);
CREATE INDEX idx_wishlist_smartphone ON wishlist(SmartID);
