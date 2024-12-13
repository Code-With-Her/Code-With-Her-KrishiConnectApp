package com.example.krishiconnect.Customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText customerLoginEmail, customerLoginPassword;
    private Button customerLoginBtn;
    private TextView customerRegisterTxt, forgotPass;
    private ProgressBar progressBar;

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        customerLoginEmail = findViewById(R.id.customerLoginEmail);
        customerLoginPassword = findViewById(R.id.customerLoginPassword);
        customerLoginBtn = findViewById(R.id.customerLoginBtn);
        customerRegisterTxt = findViewById(R.id.customerregisterTxt);
        forgotPass = findViewById(R.id.forgotPass);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();

        customerRegisterTxt.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerLoginActivity.this, CustomerRegisterActivity.class);
            startActivity(intent);
        });

        customerLoginBtn.setOnClickListener(v -> validateAndLogin());

        forgotPass.setOnClickListener(v -> showResetPasswordDialog());
    }

    private void validateAndLogin() {
        String email = customerLoginEmail.getText().toString().trim();
        String password = customerLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            customerLoginEmail.setError("Please enter your email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            customerLoginPassword.setError("Please enter your password");
            return;
        }

        login(email, password);
    }

    private void login(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if email is verified
                if (fAuth.getCurrentUser().isEmailVerified()) {
                    Toast.makeText(CustomerLoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CustomerLoginActivity.this, CustomerActivity.class));
                    finish();
                } else {
                    Toast.makeText(CustomerLoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                    fAuth.signOut();
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(CustomerLoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showResetPasswordDialog() {
        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter your email");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            String email = resetMail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(CustomerLoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            resetPassword(email);
        });

        passwordResetDialog.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        passwordResetDialog.create().show();
    }

    private void resetPassword(String email) {
        fAuth.sendPasswordResetEmail(email).addOnSuccessListener(aVoid ->
                Toast.makeText(CustomerLoginActivity.this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(CustomerLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
