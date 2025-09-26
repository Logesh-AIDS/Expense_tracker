package com.expense_tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    
    public Expense() {
    }
    
    public Expense(String name, int categoryId, BigDecimal amount, String description, LocalDate date) {
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return String.format("Expense{id=%d, name='%s', category='%s', amount=%.2f, date=%s}",
            id, name, categoryName, amount, date);
    }
}