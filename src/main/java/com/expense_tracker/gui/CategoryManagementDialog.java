package com.expense_tracker.gui;

import com.expense_tracker.dao.ExpensetrackerAppDAO;
import com.expense_tracker.model.Category;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryManagementDialog extends JDialog {
    private final ExpensetrackerAppDAO dao;
    private JTable categoryTable;
    private JTextField nameField;
    
    public CategoryManagementDialog(JFrame parent, ExpensetrackerAppDAO dao) {
        super(parent, "Manage Categories", true);
        this.dao = dao;
        initializeUI();
        loadCategories();
    }
    
    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form panel for adding/editing categories
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Category Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Buttons
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addCategory());
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateCategory());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteCategory());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Table for displaying categories
        String[] columnNames = {"ID", "Category Name"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = categoryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) categoryTable.getValueAt(selectedRow, 0);
                    String name = (String) categoryTable.getValueAt(selectedRow, 1);
                    nameField.setText(name);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        
        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadCategories() {
        DefaultTableModel model = (DefaultTableModel) categoryTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            List<Category> categories = dao.getAllCategories();
            for (Category category : categories) {
                model.addRow(new Object[]{
                    category.getId(),
                    category.getName()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCategory() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a category name", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Category category = new Category(name);
            if (dao.addCategory(category)) {
                loadCategories();
                clearForm();
                JOptionPane.showMessageDialog(this, 
                    "Category added successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding category: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a category to update", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a category name", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int id = (int) categoryTable.getValueAt(selectedRow, 0);
            Category category = new Category(id, name);
            
            if (dao.updateCategory(category)) {
                loadCategories();
                clearForm();
                JOptionPane.showMessageDialog(this, 
                    "Category updated successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating category: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a category to delete", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this category?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (int) categoryTable.getValueAt(selectedRow, 0);
                if (dao.deleteCategory(id)) {
                    loadCategories();
                    clearForm();
                    JOptionPane.showMessageDialog(this, 
                        "Category deleted successfully", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting category: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        categoryTable.clearSelection();
    }
}
