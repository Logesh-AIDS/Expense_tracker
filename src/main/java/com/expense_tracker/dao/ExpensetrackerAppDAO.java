package com.expense_tracker.dao;

import com.expense_tracker.model.Category;
import com.expense_tracker.model.Expense;
import com.expense_tracker.model.Transaction;
import com.expense_tracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpensetrackerAppDAO {
    
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS transactions (
            id INT AUTO_INCREMENT PRIMARY KEY,
            date DATE NOT NULL,
            type ENUM('INCOME', 'EXPENSE') NOT NULL,
            category VARCHAR(50) NOT NULL,
            amount DECIMAL(10, 2) NOT NULL,
            description TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

    private static final String DROP_EXPENSES_TABLE = "DROP TABLE IF EXISTS expenses";
    private static final String DROP_CATEGORIES_TABLE = "DROP TABLE IF EXISTS categories";
    private static final String DROP_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS transactions";

    private static final String CREATE_CATEGORIES_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS categories (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(50) NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )""";

    private static final String CREATE_EXPENSES_TABLE_SQL = """
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
        )""";

    public ExpensetrackerAppDAO() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Drop existing tables in the right order to avoid foreign key constraints
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            
            // Execute each DROP statement separately
            try {
                stmt.execute(DROP_EXPENSES_TABLE);
                stmt.execute(DROP_TRANSACTIONS_TABLE);
                stmt.execute(DROP_CATEGORIES_TABLE);
            } catch (SQLException e) {
                // Ignore errors if tables don't exist yet
                System.out.println("Note: Some tables didn't exist to drop: " + e.getMessage());
            }
            
            // Create tables in the right order
            stmt.execute(CREATE_CATEGORIES_TABLE_SQL);
            stmt.execute(CREATE_EXPENSES_TABLE_SQL);
            stmt.execute(CREATE_TABLE_SQL);
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
            
            // Insert default categories if they don't exist
            insertDefaultCategories(conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void insertDefaultCategories(Connection conn) throws SQLException {
        String[] defaultCategories = {
            "Food & Dining", "Shopping", "Transportation", "Bills & Utilities",
            "Housing", "Entertainment", "Healthcare", "Education",
            "Gifts & Donations", "Travel", "Personal Care", "Pets", "Other"
        };

        String checkSql = "SELECT COUNT(*) FROM categories WHERE name = ?";
        String insertSql = "INSERT IGNORE INTO categories (name) VALUES (?)";
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            
            for (String categoryName : defaultCategories) {
                checkStmt.setString(1, categoryName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertStmt.setString(1, categoryName);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // Transaction CRUD operations
    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (date, type, category, amount, description) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(transaction.getDate()));
            pstmt.setString(2, transaction.getType().toString());
            pstmt.setString(3, transaction.getCategory());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getDescription());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("id"),
                    rs.getDate("date").toLocalDate(),
                    Transaction.TransactionType.valueOf(rs.getString("type")),
                    rs.getString("category"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                );
                transactions.add(transaction);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return transactions;
    }

    public double getTotalIncome() {
        return getTransactionSum("INCOME");
    }

    public double getTotalExpenses() {
        return getTransactionSum("EXPENSE");
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    private double getTransactionSum(String type) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getDate("date").toLocalDate(),
                        Transaction.TransactionType.valueOf(rs.getString("type")),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                    );
                    transactions.add(transaction);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return transactions;
    }

    public List<Object[]> getCategoryWiseSummary() {
        List<Object[]> summary = new ArrayList<>();
        String sql = """
            SELECT type, category, SUM(amount) as total 
            FROM transactions 
            GROUP BY type, category 
            ORDER BY type, total DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("type");
                row[1] = rs.getString("category");
                row[2] = rs.getDouble("total");
                summary.add(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return summary;
    }

    // Category CRUD operations
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                categories.add(category);
            }
        }
        return categories;
    }
    
    public Category getCategoryById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setName(rs.getString("name"));
                    return category;
                }
            }
        }
        return null;
    }
    
    public boolean addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getName());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setInt(2, category.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean deleteCategory(int id) throws SQLException {
        // First, check if there are any expenses associated with this category
        String checkSql = "SELECT COUNT(*) FROM expenses WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete category: There are expenses associated with this category.");
                }
            }
        }
        
        // If no expenses are associated, proceed with deletion
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Expense CRUD operations
    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.*, c.name as category_name FROM expenses e JOIN categories c ON e.category_id = c.id ORDER BY e.date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Expense expense = new Expense();
                expense.setId(rs.getInt("id"));
                expense.setName(rs.getString("name"));
                expense.setCategoryId(rs.getInt("category_id"));
                expense.setCategoryName(rs.getString("category_name"));
                expense.setAmount(rs.getBigDecimal("amount"));
                expense.setDescription(rs.getString("description"));
                expense.setDate(rs.getDate("date").toLocalDate());
                expenses.add(expense);
            }
        }
        return expenses;
    }
    
    public Expense getExpenseById(int id) throws SQLException {
        String sql = "SELECT e.*, c.name as category_name FROM expenses e JOIN categories c ON e.category_id = c.id WHERE e.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Expense expense = new Expense();
                    expense.setId(rs.getInt("id"));
                    expense.setName(rs.getString("name"));
                    expense.setCategoryId(rs.getInt("category_id"));
                    expense.setCategoryName(rs.getString("category_name"));
                    expense.setAmount(rs.getBigDecimal("amount"));
                    expense.setDescription(rs.getString("description"));
                    expense.setDate(rs.getDate("date").toLocalDate());
                    return expense;
                }
            }
        }
        return null;
    }
    
    public boolean addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (name, category_id, amount, description, date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, expense.getName());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setBigDecimal(3, expense.getAmount());
            pstmt.setString(4, expense.getDescription());
            pstmt.setDate(5, Date.valueOf(expense.getDate()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating expense failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    expense.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("Creating expense failed, no ID obtained.");
                }
            }
        }
    }
    
    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET name = ?, category_id = ?, amount = ?, description = ?, date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, expense.getName());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setBigDecimal(3, expense.getAmount());
            pstmt.setString(4, expense.getDescription());
            pstmt.setDate(5, Date.valueOf(expense.getDate()));
            pstmt.setInt(6, expense.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Reporting methods
    public Map<String, Double> getExpensesByCategory(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Double> expensesByCategory = new HashMap<>();
        String sql = """
            SELECT c.name as category, SUM(e.amount) as total
            FROM expenses e
            JOIN categories c ON e.category_id = c.id
            WHERE e.date BETWEEN ? AND ?
            GROUP BY c.name
            ORDER BY total DESC""";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expensesByCategory.put(
                        rs.getString("category"),
                        rs.getDouble("total")
                    );
                }
            }
        }
        return expensesByCategory;
    }
    
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String sql = """
            SELECT e.*, c.name as category_name 
            FROM expenses e 
            JOIN categories c ON e.category_id = c.id 
            WHERE e.date BETWEEN ? AND ?
            ORDER BY e.date DESC, e.id DESC""";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense();
                    expense.setId(rs.getInt("id"));
                    expense.setName(rs.getString("name"));
                    expense.setCategoryId(rs.getInt("category_id"));
                    expense.setCategoryName(rs.getString("category_name"));
                    expense.setAmount(rs.getBigDecimal("amount"));
                    expense.setDescription(rs.getString("description"));
                    expense.setDate(rs.getDate("date").toLocalDate());
                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }
}
