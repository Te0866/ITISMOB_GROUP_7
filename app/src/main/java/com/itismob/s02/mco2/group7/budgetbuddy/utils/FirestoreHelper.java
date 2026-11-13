package com.itismob.s02.mco2.group7.budgetbuddy.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreHelper {

    public static final String INCOMES = "incomes";
    public static final String EXPENSES = "expenses";
    public static final String BUDGETS = "budgets";
    public static final String PAYMENTS = "payments";

    public static String uid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getUid();
    }

    public static CollectionReference col(String name) {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid())
                .collection(name);
    }
}