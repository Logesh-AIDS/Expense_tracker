package com.expense_tracker.model;

import java.time.LocalDate;
import java.util.Objects;

public class Transaction {
    public enum TransactionType {
        INCOME, EXPENSE
    }

    private int id;
    private LocalDate date;
    private TransactionType type;
    private String category;
    private double amount;
    private String description;

    // Constructor with ID (for existing transactions)
    public Transaction(int id, LocalDate date, TransactionType type, String category, double amount, String description) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    // Constructor without ID (for new transactions)
    public Transaction(LocalDate date, TransactionType type, String category, double amount, String description) {
        this(-1, date, type, category, amount, description);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id && 
               Double.compare(that.amount, amount) == 0 &&
               Objects.equals(date, that.date) &&
               type == that.type &&
               Objects.equals(category, that.category) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, type, category, amount, description);
    }

    @Override
    public String toString() {
        return String.format(
            "Transaction{id=%d, date=%s, type=%s, category='%s', amount=%.2f, description='%s'}",
            id, date, type, category, amount, description
        );
    }
}
