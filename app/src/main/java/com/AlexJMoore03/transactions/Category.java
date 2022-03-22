package com.AlexJMoore03.transactions;

public class Category {
    private String name;
    private double amount;

    public Category(String name_) {
        name = name_;
        amount = 0;
    }

    public String getName() {
        return name;
    }

    public double getAmount() { return amount; }

    public void setName(String name_) {
        name = name_;
    }

    public void setAmount(double amount_) { amount = amount_; }
}
