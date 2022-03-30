package com.AlexJMoore03.transactions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ArrayList<Transaction> transactionList;
    private ArrayList<Category> categoryList;
    private String dateSetting;
    private String categorySetting;
    private Spinner dateFilterSpinner;
    private Spinner categoryFilterSpinner;
    private static final DecimalFormat moneyFormat = new DecimalFormat("0.00");

    /*
        Activity Functions
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFilterSpinner = (Spinner) findViewById(R.id.dateFilterSpinner);
        categoryFilterSpinner = (Spinner) findViewById(R.id.categoryFilterSpinner);
        transactionList = new ArrayList<>();
        categoryList = new ArrayList<>();

        //Uncomment to delete data on startup
        /*for (File child : this.getFilesDir().listFiles()) {
            child.delete();
        }*/

        //Initialize for if data doesn't load
        if (!loadData()) {
            Category myCategory = new Category("Other");
            categoryList.add(myCategory);
            dateSetting = "This Month";
            categorySetting = "";
        }
        loadTransactions();
    }

    /*
        File Saving/Loading
     */

    //Saves transactions, filters, categories, etc. to JSON
    public void saveData() {
        for (File child : this.getFilesDir().listFiles()) {
            child.delete();
        }
        ArrayList<JSONObject> data = new ArrayList<>();
        for (Category category : categoryList) {
            JSONObject categoryData = new JSONObject();
            try {
                categoryData.put("File Type", "Category Data");
                categoryData.put("Name", category.getName());
            } catch (Exception e) {}
            data.add(categoryData);
        }
        for (Transaction transaction : transactionList) {
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
            filterData.put("Date Filter", dateSetting);
            filterData.put("Category Filter", categorySetting);
        } catch (Exception e) {
            Toast toast = Toast.makeText(this, "Error saving filter data",(short)3);
            toast.show();
        }
        data.add(filterData);
        for (int i = 0; i < data.size(); i++) {
            try {
                File file = new File(this.getFilesDir(),"file" + i);
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(data.get(i).toString());
                bufferedWriter.close();
            } catch (Exception e) {}
        }
    }

    //Loads transactions, filters, categories, etc. Returns false if there is no data to load
    public boolean loadData() {
        int i = 0;
        File file = new File(this.getFilesDir(),"file" + i);
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
            file = new File(this.getFilesDir(), "file" + i);
        }
        return true;
    }

    //Reads file data and determines what to do
    public void readFile(JSONObject data) {
        try {
            if (data.get("File Type").equals("Category Data")) {
                Category newCategory = new Category(data.get("Name").toString());
                categoryList.add(newCategory);
            }
            else if (data.get("File Type").equals("Transaction Data")) {
                try {
                    Category search = new Category("");
                    for (Category c : categoryList) {
                        if (c.getName().equals(data.get("Category").toString())) {
                            search = c;
                            break;
                        }
                    }
                    SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
                    Date transactionDate = format.parse(data.get("Date").toString());
                    Transaction newTransaction = new Transaction(Double.valueOf(data.get("Amount").toString()), transactionDate, search, data.get("Name").toString());
                    transactionList.add(newTransaction);
                } catch (Exception e) {
                }
            }
            else {
                dateSetting = data.get("Date Filter").toString();
                categorySetting = data.get("Category Filter").toString();
            }
        } catch (Exception e) {
        }
    }

    /*
        Transaction/Category Behaviour
     */

    //Loads categories and displays the total spent on each one
    public void loadCategories() {
        for (int i = 0; i < categoryList.size(); i++) {
            displayCategory(categoryList.get(i));
        }
    }

    //Loads settings, saved transactions, and new transactions from Yodlee, then displays them in list according to settings
    public void loadTransactions() {
        loadTransactions(dateSetting, categorySetting);
    }
    public void loadTransactions(String dateFilter, String categoryFilter) {
        sortTransactions(transactionList);
        //Delete previous displays & display transactions
        TableLayout transactionLayout = (TableLayout) findViewById(R.id.transactionLayout);
        transactionLayout.removeAllViews();
        for (int i = 0; i < categoryList.size(); i++) {
            categoryList.get(i).setAmount(0);
        }
        Date beginning = filterToDate(dateFilter);
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
                displayTransaction(transactionList.get(i));
            }
        }
    }

    //Creates a transaction
    public void createTransaction(View view) {
        Button createButton = (Button) view;
        ConstraintLayout createTransactionLayout = (ConstraintLayout) findViewById(R.id.createTransactionLayout);
        if (createButton.getText().equals("Cancel")) {
            createTransactionLayout.setVisibility(View.INVISIBLE);
        }
        else {
            EditText transactionNameEdit = (EditText) findViewById(R.id.transactionNameEdit);
            EditText transactionAmountEdit = (EditText) findViewById(R.id.transactionAmountEdit);
            EditText transactionDateEdit = (EditText) findViewById(R.id.transactionDateEdit);
            Spinner transactionCategorySpinner = (Spinner) findViewById(R.id.transactionCategorySpinner);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            try {
                Date transactionDate = format.parse(transactionDateEdit.getText().toString());
                Category transactionCategory = null;
                for (int i = 0; i < categoryList.size(); i++) {
                    if (transactionCategorySpinner.getSelectedItem().toString().equals(categoryList.get(i).getName())) {
                        transactionCategory = categoryList.get(i);
                        break;
                    }
                }
                Transaction newTransaction = new Transaction(Double.valueOf(transactionAmountEdit.getText().toString()), transactionDate, transactionCategory, transactionNameEdit.getText().toString());
                transactionList.add(newTransaction);
                saveData();
                loadTransactions();
                createTransactionLayout.setVisibility(View.INVISIBLE);
            } catch(Exception e){
                Toast toast = Toast.makeText(this, "Invalid Entries", (short)3);
                toast.show();
            }
        }
    }

    //Creates a category
    public void createCategory(View view) {
        Button createButton = (Button) view;
        ConstraintLayout createCategoryLayout = (ConstraintLayout) findViewById(R.id.createCategoryLayout);
        if (createButton.getText().equals("Cancel")) {
            createCategoryLayout.setVisibility(View.INVISIBLE);
        }
        else {
            EditText categoryNameEdit = (EditText) findViewById(R.id.categoryNameEdit);
            Category newCategory = new Category(categoryNameEdit.getText().toString());
            categoryList.add(newCategory);
            saveData();
            loadTransactions();
            createCategoryLayout.setVisibility(View.INVISIBLE);
        }
    }

    //Edits a transaction
    public void editTransaction(View view) {
        Button createButton = (Button) view;
        ConstraintLayout createTransactionLayout = (ConstraintLayout) findViewById(R.id.editTransactionLayout);
        if (createButton.getText().equals("Cancel")) {
            createTransactionLayout.setVisibility(View.INVISIBLE);
        }
        else {
            EditText transactionNameEdit = (EditText) findViewById(R.id.editTransactionNameEdit);
            EditText transactionAmountEdit = (EditText) findViewById(R.id.editTransactionAmountEdit);
            EditText transactionDateEdit = (EditText) findViewById(R.id.editTransactionDateEdit);
            Spinner transactionCategorySpinner = (Spinner) findViewById(R.id.editTransactionCategorySpinner);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            try {
                Date transactionDate = format.parse(transactionDateEdit.getText().toString());
                Category transactionCategory = null;
                for (int i = 0; i < categoryList.size(); i++) {
                    if (transactionCategorySpinner.getSelectedItem().toString().equals(categoryList.get(i).getName())) {
                        transactionCategory = categoryList.get(i);
                        break;
                    }
                }
                Transaction transaction = (Transaction)(createButton.getTag());
                transaction.setAmount(Double.valueOf(transactionAmountEdit.getText().toString()));
                transaction.setDate(transactionDate);
                transaction.setCategory(transactionCategory);
                transaction.setName(transactionNameEdit.getText().toString());
                saveData();
                loadTransactions();
                createTransactionLayout.setVisibility(View.INVISIBLE);
            } catch(Exception e){
                Toast toast = Toast.makeText(this, "Invalid Entries", (short)3);
                toast.show();
            }
        }
    }

    //Deletes a transaction
    public void deleteTransaction(View view) {
        transactionList.remove(view.getTag());
        ConstraintLayout editTransactionLayout = (ConstraintLayout) findViewById(R.id.editTransactionLayout);
        editTransactionLayout.setVisibility(View.INVISIBLE);
        loadTransactions();
    }

    /*
        UI Handling
     */

    //Toggles the settings menu and initializes views
    public void toggleSettings(View view) {
        ScrollView settingsView = (ScrollView) findViewById(R.id.settingsView);
        View settingsDecoration = (View) findViewById(R.id.settingsDecoration);
        if (settingsView.getVisibility() == View.VISIBLE) {
            settingsView.setVisibility(View.INVISIBLE);
            settingsDecoration.setVisibility(View.INVISIBLE);
        }
        else {
            settingsView.setVisibility(View.VISIBLE);
            settingsDecoration.setVisibility(View.VISIBLE);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.date_filter_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dateFilterSpinner.setAdapter(adapter);
            int dateFilterPosition = 0;
            if (dateSetting.equals("This and Last Week")) {
                dateFilterPosition = 1;
            }
            else if (dateSetting.equals("This Month")) {
                dateFilterPosition = 2;
            }
            else if (dateSetting.equals("This and Last Month")) {
                dateFilterPosition = 3;
            }
            dateFilterSpinner.setSelection(dateFilterPosition, false);
            dateFilterSpinner.setOnItemSelectedListener(this);

            ArrayList<String> adapterCategoryList = new ArrayList<>();
            adapterCategoryList.add("All Categories");
            for (int i = 0; i < categoryList.size(); i++) {
                adapterCategoryList.add(categoryList.get(i).getName());
            }
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, adapterCategoryList);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoryFilterSpinner.setAdapter(adapter2);
            if (adapter2.getPosition(categorySetting) == -1) {
                categoryFilterSpinner.setSelection(0, false);
                categorySetting = "";
            }
            else {
                categoryFilterSpinner.setSelection(adapter2.getPosition(categorySetting), false);
            }
            categoryFilterSpinner.setOnItemSelectedListener(this);
        }
    }

    //Toggles the new transaction menu
    public void toggleNewTransaction(View view) {
        toggleSettings(view);
        Button createTransactionButton = (Button) findViewById(R.id.createTransactionButton);
        EditText transactionNameEdit = (EditText) findViewById(R.id.transactionNameEdit);
        transactionNameEdit.setText("Enter Name");
        createTransactionButton.setText("Cancel");
        TextWatcher transactionNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (transactionNameEdit.getText().toString().equals("Unnamed Transaction") || transactionNameEdit.getText().toString().equals("Enter Name") || transactionNameEdit.getText().toString().equals("")) {
                    createTransactionButton.setText("Cancel");
                }
                else {
                    createTransactionButton.setText("Create");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        transactionNameEdit.addTextChangedListener(transactionNameWatcher);
        EditText transactionAmountEdit = (EditText) findViewById(R.id.transactionAmountEdit);
        transactionAmountEdit.setText("");

        EditText transactionDateEdit = (EditText) findViewById(R.id.transactionDateEdit);
        transactionDateEdit.setText("");

        ArrayList<String> adapterCategoryList = new ArrayList<>();
        for (int i = 0; i < categoryList.size(); i++) {
            adapterCategoryList.add(categoryList.get(i).getName());
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, adapterCategoryList);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner transactionCategorySpinner = (Spinner) findViewById(R.id.transactionCategorySpinner);
        transactionCategorySpinner.setAdapter(adapter2);
        transactionCategorySpinner.setSelection(0, false);
        transactionCategorySpinner.setOnItemSelectedListener(this);

        ConstraintLayout newTransactionLayout = (ConstraintLayout) findViewById(R.id.createTransactionLayout);
        newTransactionLayout.setVisibility(View.VISIBLE);
    }

    //Toggles the new category menu
    public void toggleNewCategory(View view) {
        toggleSettings(view);
        Button createCategoryButton = (Button) findViewById(R.id.createCategoryButton);
        EditText categoryNameEdit = (EditText) findViewById(R.id.categoryNameEdit);
        categoryNameEdit.setText("Enter Name");
        createCategoryButton.setText("Cancel");
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) {
            categoryNames.add(c.getName());
        }
        TextWatcher categoryNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (categoryNames.indexOf(categoryNameEdit.getText().toString()) >= 0 || categoryNameEdit.getText().toString().equals("Unnamed Category") || categoryNameEdit.getText().toString().equals("Enter Name") || categoryNameEdit.getText().toString().equals("") || categoryNameEdit.getText().toString().equals("All Categories")) {
                    createCategoryButton.setText("Cancel");
                }
                else {
                    createCategoryButton.setText("Create");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        categoryNameEdit.addTextChangedListener(categoryNameWatcher);

        ConstraintLayout newCategoryLayout = (ConstraintLayout) findViewById(R.id.createCategoryLayout);
        newCategoryLayout.setVisibility(View.VISIBLE);
    }

    //Toggles the edit transaction menu
    public void toggleEditTransaction(View view) {
        Transaction transaction = (Transaction)(view.getTag());
        Button editTransactionButton = (Button) findViewById(R.id.editTransactionButton);
        editTransactionButton.setTag(transaction);
        Button deleteTransactionButton = (Button) findViewById(R.id.deleteTransactionButton);
        deleteTransactionButton.setTag(transaction);
        EditText transactionNameEdit = (EditText) findViewById(R.id.editTransactionNameEdit);
        transactionNameEdit.setText(transaction.getName());
        editTransactionButton.setText("Save");
        TextWatcher transactionNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (transactionNameEdit.getText().toString().equals(transactionNameEdit.getText().toString().equals(""))) {
                    editTransactionButton.setText("Cancel");
                }
                else {
                    editTransactionButton.setText("Save");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        transactionNameEdit.addTextChangedListener(transactionNameWatcher);
        EditText transactionAmountEdit = (EditText) findViewById(R.id.editTransactionAmountEdit);
        transactionAmountEdit.setText(String.valueOf(transaction.getAmount()));

        EditText transactionDateEdit = (EditText) findViewById(R.id.editTransactionDateEdit);
        transactionDateEdit.setText((transaction.getDate().getMonth() + 1) + "-" + transaction.getDate().getDate() + "-" + (transaction.getDate().getYear() + 1900));

        ArrayList<String> adapterCategoryList = new ArrayList<>();
        for (int i = 0; i < categoryList.size(); i++) {
            adapterCategoryList.add(categoryList.get(i).getName());
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, adapterCategoryList);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner transactionCategorySpinner = (Spinner) findViewById(R.id.editTransactionCategorySpinner);
        transactionCategorySpinner.setAdapter(adapter2);
        transactionCategorySpinner.setSelection(0, false);
        transactionCategorySpinner.setOnItemSelectedListener(this);

        ConstraintLayout newTransactionLayout = (ConstraintLayout) findViewById(R.id.editTransactionLayout);
        newTransactionLayout.setVisibility(View.VISIBLE);
    }

    //Adds the category to the displayed list
    public void displayCategory(Category category) {
        TableLayout transactionLayout = (TableLayout) findViewById(R.id.transactionLayout);

        TableRow row0 = new TableRow(this);
        TableRow.LayoutParams row0Params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        row0.setLayoutParams(row0Params);
        transactionLayout.addView(row0);

        TextView categoryNameView = new TextView(this);
        TableRow.LayoutParams categoryNameViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryNameViewParams.column = 0;
        categoryNameViewParams.weight = 1;
        categoryNameView.setLayoutParams(categoryNameViewParams);
        Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        categoryNameView.setTypeface(typeface);
        categoryNameView.setMaxLines(1);
        categoryNameView.setText(category.getName());
        categoryNameView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        categoryNameView.setTextColor(Color.WHITE);
        categoryNameView.setTextSize(20);
        row0.addView(categoryNameView);

        TextView categoryAmountView = new TextView(this);
        TableRow.LayoutParams categoryAmountViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryAmountViewParams.column = 1;
        categoryAmountViewParams.weight = 1;
        categoryAmountView.setLayoutParams(categoryAmountViewParams);
        categoryAmountView.setTypeface(typeface);
        categoryAmountView.setMaxLines(1);
        if (category.getAmount() < 0) {
            categoryAmountView.setText("-$" + moneyFormat.format((category.getAmount() * -1)));
        }
        else {
            categoryAmountView.setText("$" + moneyFormat.format(category.getAmount()));
        }
        categoryAmountView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        categoryAmountView.setTextColor(Color.WHITE);
        categoryAmountView.setTextSize(20);
        row0.addView(categoryAmountView);
    }

    //Adds the transaction to the displayed list
    public void displayTransaction(Transaction transaction) {
        TableLayout transactionLayout = (TableLayout) findViewById(R.id.transactionLayout);

        TableRow row0 = new TableRow(this);
        TableRow.LayoutParams row0Params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        row0.setLayoutParams(row0Params);
        transactionLayout.addView(row0);

        TextView transactionNameView = new TextView(this);
        TableRow.LayoutParams transactionNameViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        transactionNameViewParams.column = 0;
        transactionNameViewParams.weight = 1;
        transactionNameView.setLayoutParams(transactionNameViewParams);
        Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        transactionNameView.setTypeface(typeface);
        transactionNameView.setMaxLines(1);
        transactionNameView.setText(transaction.getName());
        transactionNameView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        transactionNameView.setTag(transaction);
        View.OnClickListener transactionOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditTransaction(view);
            }
        };
        transactionNameView.setOnClickListener(transactionOnClickListener);
        transactionNameView.setClickable(true);
        if (transaction.getName().equals("Unnamed Transaction")) {
            transactionNameView.setTextColor(Color.RED);
        }
        else if (transaction.getAmount() > 0) {
            transactionNameView.setTextColor(Color.GREEN);
        }
        else {
            transactionNameView.setTextColor(Color.WHITE);
        }
        transactionNameView.setTextSize(20);
        row0.addView(transactionNameView);

        TableRow row1 = new TableRow(this);
        row1.setLayoutParams(row0Params);
        transactionLayout.addView(row1);

        TextView transactionAmountView = new TextView(this);
        TableRow.LayoutParams transactionAmountViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        transactionAmountViewParams.column = 1;
        transactionAmountViewParams.weight = 1;
        transactionAmountView.setLayoutParams(transactionAmountViewParams);
        Typeface typeface2 = Typeface.create("sans-serif-light", Typeface.NORMAL);
        transactionAmountView.setTypeface(typeface2);
        transactionAmountView.setMaxLines(1);
        if (transaction.getAmount() < 0) {
            transactionAmountView.setText("-$" + moneyFormat.format(transaction.getAmount() * -1));
        }
        else {
            transactionAmountView.setText("$" + moneyFormat.format(transaction.getAmount()));
        }
        transactionAmountView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        transactionAmountView.setTextColor(Color.WHITE);
        transactionAmountView.setTextSize(20);
        row1.addView(transactionAmountView);

        TableRow.LayoutParams transactionCategoryViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        transactionCategoryViewParams.column = 2;
        transactionCategoryViewParams.weight = 1;
        if (transaction.getCategory() != null) {
            TextView transactionCategoryView = new TextView(this);
            transactionCategoryView.setLayoutParams(transactionCategoryViewParams);
            transactionCategoryView.setTypeface(typeface2);
            transactionCategoryView.setMaxLines(1);
            transactionCategoryView.setText(transaction.getCategory().getName());
            transactionCategoryView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            transactionCategoryView.setTextColor(Color.WHITE);
            transactionCategoryView.setTextSize(20);
            row1.addView(transactionCategoryView);
        }

        TableRow row2 = new TableRow(this);
        row2.setLayoutParams(row0Params);
        transactionLayout.addView(row2);

        if (transaction.getDate() != null) {
            TextView transactionDateView = new TextView(this);
            transactionDateView.setLayoutParams(transactionAmountViewParams);
            transactionDateView.setTypeface(typeface2);
            transactionDateView.setMaxLines(1);
            transactionDateView.setText((transaction.getDate().getMonth() + 1) + "/" + transaction.getDate().getDate() + "/" + (transaction.getDate().getYear() + 1900));
            transactionDateView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            transactionDateView.setTextColor(Color.WHITE);
            transactionDateView.setTextSize(20);
            row2.addView(transactionDateView);
        }
    }

    //Implements filter spinner selections
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.dateFilterSpinner) {
            dateSetting = parent.getItemAtPosition(position).toString();
            loadTransactions(dateSetting, categorySetting);
        }
        else if (parent.getId() == R.id.categoryFilterSpinner) {
            if (parent.getItemAtPosition(position).toString().equals("All Categories")) {
                categorySetting = "";
            }
            else {
                categorySetting = parent.getItemAtPosition(position).toString();
            }
            loadTransactions(dateSetting, categorySetting);
        }
        saveData();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /*
        Utility Functions
     */

    //Sorts transactions by date
    public void sortTransactions(ArrayList<Transaction> listToSort) {
        Collections.sort(listToSort);
    }

    //Converts the date filter setting to the beginning date
    public Date filterToDate(String dateFilter) {
        Calendar cal = Calendar.getInstance();
        if (dateFilter.equals("This Week")) {
            Date currentDate = new Date();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1 * currentDate.getDay());
        }
        else if (dateFilter.equals("This and Last Week")) {
            Date currentDate = new Date();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1 * (currentDate.getDay() + 7));
        }
        else if (dateFilter.equals("This Month")) {
            Date currentDate = new Date();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1 * currentDate.getDate() + 1);
        }
        else {
            Date currentDate = new Date();
            currentDate.setMonth(currentDate.getMonth() - 1);
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1 * currentDate.getDate() + 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}