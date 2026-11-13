package com.itismob.s02.mco2.group7.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEt, emailEt, passwordEt;
    private MaterialButton signUpBtn, googleBtn;
    private TextView backTv;

    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(this, "Google sign-in failed (code "
                            + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        nameEt = findViewById(R.id.nameEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        signUpBtn = findViewById(R.id.signUpBtn);
        googleBtn = findViewById(R.id.googleBtn);
        backTv = findViewById(R.id.backTv);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        backTv.setOnClickListener(v -> finish());
        signUpBtn.setOnClickListener(v -> registerWithEmail());
        googleBtn.setOnClickListener(v ->
                googleClient.signOut().addOnCompleteListener(t ->
                        googleLauncher.launch(googleClient.getSignInIntent())));
    }

    private void registerWithEmail() {
        String name = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString();

        if (TextUtils.isEmpty(name)) {
            nameEt.setError("Enter your full name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Enter your email");
            return;
        }
        if (password.length() < 6) {
            passwordEt.setError("Password must be at least 6 characters");
            return;
        }

        signUpBtn.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user == null) {
                    signUpBtn.setEnabled(true);
                    return;
                }
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();
                user.updateProfile(request).addOnCompleteListener(t -> {
                    PrefsHelper.saveName(this, name);
                    PrefsHelper.saveEmail(this, email);
                    goToMain();
                });
            } else {
                signUpBtn.setEnabled(true);
                String msg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Sign up failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    PrefsHelper.saveName(this, user.getDisplayName() == null ? "" : user.getDisplayName());
                    PrefsHelper.saveEmail(this, user.getEmail() == null ? "" : user.getEmail());
                }
                goToMain();
            } else {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}