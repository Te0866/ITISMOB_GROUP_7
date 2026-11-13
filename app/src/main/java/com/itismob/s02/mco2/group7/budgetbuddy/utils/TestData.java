package com.itismob.s02.mco2.group7.budgetbuddy.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Budget;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Expense;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Income;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Payment;

public class TestData {

    public static void seed() {
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        addIncome(5000, "Allowance", "Monthly allowance", now - 2 * day);
        addIncome(4500, "Part-time Job", "Weekend shift", now - 5 * day);
        addIncome(3000, "Scholarship", "", now - 8 * day);

        addExpense(500, "Food", "Lunch", now - day);
        addExpense(300, "Food", "Snacks", now - 2 * day);
        addExpense(400, "Food", "Dinner", now - 3 * day);
        addExpense(675, "Transport", "Jeepney and MRT", now - 2 * day);
        addExpense(560, "School", "Printing", now - 4 * day);
        addExpense(450, "Shopping", "Notebook", now - 5 * day);
        addExpense(300, "Entertainment", "Movie", now);

        addBudget("Food", 2000);
        addBudget("Transport", 1000);
        addBudget("School", 1000);
        addBudget("Entertainment", 300);
    }

    private static void addIncome(double amount, String source, String notes, long date) {
        CollectionReference col = FirestoreHelper.col(FirestoreHelper.INCOMES);
        DocumentReference ref = col.document();
        ref.set(new Income(ref.getId(), amount, source, notes, date));
    }

    private static void addExpense(double amount, String category, String notes, long date) {
        CollectionReference col = FirestoreHelper.col(FirestoreHelper.EXPENSES);
        DocumentReference ref = col.document();
        ref.set(new Expense(ref.getId(), amount, category, notes, date));
    }

    private static void addBudget(String category, double limit) {
        CollectionReference col = FirestoreHelper.col(FirestoreHelper.BUDGETS);
        DocumentReference ref = col.document();
        ref.set(new Budget(ref.getId(), category, limit, "Monthly",
                Periods.monthStart(), Periods.monthEnd()));
    }

    public static void seedPayment() {
        CollectionReference col = FirestoreHelper.col(FirestoreHelper.PAYMENTS);
        DocumentReference ref = col.document();
        long due = System.currentTimeMillis() + 3 * 24L * 60 * 60 * 1000;
        ref.set(new Payment(ref.getId(), "Tuition Fee", 8000, due, false));
    }
}