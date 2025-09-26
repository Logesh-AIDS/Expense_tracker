package com.expense_tracker.model;

public class Category {
    private int id;
    private String name;
    
    public Category() {
    }
    
    public Category(String name) {
        this.name = name;
    }
    
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
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
    
    @Override
    public String toString() {
        return name;
    }
}
