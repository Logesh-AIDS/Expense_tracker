-- Drop tables if they exist (be careful with this in production)
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS categories;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- Insert some default categories
INSERT INTO categories (name) VALUES 
('Food & Dining'),
('Shopping'),
('Transportation'),
('Bills & Utilities'),
('Housing'),
('Entertainment'),
('Healthcare'),
('Education'),
('Gifts & Donations'),
('Travel'),
('Personal Care'),
('Pets'),
('Other');
