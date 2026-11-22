-- Create order table (renamed from sale to avoid SQL keyword conflict)
CREATE TABLE IF NOT EXISTS shop_order (
    OrderID SERIAL PRIMARY KEY,
    OrderNumber VARCHAR(50) UNIQUE NOT NULL,
    CustID INTEGER NOT NULL,
    EmpID INTEGER NOT NULL,
    TotalAmount DECIMAL(10, 2) NOT NULL,
    Status VARCHAR(20) DEFAULT 'PENDING',
    PaymentStatus VARCHAR(20) DEFAULT 'PENDING',
    PaymentID VARCHAR(100),
    OrderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ShippedDate TIMESTAMP,
    DeliveredDate TIMESTAMP,
    Notes TEXT,
    FOREIGN KEY (CustID) REFERENCES customer(CustID) ON DELETE CASCADE,
    FOREIGN KEY (EmpID) REFERENCES employee(EmpID) ON DELETE CASCADE
);

-- Create order_item table
CREATE TABLE IF NOT EXISTS order_item (
    OrderItemID SERIAL PRIMARY KEY,
    OrderID INTEGER NOT NULL,
    SmartID VARCHAR(50) NOT NULL,
    Quantity INTEGER NOT NULL,
    UnitPrice DECIMAL(10, 2) NOT NULL,
    TotalPrice DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (OrderID) REFERENCES shop_order(OrderID) ON DELETE CASCADE,
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_order_customer ON shop_order(CustID);
CREATE INDEX idx_order_employee ON shop_order(EmpID);
CREATE INDEX idx_order_status ON shop_order(Status);
CREATE INDEX idx_order_date ON shop_order(OrderDate);
CREATE INDEX idx_order_item_order ON order_item(OrderID);
