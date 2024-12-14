package com.example.krishiconnect.Farmers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class FarmerRegisterActivity extends AppCompatActivity {

    ImageView farmerRegisterProfileImage;
    EditText farmerRegisterName, farmerRegisterAddress, farmerRegisterNumber, farmerRegisterEmail, farmerRegisterPassword, farmerRegisterConfirmPassword;
    Button farmerRegisterBtn;

    FirebaseAuth fAuth;
    DatabaseReference dRef;

    private static final int GALLERY_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1002;

    private Uri imageUrl;

    String farmerName, farmerAddress, farmerNumber, farmerEmail, farmerPassword, farmerConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_register);

        farmerRegisterProfileImage = findViewById(R.id.farmerRegisterProfileImage);
        farmerRegisterName = findViewById(R.id.farmerRegisterName);
        farmerRegisterAddress = findViewById(R.id.farmerRegisterAddress);
        farmerRegisterNumber = findViewById(R.id.farmerRegisterNumber);
        farmerRegisterEmail = findViewById(R.id.farmerRegisterEmail);
        farmerRegisterPassword = findViewById(R.id.farmerRegisterPassword);
        farmerRegisterConfirmPassword = findViewById(R.id.farmerRegisterConfirmPassword);
        farmerRegisterBtn = findViewById(R.id.farmerRegisterBtn);

        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference("Farmer");

        farmerRegisterProfileImage.setOnClickListener(v -> showImageSelectionDialog());

        farmerRegisterBtn.setOnClickListener(v -> {
            farmerName = farmerRegisterName.getText().toString().trim();
            farmerAddress = farmerRegisterAddress.getText().toString().trim();
            farmerNumber = farmerRegisterNumber.getText().toString().trim();
            farmerEmail = farmerRegisterEmail.getText().toString().trim();
            farmerPassword = farmerRegisterPassword.getText().toString().trim();
            farmerConfirmPassword = farmerRegisterConfirmPassword.getText().toString().trim();

            if (validateFields()) {
                registerFarmer(farmerEmail, farmerPassword);
            }
        });
    }

    private void showImageSelectionDialog() {
        String[] options = {"Open Camera", "Open Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Open Camera
                        openCamera();
                    } else if (which == 1) {
                        // Open Gallery
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                // Handle Gallery Image
                imageUrl = data.getData();
                farmerRegisterProfileImage.setImageURI(imageUrl);
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                // Handle Camera Image
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                farmerRegisterProfileImage.setImageBitmap(imageBitmap);

                // Convert Bitmap to URI (if needed for uploading to Firebase)
                imageUrl = getImageUri(imageBitmap);
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "ProfileImage", null);
        return Uri.parse(path);
    }

    private boolean validateFields() {
        if (farmerName.isEmpty()) {
            farmerRegisterName.setError("Enter name");
            return false;
        }
        if (farmerAddress.isEmpty()) {
            farmerRegisterAddress.setError("Enter address");
            return false;
        }
        if (farmerNumber.isEmpty()) {
            farmerRegisterNumber.setError("Enter number");
            return false;
        }
        if (farmerEmail.isEmpty()) {
            farmerRegisterEmail.setError("Enter email");
            return false;
        }
        if (farmerPassword.isEmpty()) {
            farmerRegisterPassword.setError("Enter password");
            return false;
        }
        if (!farmerPassword.equals(farmerConfirmPassword)) {
            farmerRegisterConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void registerFarmer(String email, String password) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    sendEmailVerification(user);
                    String userID = user.getUid();
                    uploadImageToFirebase(userID);
                }
            } else {
                Toast.makeText(FarmerRegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FarmerRegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FarmerRegisterActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(String userID) {
        if (imageUrl != null) {
            StorageReference fileRef = FirebaseStorage.getInstance().getReference("Farmer/Profile Images/" + userID + ".jpg");
            fileRef.putFile(imageUrl).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveDataToRealtimeDB(userID, uri.toString()));
            }).addOnFailureListener(e -> {
                Toast.makeText(FarmerRegisterActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            saveDataToRealtimeDB(userID, null);
        }
    }

    private void saveDataToRealtimeDB(String userID, String imageUrl) {
        Map<String, Object> farmerMap = new HashMap<>();
        farmerMap.put("Name", farmerName);
        farmerMap.put("Address", farmerAddress);
        farmerMap.put("Number", farmerNumber);
        farmerMap.put("Email", farmerEmail);
        farmerMap.put("Password", farmerPassword);

        if (imageUrl != null) {
            farmerMap.put("ImageUrl", imageUrl);
        }

        dRef.child(userID).setValue(farmerMap).addOnSuccessListener(unused -> {
            Toast.makeText(FarmerRegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            clearFields();
            // Navigate to the next activity
            Intent intent = new Intent(FarmerRegisterActivity.this, FarmerActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(FarmerRegisterActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFields() {
        farmerRegisterName.setText("");
        farmerRegisterAddress.setText("");
        farmerRegisterNumber.setText("");
        farmerRegisterEmail.setText("");
        farmerRegisterPassword.setText("");
        farmerRegisterConfirmPassword.setText("");
        farmerRegisterProfileImage.setImageResource(R.drawable.logo); // Replace with your default placeholder
    }
}
