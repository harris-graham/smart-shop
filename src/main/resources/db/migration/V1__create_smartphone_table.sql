-- Create smartphone table
CREATE TABLE IF NOT EXISTS smartphone (
    SmartID VARCHAR(50) PRIMARY KEY,
    SmartName VARCHAR(50) NOT NULL,
    SmartPrice DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    SmartQty INTEGER NOT NULL DEFAULT 0,
    SmartRAM INTEGER NOT NULL DEFAULT 0,
    SmartStorage INTEGER NOT NULL DEFAULT 0,
    SmartImage TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for search
CREATE INDEX idx_smartphone_name ON smartphone(SmartName);
CREATE INDEX idx_smartphone_price ON smartphone(SmartPrice);
