package com.itismob.s02.mco2.group7.budgetbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.itismob.s02.mco2.group7.budgetbuddy.adapters.AlertAdapter;
import com.itismob.s02.mco2.group7.budgetbuddy.models.AlertItem;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Budget;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Expense;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Payment;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.AlertBuilder;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.FirestoreHelper;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.TestData;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private final ArrayList<Budget> budgets = new ArrayList<>();
    private final ArrayList<Expense> expenses = new ArrayList<>();
    private final ArrayList<Payment> payments = new ArrayList<>();
    private final ArrayList<AlertItem> shown = new ArrayList<>();
    private final List<ListenerRegistration> listeners = new ArrayList<>();

    private AlertAdapter adapter;
    private TextView emptyTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        emptyTv = v.findViewById(R.id.emptyTv);
        RecyclerView rv = v.findViewById(R.id.alertRv);

        adapter = new AlertAdapter(shown);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

//        v.findViewById(R.id.titleTv).setOnLongClickListener(x -> {
//            TestData.seedPayment();
//            Toast.makeText(requireContext(), "Sample payment added", Toast.LENGTH_SHORT).show();
//            return true;
//        });

        listeners.add(FirestoreHelper.col(FirestoreHelper.BUDGETS)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || getView() == null) return;
                    budgets.clear();
                    budgets.addAll(snap.toObjects(Budget.class));
                    refresh();
                }));

        listeners.add(FirestoreHelper.col(FirestoreHelper.EXPENSES)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || getView() == null) return;
                    expenses.clear();
                    expenses.addAll(snap.toObjects(Expense.class));
                    refresh();
                }));

        listeners.add(FirestoreHelper.col(FirestoreHelper.PAYMENTS)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null || getView() == null) return;
                    payments.clear();
                    payments.addAll(snap.toObjects(Payment.class));
                    refresh();
                }));
    }

    private void refresh() {
        shown.clear();
        shown.addAll(AlertBuilder.build(budgets, expenses, payments));
        adapter.notifyDataSetChanged();
        emptyTv.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (ListenerRegistration l : listeners) l.remove();
        listeners.clear();
    }
}