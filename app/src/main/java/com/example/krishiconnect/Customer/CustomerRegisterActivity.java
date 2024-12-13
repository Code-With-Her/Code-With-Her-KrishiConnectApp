package com.example.krishiconnect.Customer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class CustomerRegisterActivity extends AppCompatActivity {

    private EditText customerRegisterName, customerRegisterAddress, customerRegisterNumber, customerRegisterEmail, customerRegisterPassword, customerRegisterConfirmPassword;
    private Button customerRegisterBtn;
    private ImageView customerRegisterProfileImage;

    private FirebaseAuth fAuth;
    private FirebaseDatabase fDatabase;
    private DatabaseReference dRef;
    private StorageReference sRef;

    private static final int GALLERY_REQUEST_CODE = 1000;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1001;

    private Uri imageUrl;

    private String customerName, customerAddress, customerNumber, customerEmail, customerPassword, customerConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        customerRegisterProfileImage = findViewById(R.id.customerRegisterProfileImage);
        customerRegisterName = findViewById(R.id.customerRegisterName);
        customerRegisterAddress = findViewById(R.id.customerRegisterAddress);
        customerRegisterNumber = findViewById(R.id.customerRegisterNumber);
        customerRegisterEmail = findViewById(R.id.customerRegisterEmail);
        customerRegisterPassword = findViewById(R.id.customerRegisterPassword);
        customerRegisterConfirmPassword = findViewById(R.id.customerRegisterConfirmPassword);
        customerRegisterBtn = findViewById(R.id.customerRegisterBtn);

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dRef = fDatabase.getReference("KrishiCustomers");
        sRef = FirebaseStorage.getInstance().getReference("KrishiCustomerProfileImages");

        customerRegisterProfileImage.setOnClickListener(v -> openGallery());

        customerRegisterBtn.setOnClickListener(v -> {
            customerName = customerRegisterName.getText().toString().trim();
            customerAddress = customerRegisterAddress.getText().toString().trim();
            customerNumber = customerRegisterNumber.getText().toString().trim();
            customerEmail = customerRegisterEmail.getText().toString().trim();
            customerPassword = customerRegisterPassword.getText().toString().trim();
            customerConfirmPassword = customerRegisterConfirmPassword.getText().toString().trim();

            if (validateInputs()) {
                registerCustomer(customerEmail, customerPassword);
            }
        });
    }

    private boolean validateInputs() {
        if (customerName.isEmpty()) {
            customerRegisterName.setError("Please enter your name");
            return false;
        }
        if (customerAddress.isEmpty()) {
            customerRegisterAddress.setError("Please enter your address");
            return false;
        }
        if (customerNumber.isEmpty()) {
            customerRegisterNumber.setError("Please enter your phone number");
            return false;
        }
        if (customerEmail.isEmpty()) {
            customerRegisterEmail.setError("Please enter your email");
            return false;
        }
        if (customerPassword.isEmpty()) {
            customerRegisterPassword.setError("Please enter your password");
            return false;
        }
        if (!customerPassword.equals(customerConfirmPassword)) {
            customerRegisterConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void openGallery() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    private void registerCustomer(String email, String password) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userID = fAuth.getCurrentUser().getUid();
                FirebaseUser user = fAuth.getCurrentUser();
                if (imageUrl != null) {
                    uploadImageToFirebase(userID);
                    sendEmailVerification(user);
                } else {
                    saveCustomerDataToDatabase(userID, null);
                }
            } else {
                Toast.makeText(CustomerRegisterActivity.this, "Registration Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showVerificationDialog();
            } else {
                Toast.makeText(CustomerRegisterActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Verify Your Email")
                .setMessage("A verification email has been sent to your registered email. Please verify your email to proceed.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    fAuth.signOut();
                    startActivity(new Intent(CustomerRegisterActivity.this, CustomerLoginActivity.class));
                    finish();
                })
                .create()
                .show();
    }

    private void uploadImageToFirebase(String userID) {
        StorageReference fileRef = sRef.child("Customer/Profile Images/" +userID + ".jpg");
        fileRef.putFile(imageUrl).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String downloadUri = task.getResult().toString();
                saveCustomerDataToDatabase(userID, downloadUri);
            }
        })).addOnFailureListener(e -> Toast.makeText(CustomerRegisterActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveCustomerDataToDatabase(String userID, String imageUrl) {
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("Name", customerName);
        customerData.put("Address", customerAddress);
        customerData.put("Number", customerNumber);
        customerData.put("Email", customerEmail);
        customerData.put("Password", customerPassword);

        if (imageUrl != null) {
            customerData.put("ImageUrl", imageUrl);
        }

        dRef.child(userID).setValue(customerData).addOnSuccessListener(unused -> {
            Toast.makeText(CustomerRegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            resetFields();
            startActivity(new Intent(CustomerRegisterActivity.this, CustomerActivity.class));
            finish();
        }).addOnFailureListener(e -> Toast.makeText(CustomerRegisterActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void resetFields() {
        customerRegisterName.setText("");
        customerRegisterAddress.setText("");
        customerRegisterNumber.setText("");
        customerRegisterEmail.setText("");
        customerRegisterPassword.setText("");
        customerRegisterConfirmPassword.setText("");
        customerRegisterProfileImage.setImageResource(R.drawable.ic_launcher_background);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUrl = data.getData();
            customerRegisterProfileImage.setImageURI(imageUrl);
        }
    }
}
