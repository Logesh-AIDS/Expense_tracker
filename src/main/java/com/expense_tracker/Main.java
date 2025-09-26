package com.expense_tracker;

import com.expense_tracker.dao.ExpensetrackerAppDAO;
import com.expense_tracker.gui.MainFrame;
import com.expense_tracker.util.DatabaseConnection;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Main class for the Expense Tracker application.
 * This class initializes the database connection and launches the GUI.
 */
public class Main {
    
    public static void main(String[] args) {
        // Initialize database connection
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
            
            // Launch the application with MainFrame
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(dao);
                mainFrame.setVisible(true);
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error connecting to the database: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "An unexpected error occurred: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
