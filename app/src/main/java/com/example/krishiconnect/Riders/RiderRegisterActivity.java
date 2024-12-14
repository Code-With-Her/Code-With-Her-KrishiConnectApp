package com.example.krishiconnect.Riders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class RiderRegisterActivity extends AppCompatActivity {

    ImageView riderRegisterProfileImage;
    EditText riderRegisterName, riderRegisterAddress, riderRegisterNumber, riderRegisterEmail, riderRegisterPassword, riderRegisterConfirmPassword;
    Button riderRegisterBtn;
    ProgressBar progressBar;

    FirebaseAuth fAuth;
    DatabaseReference dRef;

    private static final int GALLERY_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1002;

    private Uri imageUrl;

    String riderName, riderAddress, riderNumber, riderEmail, riderPassword, riderConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_register);

        riderRegisterProfileImage = findViewById(R.id.riderRegisterProfileImage);
        riderRegisterName = findViewById(R.id.riderRegisterName);
        riderRegisterAddress = findViewById(R.id.riderRegisterAddress);
        riderRegisterNumber = findViewById(R.id.riderRegisterNumber);
        riderRegisterEmail = findViewById(R.id.riderRegisterEmail);
        riderRegisterPassword = findViewById(R.id.riderRegisterPassword);
        riderRegisterConfirmPassword = findViewById(R.id.riderRegisterConfirmPassword);
        riderRegisterBtn = findViewById(R.id.riderRegisterBtn);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference("Rider");

        riderRegisterProfileImage.setOnClickListener(v -> showImageSelectionDialog());

        riderRegisterBtn.setOnClickListener(v -> {
            riderName = riderRegisterName.getText().toString().trim();
            riderAddress = riderRegisterAddress.getText().toString().trim();
            riderNumber = riderRegisterNumber.getText().toString().trim();
            riderEmail = riderRegisterEmail.getText().toString().trim();
            riderPassword = riderRegisterPassword.getText().toString().trim();
            riderConfirmPassword = riderRegisterConfirmPassword.getText().toString().trim();

            if (validateFields()) {
                registerRider(riderEmail, riderPassword);
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
                riderRegisterProfileImage.setImageURI(imageUrl);
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                // Handle Camera Image
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                riderRegisterProfileImage.setImageBitmap(imageBitmap);

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
        if (riderName.isEmpty()) {
            riderRegisterName.setError("Enter name");
            return false;
        }
        if (riderAddress.isEmpty()) {
            riderRegisterAddress.setError("Enter address");
            return false;
        }
        if (riderNumber.isEmpty()) {
            riderRegisterNumber.setError("Enter number");
            return false;
        }
        if (riderEmail.isEmpty()) {
            riderRegisterEmail.setError("Enter email");
            return false;
        }
        if (riderPassword.isEmpty()) {
            riderRegisterPassword.setError("Enter password");
            return false;
        }
        if (!riderPassword.equals(riderConfirmPassword)) {
            riderRegisterConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void registerRider(String email, String password) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    String userID = user.getUid();
                    uploadImageToFirebase(userID);
                }
            } else {
                Toast.makeText(RiderRegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(String userID) {
        if (imageUrl != null) {
            StorageReference fileRef = FirebaseStorage.getInstance().getReference("Rider/Profile Images/" + userID + ".jpg");
            fileRef.putFile(imageUrl).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveDataToRealtimeDB(userID, uri.toString()));
            }).addOnFailureListener(e -> {
                Toast.makeText(RiderRegisterActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            saveDataToRealtimeDB(userID, null);
        }
    }

    private void saveDataToRealtimeDB(String userID, String imageUrl) {
        Map<String, Object> riderMap = new HashMap<>();
        riderMap.put("Name", riderName);
        riderMap.put("Address", riderAddress);
        riderMap.put("Number", riderNumber);
        riderMap.put("Email", riderEmail);
        riderMap.put("Password", riderPassword);

        if (imageUrl != null) {
            riderMap.put("ImageUrl", imageUrl);
        }

        dRef.child(userID).setValue(riderMap).addOnSuccessListener(unused -> {
            Toast.makeText(RiderRegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            clearFields();
            // Navigate to the next activity
            Intent intent = new Intent(RiderRegisterActivity.this, RiderActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(RiderRegisterActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFields() {
        riderRegisterName.setText("");
        riderRegisterAddress.setText("");
        riderRegisterNumber.setText("");
        riderRegisterEmail.setText("");
        riderRegisterPassword.setText("");
        riderRegisterConfirmPassword.setText("");
        riderRegisterProfileImage.setImageResource(R.drawable.logo); // Replace with your default placeholder
    }
}
