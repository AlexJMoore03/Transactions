package com.AlexJMoore03.transactions.data;

public class Category {
    private String name;
    private double amount;
    private double budget;

    public Category(String name_, double budget_) {
        name = name_;
        amount = 0;
        budget = budget_;
    }

    public String getName() {
        return name;
    }

    public double getAmount() { return amount; }

    public double getBudget() { return budget; }

    public void setName(String name_) {
        name = name_;
    }

    public void setAmount(double amount_) { amount = amount_; }

    public void setBudget(double budget_) { budget = budget_; }
}
