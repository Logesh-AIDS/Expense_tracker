package com.expense_tracker.gui;

import com.expense_tracker.dao.ExpensetrackerAppDAO;
import com.expense_tracker.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ExpensetrackerAppDAO dao;
    
    public MainFrame(ExpensetrackerAppDAO dao) {
        this.dao = dao;
        initializeUI();
        setupLayout();
    }
    
    private void initializeUI() {
        setTitle("Expense Tracker");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Expense Tracker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JButton manageCategoriesBtn = new JButton("Manage Categories");
        manageCategoriesBtn.setPreferredSize(new Dimension(200, 50));
        manageCategoriesBtn.addActionListener(e -> {
            CategoryManagementDialog dialog = new CategoryManagementDialog(this, dao);
            dialog.setVisible(true);
        });
        
        JButton manageExpensesBtn = new JButton("Manage Expenses");
        manageExpensesBtn.setPreferredSize(new Dimension(200, 50));
        manageExpensesBtn.addActionListener(e -> {
            ExpenseManagementDialog dialog = new ExpenseManagementDialog(this, dao);
            dialog.setVisible(true);
        });
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(manageCategoriesBtn);
        buttonPanel.add(manageExpensesBtn);
        
        mainPanel.add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 1, 0.3, 
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            
        mainPanel.add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 1, 0.7, 
            GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        add(mainPanel);
    }
    
    public static void main(String[] args) {
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.println("Successfully connected to the database.");
            
            // Initialize DAO
            ExpensetrackerAppDAO dao = new ExpensetrackerAppDAO();
            
            // Set system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Launch the application
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame(dao);
                frame.setVisible(true);
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error connecting to the database: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
