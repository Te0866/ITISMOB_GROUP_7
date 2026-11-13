package com.itismob.s02.mco2.group7.budgetbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itismob.s02.mco2.group7.budgetbuddy.models.AlertItem;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Budget;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Expense;
import com.itismob.s02.mco2.group7.budgetbuddy.models.Payment;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.AlertBuilder;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.FirestoreHelper;
import com.itismob.s02.mco2.group7.budgetbuddy.utils.NotificationHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_ALERTS = "EXTRA_OPEN_ALERTS";

    private BottomNavigationView bottomNav;
    private FloatingActionButton addFab;

    private final ArrayList<Budget> budgets = new ArrayList<>();
    private final ArrayList<Expense> expenses = new ArrayList<>();
    private final ArrayList<Payment> payments = new ArrayList<>();

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) notifyNewAlerts();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        addFab = findViewById(R.id.addFab);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_home) {
                f = new HomeFragment();
            } else if (id == R.id.nav_transactions) {
                f = new TransactionsFragment();
            } else if (id == R.id.nav_budgets) {
                f = new BudgetsFragment();
            } else {
                f = new AlertsFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, f)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            boolean openAlerts = getIntent().getBooleanExtra(EXTRA_OPEN_ALERTS, false);
            bottomNav.setSelectedItemId(openAlerts ? R.id.nav_alerts : R.id.nav_home);
        }

        addFab.setOnClickListener(v -> showAddDialog());

        NotificationHelper.createChannel(this);
        askNotificationPermission();
        watchAlerts();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_OPEN_ALERTS, false)) {
            bottomNav.setSelectedItemId(R.id.nav_alerts);
        }
    }

    public void selectTab(int menuId) {
        bottomNav.setSelectedItemId(menuId);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void watchAlerts() {
        FirestoreHelper.col(FirestoreHelper.BUDGETS)
                .addSnapshotListener(this, (snap, e) -> {
                    if (snap == null) return;
                    budgets.clear();
                    budgets.addAll(snap.toObjects(Budget.class));
                    notifyNewAlerts();
                });

        FirestoreHelper.col(FirestoreHelper.EXPENSES)
                .addSnapshotListener(this, (snap, e) -> {
                    if (snap == null) return;
                    expenses.clear();
                    expenses.addAll(snap.toObjects(Expense.class));
                    notifyNewAlerts();
                });

        FirestoreHelper.col(FirestoreHelper.PAYMENTS)
                .addSnapshotListener(this, (snap, e) -> {
                    if (snap == null) return;
                    payments.clear();
                    payments.addAll(snap.toObjects(Payment.class));
                    notifyNewAlerts();
                });
    }

    private void notifyNewAlerts() {
        for (AlertItem alert : AlertBuilder.build(budgets, expenses, payments)) {
            if (PrefsHelper.wasNotified(this, alert.getKey())) continue;
            if (NotificationHelper.show(this, alert)) {
                PrefsHelper.markNotified(this, alert.getKey());
            }
        }
    }

    private void showAddDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Add new")
                .setItems(new String[]{"Add Income", "Add Expense"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, AddEditIncomeActivity.class));
                    } else {
                        startActivity(new Intent(this, AddEditExpenseActivity.class));
                    }
                })
                .show();
    }
}