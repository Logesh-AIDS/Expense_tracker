package com.expense_tracker.gui;

import com.expense_tracker.dao.ExpensetrackerAppDAO;
import com.expense_tracker.model.Category;
import com.expense_tracker.model.Expense;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseManagementDialog extends JDialog {
    private final ExpensetrackerAppDAO dao;
    private JTable expenseTable;
    private JTextField nameField;
    private JComboBox<Category> categoryCombo;
    private JFormattedTextField amountField;
    private JTextArea descriptionArea;
    private JFormattedTextField dateField;
    
    public ExpenseManagementDialog(JFrame parent, ExpensetrackerAppDAO dao) {
        super(parent, "Manage Expenses", true);
        this.dao = dao;
        initializeUI();
        loadExpenses();
        loadCategories();
    }
    
    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form panel for adding/editing expenses
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Expense Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Category combo
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        categoryCombo = new JComboBox<>();
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                         boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });
        formPanel.add(categoryCombo, gbc);
        
        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        amountField = new JFormattedTextField(java.text.NumberFormat.getNumberInstance());
        amountField.setColumns(10);
        formPanel.add(amountField, gbc);
        
        // Date field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        dateField = new JFormattedTextField(java.text.DateFormat.getDateInstance());
        dateField.setValue(java.sql.Date.valueOf(LocalDate.now()));
        formPanel.add(dateField, gbc);
        
        // Description area
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Buttons
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addExpense());
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateExpense());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteExpense());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Table for displaying expenses
        String[] columnNames = {"ID", "Name", "Category", "Amount", "Date", "Description"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        expenseTable = new JTable(tableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = expenseTable.getSelectedRow();
                if (selectedRow >= 0) {
                    loadExpenseToForm(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        
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
        try {
            List<Category> categories = dao.getAllCategories();
            categoryCombo.removeAllItems();
            for (Category category : categories) {
                categoryCombo.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadExpenses() {
        DefaultTableModel model = (DefaultTableModel) expenseTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            List<Expense> expenses = dao.getAllExpenses();
            for (Expense expense : expenses) {
                model.addRow(new Object[]{
                    expense.getId(),
                    expense.getName(),
                    expense.getCategoryName(),
                    String.format("%.2f", expense.getAmount()),
                    expense.getDate(),
                    expense.getDescription()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading expenses: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadExpenseToForm(int rowIndex) {
        try {
            int id = (int) expenseTable.getValueAt(rowIndex, 0);
            Expense expense = dao.getExpenseById(id);
            
            if (expense != null) {
                nameField.setText(expense.getName());
                
                // Set the selected category in the combo box
                for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                    Category category = categoryCombo.getItemAt(i);
                    if (category.getId() == expense.getCategoryId()) {
                        categoryCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                amountField.setValue(expense.getAmount());
                dateField.setValue(java.sql.Date.valueOf(expense.getDate()));
                descriptionArea.setText(expense.getDescription());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading expense details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addExpense() {
        try {
            Expense expense = createExpenseFromForm();
            if (expense == null) return;
            
            if (dao.addExpense(expense)) {
                loadExpenses();
                clearForm();
                JOptionPane.showMessageDialog(this, 
                    "Expense added successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding expense: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select an expense to update", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int id = (int) expenseTable.getValueAt(selectedRow, 0);
            Expense expense = createExpenseFromForm();
            if (expense == null) return;
            
            expense.setId(id);
            
            if (dao.updateExpense(expense)) {
                loadExpenses();
                clearForm();
                JOptionPane.showMessageDialog(this, 
                    "Expense updated successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating expense: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select an expense to delete", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this expense?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = (int) expenseTable.getValueAt(selectedRow, 0);
                if (dao.deleteExpense(id)) {
                    loadExpenses();
                    clearForm();
                    JOptionPane.showMessageDialog(this, 
                        "Expense deleted successfully", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting expense: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private Expense createExpenseFromForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter an expense name", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        Category selectedCategory = (Category) categoryCombo.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a category", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid amount", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        LocalDate date;
        try {
            date = ((java.sql.Date) dateField.getValue()).toLocalDate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid date", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        String description = descriptionArea.getText().trim();
        
        Expense expense = new Expense();
        expense.setName(name);
        expense.setCategoryId(selectedCategory.getId());
        expense.setCategoryName(selectedCategory.getName());
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setDescription(description);
        
        return expense;
    }
    
    private void clearForm() {
        nameField.setText("");
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        amountField.setValue(null);
        dateField.setValue(java.sql.Date.valueOf(LocalDate.now()));
        descriptionArea.setText("");
        expenseTable.clearSelection();
    }
}
