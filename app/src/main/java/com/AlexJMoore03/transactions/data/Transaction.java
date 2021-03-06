package com.AlexJMoore03.transactions.data;

import java.util.Date;

public class Transaction implements Comparable<Transaction> {
    private Category category;
    private double amount;
    private String name;
    private Date date;

    public Transaction (double amount_, Date date_) {
        amount = amount_;
        date = date_;
        name = "Unnamed Transaction";
    }
    public Transaction (double amount_, Date date_, Category category_, String name_) {
        amount = amount_;
        date = date_;
        category = category_;
        name = name_;
    }

    public Category getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public void setCategory(Category category_) {
        category = category_;
    }

    public void setAmount(double amount_) {
        amount = amount_;
    }

    public void setName(String name_) {
        name = name_;
    }

    public void setDate(Date date_) {
        date = date_;
    }

    @Override
    public int compareTo(Transaction other) {
        return date.compareTo(other.getDate()) * -1;
    }
}
