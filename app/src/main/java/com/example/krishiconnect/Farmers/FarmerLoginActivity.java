package com.example.krishiconnect.Farmers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FarmerLoginActivity extends AppCompatActivity {

    EditText farmerLoginEmail, farmerLoginPassword;
    Button farmerLoginBtn;
    TextView registerTxt, forgotPass;
    ProgressBar progressBar;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_farmer_login);

        farmerLoginEmail = findViewById(R.id.farmerLoginEmail);
        farmerLoginPassword = findViewById(R.id.farmerLoginPassword);
        farmerLoginBtn = findViewById(R.id.farmerLoginBtn);
        registerTxt = findViewById(R.id.registerTxt);
        forgotPass = findViewById(R.id.forgotPass);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();

        // Login button click listener
        farmerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        // Register button click listener
        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerLoginActivity.this, FarmerRegisterActivity.class);
                startActivity(intent);
            }
        });

        // Forgot password click listener
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
    }

    private void validateAndLogin() {
        String email = farmerLoginEmail.getText().toString().trim();
        String password = farmerLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            farmerLoginEmail.setError("Enter your email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            farmerLoginPassword.setError("Enter your password");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        login(email, password);
    }

    private void login(String email, String password) {
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(FarmerLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(FarmerLoginActivity.this, FarmerActivity.class));
                            finish();
                        } else {
                            Toast.makeText(FarmerLoginActivity.this, "Login failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showPasswordResetDialog() {
        EditText resetMail = new EditText(this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter your email to receive the reset link");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mail = resetMail.getText().toString().trim();
                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(FarmerLoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                fAuth.sendPasswordResetEmail(mail)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(FarmerLoginActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FarmerLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        passwordResetDialog.create().show();
    }
}
