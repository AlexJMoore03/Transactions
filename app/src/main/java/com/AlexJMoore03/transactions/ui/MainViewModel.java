package com.AlexJMoore03.transactions.ui;

import android.widget.TableLayout;

import androidx.lifecycle.ViewModel;

import com.AlexJMoore03.transactions.R;
import com.AlexJMoore03.transactions.data.Category;
import com.AlexJMoore03.transactions.data.Transaction;
import com.AlexJMoore03.transactions.util.Utilities;

import java.util.ArrayList;
import java.util.Date;

public class MainViewModel extends ViewModel {
    public static ArrayList<Transaction> transactionList;
    public static ArrayList<Category> categoryList;
    public static String dateSetting;
    public static String categorySetting;
    public MainActivity mainActivity;

    //Loads categories and displays the total spent on each one
    public void loadCategories() {
        mainActivity.emptyCategoryDisplay();
        for (int i = 0; i < categoryList.size(); i++) {
            mainActivity.displayCategory(categoryList.get(i));
        }
    }

    //Loads settings, saved transactions and categories then displays them in list according to settings
    public void loadTransactions() {
        loadTransactions(dateSetting, categorySetting);
    }
    public void loadTransactions(String dateFilter, String categoryFilter) {
        Utilities.sortTransactions(transactionList);
        //Delete previous displays & display transactions
        mainActivity.emptyTransactionDisplay();
        for (int i = 0; i < categoryList.size(); i++) {
            categoryList.get(i).setAmount(0);
        }
        Date beginning = Utilities.filterToDate(dateFilter);
        for (int i = 0; i < transactionList.size(); i++) {
            boolean display = true;
            if (beginning != null) {
                if (transactionList.get(i).getDate().before(beginning)) {
                    display = false;
                }
            }
            if (display && !categoryFilter.equals("")) {
                if (transactionList.get(i).getCategory().getName() != categoryFilter) {
                    display = false;
                }
            }
            if (display) {
                Category tCat = transactionList.get(i).getCategory();
                tCat.setAmount(tCat.getAmount() + transactionList.get(i).getAmount());
            }
        }
        loadCategories();
        for (int i = 0; i < transactionList.size(); i++) {
            boolean display = true;
            if (beginning != null) {
                if (transactionList.get(i).getDate().before(beginning)) {
                    display = false;
                }
            }
            if (display && !categoryFilter.equals("")) {
                if (transactionList.get(i).getCategory().getName() != categoryFilter) {
                    display = false;
                }
            }
            if (display) {
                mainActivity.displayTransaction(transactionList.get(i));
            }
        }
    }
}
