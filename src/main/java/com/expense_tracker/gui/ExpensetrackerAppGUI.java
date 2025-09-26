package com.expense_tracker.gui;

import com.expense_tracker.dao.ExpensetrackerAppDAO;
import com.expense_tracker.model.Transaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ExpensetrackerAppGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ExpensetrackerAppDAO dao;
    private JTable transactionTable;
    private JButton addExpenseButton;
    private JButton addIncomeButton;
    private JButton viewReportButton;
    private JPanel incomePanel;
    private JPanel expensePanel;
    private JPanel balancePanel;
    
    public ExpensetrackerAppGUI(ExpensetrackerAppDAO dao) {
        this.dao = dao;
        initializeUI();
        setupLayout();
        setupEventListeners();
        refreshData();
    }
    
    private void initializeUI() {
        setTitle("Expense Tracker");
        setSize(1000, 700);
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
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Expense Tracker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addExpenseButton = new JButton("Add Expense");
        addIncomeButton = new JButton("Add Income");
        viewReportButton = new JButton("View Reports");
        
        buttonPanel.add(addExpenseButton);
        buttonPanel.add(addIncomeButton);
        buttonPanel.add(viewReportButton);
        
        // Table for transactions
        String[] columnNames = {"ID", "Date", "Type", "Category", "Amount", "Description"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        incomePanel = createSummaryPanel("Total Income", "₹0.00", Color.GREEN.darker());
        expensePanel = createSummaryPanel("Total Expenses", "₹0.00", Color.RED.darker());
        balancePanel = createSummaryPanel("Balance", "₹0.00", Color.BLUE.darker());
        
        summaryPanel.add(incomePanel);
        summaryPanel.add(expensePanel);
        summaryPanel.add(balancePanel);
        
        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createSummaryPanel(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(color);
        valueLabel.setName(title.toLowerCase().replace(" ", "") + "Label");
        
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private void setupEventListeners() {
        addExpenseButton.addActionListener(e -> showAddTransactionDialog("EXPENSE"));
        addIncomeButton.addActionListener(e -> showAddTransactionDialog("INCOME"));
        viewReportButton.addActionListener(e -> showReportsDialog());
    }
    
    private void showAddTransactionDialog(String type) {
        JDialog dialog = new JDialog(this, "Add " + type, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        panel.add(new JLabel("Amount:"));
        JTextField amountField = new JTextField();
        panel.add(amountField);
        
        panel.add(new JLabel("Category:"));
        String[] categories = type.equals("EXPENSE") ? 
            new String[]{"Food", "Transport", "Shopping", "Bills", "Entertainment"} :
            new String[]{"Salary", "Freelance", "Gift", "Other Income"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        panel.add(categoryCombo);
        
        panel.add(new JLabel("Date:"));
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        panel.add(dateField);
        
        panel.add(new JLabel("Description:"));
        JTextField descField = new JTextField();
        panel.add(descField);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                Transaction transaction = new Transaction(
                    java.time.LocalDate.parse(dateField.getText()),
                    Transaction.TransactionType.valueOf(type),
                    (String) categoryCombo.getSelectedItem(),
                    Double.parseDouble(amountField.getText()),
                    descField.getText()
                );
                
                if (dao.addTransaction(transaction)) {
                    JOptionPane.showMessageDialog(dialog, type + " added successfully!");
                    refreshData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add " + type, 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        panel.add(new JLabel()); // Empty cell for layout
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showReportsDialog() {
        JDialog dialog = new JDialog(this, "Expense Reports", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Monthly Summary Tab
        JPanel monthlyPanel = new JPanel(new BorderLayout());
        // TODO: Add monthly summary chart/table
        tabbedPane.addTab("Monthly Summary", monthlyPanel);
        
        // Category-wise Tab
        JPanel categoryPanel = new JPanel(new BorderLayout());
        // TODO: Add category-wise breakdown
        tabbedPane.addTab("Category-wise", categoryPanel);
        
        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }
    
    private void refreshData() {
        // Refresh transactions table
        DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        List<Transaction> transactions = dao.getAllTransactions();
        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                t.getId(),
                t.getDate(),
                t.getType(),
                t.getCategory(),
                String.format("₹%.2f", t.getAmount()),
                t.getDescription()
            });
        }
        
        // Refresh summary
        double income = dao.getTotalIncome();
        double expenses = dao.getTotalExpenses();
        double balance = income - expenses;
        
        updateSummaryPanel(incomePanel, income, Color.GREEN.darker());
        updateSummaryPanel(expensePanel, expenses, Color.RED.darker());
        updateSummaryPanel(balancePanel, balance, 
            balance >= 0 ? Color.BLUE.darker() : Color.RED.darker());
    }
    
    private void updateSummaryPanel(JPanel panel, double value, Color color) {
        JLabel label = (JLabel) ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (label != null) {
            label.setText(String.format("₹%.2f", value));
            label.setForeground(color);
        }
    }
}
