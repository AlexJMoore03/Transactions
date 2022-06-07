package com.AlexJMoore03.transactions.util;

import com.AlexJMoore03.transactions.data.Category;
import com.AlexJMoore03.transactions.data.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Utilities {
    //Sorts transactions by date
    public static void sortTransactions(ArrayList<Transaction> listToSort) {
        Collections.sort(listToSort);
    }

    //Sorts categories by name
    public static void sortCategories(ArrayList<Category> listToSort) {
        Collections.sort(listToSort);
    }

    //Converts the date filter setting to the beginning date
    public static Date filterToDate(String dateFilter) {
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
