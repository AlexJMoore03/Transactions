package com.AlexJMoore03.transactions.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.AlexJMoore03.transactions.R;
import com.AlexJMoore03.transactions.data.Category;
import com.AlexJMoore03.transactions.data.Transaction;
import com.AlexJMoore03.transactions.util.FileHandler;
import com.AlexJMoore03.transactions.util.Utilities;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner dateFilterSpinner;
    private Spinner categoryFilterSpinner;
    private static final DecimalFormat moneyFormat = new DecimalFormat("0.00");
    public MainViewModel model;

    /*
        Activity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new ViewModelProvider(this).get(MainViewModel.class);
        model.mainActivity = this;

        dateFilterSpinner = (Spinner) findViewById(R.id.dateFilterSpinner);
        categoryFilterSpinner = (Spinner) findViewById(R.id.categoryFilterSpinner);
        model.transactionList = new ArrayList<>();
        model.categoryList = new ArrayList<>();

        //Uncomment to delete data on startup
        FileHandler.deleteData(this);

        //Initialize for if data doesn't load

        if (!FileHandler.loadData(this)) {
            Category myCategory = new Category("Other", 100);
            model.categoryList.add(myCategory);
            model.dateSetting = "This Month";
            model.categorySetting = "";
        }
        model.loadTransactions();
    }

    /*
        Transaction/Category Behaviour
     */

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
                for (int i = 0; i < model.categoryList.size(); i++) {
                    if (transactionCategorySpinner.getSelectedItem().toString().equals(model.categoryList.get(i).getName())) {
                        transactionCategory = model.categoryList.get(i);
                        break;
                    }
                }
                Transaction newTransaction = new Transaction(Double.valueOf(transactionAmountEdit.getText().toString()), transactionDate, transactionCategory, transactionNameEdit.getText().toString());
                model.transactionList.add(newTransaction);
                FileHandler.saveData(this);
                model.loadTransactions();
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
            EditText categoryBudgetEdit = (EditText) findViewById(R.id.categoryBudgetEdit);
            Category newCategory = new Category(categoryNameEdit.getText().toString(), Double.valueOf(categoryBudgetEdit.getText().toString()));
            model.categoryList.add(newCategory);
            FileHandler.saveData(this);
            model.loadTransactions();
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
                for (int i = 0; i < model.categoryList.size(); i++) {
                    if (transactionCategorySpinner.getSelectedItem().toString().equals(model.categoryList.get(i).getName())) {
                        transactionCategory = model.categoryList.get(i);
                        break;
                    }
                }
                Transaction transaction = (Transaction)(createButton.getTag());
                transaction.setAmount(Double.valueOf(transactionAmountEdit.getText().toString()));
                transaction.setDate(transactionDate);
                transaction.setCategory(transactionCategory);
                transaction.setName(transactionNameEdit.getText().toString());
                FileHandler.saveData(this);
                model.loadTransactions();
                createTransactionLayout.setVisibility(View.INVISIBLE);
            } catch(Exception e){
                Toast toast = Toast.makeText(this, "Invalid Entries", (short)3);
                toast.show();
            }
        }
    }

    //Edits a category
    public void editCategory(View view) {
        Button createButton = (Button) view;
        ConstraintLayout createCategoryLayout = (ConstraintLayout) findViewById(R.id.editCategoryLayout);
        if (createButton.getText().equals("Cancel")) {
            createCategoryLayout.setVisibility(View.INVISIBLE);
        }
        else {
            EditText categoryNameEdit = (EditText) findViewById(R.id.editCategoryNameEdit);
            EditText categoryBudgetEdit = (EditText) findViewById(R.id.editCategoryBudgetEdit);
            Category category = (Category)(view.getTag());
            category.setName(categoryNameEdit.getText().toString());
            category.setBudget(Double.valueOf(categoryBudgetEdit.getText().toString()));
            FileHandler.saveData(this);
            model.loadTransactions();
            createCategoryLayout.setVisibility(View.INVISIBLE);
        }
    }

    //Deletes a transaction
    public void deleteTransaction(View view) {
        model.transactionList.remove(view.getTag());
        ConstraintLayout editTransactionLayout = (ConstraintLayout) findViewById(R.id.editTransactionLayout);
        editTransactionLayout.setVisibility(View.INVISIBLE);
        FileHandler.saveData(this);
        model.loadTransactions();
    }

    //Deletes a category
    public void deleteCategory(View view) {
        model.categoryList.remove(view.getTag());
        ConstraintLayout editTransactionLayout = (ConstraintLayout) findViewById(R.id.editCategoryLayout);
        editTransactionLayout.setVisibility(View.INVISIBLE);
        FileHandler.saveData(this);
        model.loadTransactions();
    }

    /*
        UI Handling
     */

    //Empties the transactions box
    public void emptyTransactionDisplay() {
        TableLayout transactionLayout = (TableLayout) findViewById(R.id.transactionLayout);
        transactionLayout.removeAllViews();
    }
    //Empties the categories box
    public void emptyCategoryDisplay() {
        LinearLayout categoryLayout = (LinearLayout) findViewById(R.id.categoryLayout);
        categoryLayout.removeAllViews();
    }

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
            if (model.dateSetting.equals("This and Last Week")) {
                dateFilterPosition = 1;
            }
            else if (model.dateSetting.equals("This Month")) {
                dateFilterPosition = 2;
            }
            else if (model.dateSetting.equals("This and Last Month")) {
                dateFilterPosition = 3;
            }
            dateFilterSpinner.setSelection(dateFilterPosition, false);
            dateFilterSpinner.setOnItemSelectedListener(this);

            ArrayList<String> adapterCategoryList = new ArrayList<>();
            adapterCategoryList.add("All Categories");
            for (int i = 0; i < model.categoryList.size(); i++) {
                adapterCategoryList.add(model.categoryList.get(i).getName());
            }
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, adapterCategoryList);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoryFilterSpinner.setAdapter(adapter2);
            if (adapter2.getPosition(model.categorySetting) == -1) {
                categoryFilterSpinner.setSelection(0, false);
                model.categorySetting = "";
            }
            else {
                categoryFilterSpinner.setSelection(adapter2.getPosition(model.categorySetting), false);
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
        for (int i = 0; i < model.categoryList.size(); i++) {
            adapterCategoryList.add(model.categoryList.get(i).getName());
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
        EditText categoryBudgetEdit = (EditText) findViewById(R.id.categoryBudgetEdit);
        categoryNameEdit.setText("Enter Name");
        categoryBudgetEdit.setText("0");
        createCategoryButton.setText("Cancel");
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : model.categoryList) {
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
        for (int i = 0; i < model.categoryList.size(); i++) {
            adapterCategoryList.add(model.categoryList.get(i).getName());
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

    //Toggles the edit category menu
    public void toggleEditCategory(View view) {
        Category category = (Category)(view.getTag());
        Button createCategoryButton = (Button) findViewById(R.id.editCategoryButton);
        createCategoryButton.setTag(category);
        Button deleteCategoryButton = (Button) findViewById(R.id.deleteCategoryButton);
        deleteCategoryButton.setTag(category);
        EditText categoryNameEdit = (EditText) findViewById(R.id.editCategoryNameEdit);
        EditText categoryBudgetEdit = (EditText) findViewById(R.id.editCategoryBudgetEdit);
        categoryNameEdit.setText(category.getName());
        categoryBudgetEdit.setText(String.valueOf(category.getBudget()));
        createCategoryButton.setText("Save");
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : model.categoryList) {
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
                    createCategoryButton.setText("Save");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        categoryNameEdit.addTextChangedListener(categoryNameWatcher);

        ConstraintLayout newCategoryLayout = (ConstraintLayout) findViewById(R.id.editCategoryLayout);
        newCategoryLayout.setVisibility(View.VISIBLE);
    }

    //Adds the category to the displayed list
    public void displayCategory(Category category) {
        LinearLayout categoryLayout = (LinearLayout) findViewById(R.id.categoryLayout);

        LinearLayout mainLayout = new LinearLayout(this);
        LinearLayout.LayoutParams mainParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mainParams.bottomMargin = 16;
        mainLayout.setLayoutParams(mainParams);
        mainLayout.setBackgroundColor(Color.parseColor("#101112"));
        categoryLayout.addView(mainLayout);

        TableRow row0 = new TableRow(this);
        TableRow.LayoutParams row0Params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        row0.setLayoutParams(row0Params);
        mainLayout.addView(row0);

        TextView categoryNameView = new TextView(this);
        TableRow.LayoutParams categoryNameViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryNameViewParams.column = 0;
        categoryNameViewParams.weight = 1;
        categoryNameViewParams.setMarginStart(8);
        categoryNameView.setLayoutParams(categoryNameViewParams);
        Typeface typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        categoryNameView.setTypeface(typeface);
        categoryNameView.setMaxLines(1);
        categoryNameView.setText(category.getName());
        categoryNameView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        if (category.getName() != "Other") {
            categoryNameView.setTag(category);
            View.OnClickListener categoryOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleEditCategory(view);
                }
            };
            categoryNameView.setOnClickListener(categoryOnClickListener);
            categoryNameView.setClickable(true);
        }
        categoryNameView.setTextColor(Color.WHITE);
        categoryNameView.setTextSize(20);
        row0.addView(categoryNameView);

        TextView categoryAmountView = new TextView(this);
        TableRow.LayoutParams categoryAmountViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        categoryAmountViewParams.column = 1;
        categoryAmountViewParams.weight = 1;
        categoryAmountViewParams.setMarginStart(8);
        categoryAmountView.setLayoutParams(categoryAmountViewParams);
        categoryAmountView.setTypeface(typeface);
        categoryAmountView.setMaxLines(1);
        if (category.getAmount() < 0) {
            categoryAmountView.setText("-$" + moneyFormat.format((category.getAmount() * -1)) + "/" + moneyFormat.format(category.getBudget()));
        }
        else {
            categoryAmountView.setText("+$" + moneyFormat.format(category.getAmount()) + "/" + moneyFormat.format(category.getBudget()));
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
        transactionNameViewParams.setMarginStart(8);
        transactionNameViewParams.topMargin = 8;
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
        transactionAmountViewParams.setMarginStart(8);
        transactionAmountView.setLayoutParams(transactionAmountViewParams);
        Typeface typeface2 = Typeface.create("sans-serif-light", Typeface.NORMAL);
        transactionAmountView.setTypeface(typeface2);
        transactionAmountView.setMaxLines(1);
        if (transaction.getAmount() < 0) {
            transactionAmountView.setText("-$" + moneyFormat.format(transaction.getAmount() * -1));
        }
        else {
            transactionAmountView.setText("+$" + moneyFormat.format(transaction.getAmount()));
        }
        transactionAmountView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        transactionAmountView.setTextColor(Color.WHITE);
        transactionAmountView.setTextSize(20);
        row1.addView(transactionAmountView);

        TableRow.LayoutParams transactionCategoryViewParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        transactionCategoryViewParams.column = 2;
        transactionCategoryViewParams.weight = 1;
        transactionCategoryViewParams.setMarginStart(8);
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
            model.dateSetting = parent.getItemAtPosition(position).toString();
            model.loadTransactions(model.dateSetting, model.categorySetting);
        }
        else if (parent.getId() == R.id.categoryFilterSpinner) {
            if (parent.getItemAtPosition(position).toString().equals("All Categories")) {
                model.categorySetting = "";
            }
            else {
                model.categorySetting = parent.getItemAtPosition(position).toString();
            }
            model.loadTransactions(model.dateSetting, model.categorySetting);
        }
        FileHandler.saveData(this);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}