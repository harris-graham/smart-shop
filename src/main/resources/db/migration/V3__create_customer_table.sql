-- Create customer table
CREATE TABLE IF NOT EXISTS customer (
    CustID SERIAL PRIMARY KEY,
    CustName VARCHAR(60) NOT NULL,
    CustPNum VARCHAR(20),
    CustAddress VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for email
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_name ON customer(CustName);
