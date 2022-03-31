package com.AlexJMoore03.transactions.util;

import android.content.Context;
import android.widget.Toast;

import com.AlexJMoore03.transactions.data.Category;
import com.AlexJMoore03.transactions.ui.MainActivity;
import com.AlexJMoore03.transactions.data.Transaction;
import com.AlexJMoore03.transactions.ui.MainViewModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FileHandler {
    //Saves transactions, filters, categories, etc. to JSON
    public static void saveData(Context context) {
        for (File child : context.getFilesDir().listFiles()) {
            child.delete();
        }
        ArrayList<JSONObject> data = new ArrayList<>();
        for (Category category : MainViewModel.categoryList) {
            JSONObject categoryData = new JSONObject();
            try {
                categoryData.put("File Type", "Category Data");
                categoryData.put("Name", category.getName());
            } catch (Exception e) {}
            data.add(categoryData);
        }
        for (Transaction transaction : MainViewModel.transactionList) {
            JSONObject transactionData = new JSONObject();
            try {
                transactionData.put("File Type", "Transaction Data");
                transactionData.put("Name", transaction.getName());
                transactionData.put("Category", transaction.getCategory().getName());
                transactionData.put("Amount", String.valueOf(transaction.getAmount()));
                transactionData.put("Date", (transaction.getDate().getMonth() + 1) + "-" + transaction.getDate().getDate() + "-" + (transaction.getDate().getYear() + 1900));
            } catch (Exception e) {}
            data.add(transactionData);
        }
        JSONObject filterData = new JSONObject();
        try {
            filterData.put("File Type", "Filter Data");
            filterData.put("Date Filter", MainViewModel.dateSetting);
            filterData.put("Category Filter", MainViewModel.categorySetting);
        } catch (Exception e) {
            Toast toast = Toast.makeText(context, "Error saving filter data",(short)3);
            toast.show();
        }
        data.add(filterData);
        for (int i = 0; i < data.size(); i++) {
            try {
                File file = new File(context.getFilesDir(),"file" + i);
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(data.get(i).toString());
                bufferedWriter.close();
            } catch (Exception e) {}
        }
    }

    //Loads transactions, filters, categories, etc. Returns false if there is no data to load
    public static boolean loadData(Context context) {
        int i = 0;
        File file = new File(context.getFilesDir(),"file" + i);
        if (!file.exists()) {
            return false;
        }
        while (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                readFile(new JSONObject(stringBuilder.toString()));
            } catch (Exception e) {}
            i++;
            file = new File(context.getFilesDir(), "file" + i);
        }
        return true;
    }

    //Reads file data and determines what to do
    public static void readFile(JSONObject data) {
        try {
            if (data.get("File Type").equals("Category Data")) {
                Category newCategory = new Category(data.get("Name").toString());
                MainViewModel.categoryList.add(newCategory);
            }
            else if (data.get("File Type").equals("Transaction Data")) {
                try {
                    Category search = new Category("");
                    for (Category c : MainViewModel.categoryList) {
                        if (c.getName().equals(data.get("Category").toString())) {
                            search = c;
                            break;
                        }
                    }
                    SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
                    Date transactionDate = format.parse(data.get("Date").toString());
                    Transaction newTransaction = new Transaction(Double.valueOf(data.get("Amount").toString()), transactionDate, search, data.get("Name").toString());
                    MainViewModel.transactionList.add(newTransaction);
                } catch (Exception e) {
                }
            }
            else {
                MainViewModel.dateSetting = data.get("Date Filter").toString();
                MainViewModel.categorySetting = data.get("Category Filter").toString();
            }
        } catch (Exception e) {
        }
    }
}
