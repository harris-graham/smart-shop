-- Create review table for product reviews
CREATE TABLE IF NOT EXISTS review (
    ReviewID SERIAL PRIMARY KEY,
    SmartID VARCHAR(50) NOT NULL,
    CustID INTEGER NOT NULL,
    Rating INTEGER NOT NULL CHECK (Rating >= 1 AND Rating <= 5),
    Comment TEXT,
    Status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (SmartID) REFERENCES smartphone(SmartID) ON DELETE CASCADE,
    FOREIGN KEY (CustID) REFERENCES customer(CustID) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_review_smartphone ON review(SmartID);
CREATE INDEX idx_review_customer ON review(CustID);
CREATE INDEX idx_review_rating ON review(Rating);
CREATE INDEX idx_review_status ON review(Status);
