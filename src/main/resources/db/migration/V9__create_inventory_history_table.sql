-- Create inventory_history table for tracking stock changes
CREATE TABLE IF NOT EXISTS inventory_history (
    HistoryID SERIAL PRIMARY KEY,
    SmartID VARCHAR(50) NOT NULL,
    QuantityChange INTEGER NOT NULL,
    PreviousQty INTEGER NOT NULL,
    NewQty INTEGER NOT NULL,
    ChangeType VARCHAR(20) NOT NULL, -- 'SALE', 'RESTOCK', 'ADJUSTMENT'
    ReferenceID INTEGER,
    Notes TEXT,
    ChangedBy INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE,
    FOREIGN KEY (ChangedBy) REFERENCES employee(EmpID) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_inventory_smartphone ON inventory_history(SmartID);
CREATE INDEX idx_inventory_date ON inventory_history(created_at);
CREATE INDEX idx_inventory_type ON inventory_history(ChangeType);
