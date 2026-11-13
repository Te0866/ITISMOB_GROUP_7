package com.itismob.s02.mco2.group7.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            Class<?> next = loggedIn ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(SplashActivity.this, next));
            finish();
        }, 1800);
    }
}