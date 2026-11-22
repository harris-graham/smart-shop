-- Create sale table
CREATE TABLE IF NOT EXISTS sale (
    SaleID SERIAL PRIMARY KEY,
    SaleQty INTEGER NOT NULL,
    SaleTotal DECIMAL(10, 2) NOT NULL,
    SmartID VARCHAR(50) NOT NULL,
    CustID INTEGER NOT NULL,
    EmpID INTEGER NOT NULL,
    SaleDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE,
    FOREIGN KEY (CustID) REFERENCES customer(CustID) ON DELETE CASCADE,
    FOREIGN KEY (EmpID) REFERENCES employee(EmpID) ON DELETE CASCADE
);

-- Create indexes for sale queries
CREATE INDEX idx_sale_date ON sale(SaleDate);
CREATE INDEX idx_sale_customer ON sale(CustID);
CREATE INDEX idx_sale_employee ON sale(EmpID);
CREATE INDEX idx_sale_smartphone ON sale(SmartID);
