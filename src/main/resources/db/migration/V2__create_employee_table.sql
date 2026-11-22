-- Create employee table
CREATE TABLE IF NOT EXISTS employee (
    EmpID SERIAL PRIMARY KEY,
    EmpName VARCHAR(60) NOT NULL,
    EmpPNum VARCHAR(20),
    EmpAddress VARCHAR(100),
    EmpUName VARCHAR(20) UNIQUE NOT NULL,
    EmpPword VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'EMPLOYEE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for username
CREATE INDEX idx_employee_username ON employee(EmpUName);

-- Insert default admin user (password: admin123 - hashed with BCrypt)
INSERT INTO employee (EmpName, EmpPNum, EmpAddress, EmpUName, EmpPword, role)
VALUES ('Admin', '0000000000', 'Admin Address', 'admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5NXyZ3O0/5HCS', 'ADMIN')
ON CONFLICT (EmpUName) DO NOTHING;
