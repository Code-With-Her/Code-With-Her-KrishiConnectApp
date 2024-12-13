package com.example.krishiconnect.Farmers.FarmerFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FarmerPostFragment extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 1000;

    private FirebaseAuth fAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;

    private ImageView profileImage;
    private EditText nameField, descriptionField, priceField;
    private ProgressBar progressBar;
    private TextView retrieveBtn, postBtn;

    public FarmerPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_farmer_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views using R.id
        profileImage = view.findViewById(R.id.profileImage);
        nameField = view.findViewById(R.id.name);
        descriptionField = view.findViewById(R.id.des);
        priceField = view.findViewById(R.id.price);
        progressBar = view.findViewById(R.id.progressBar);
        postBtn = view.findViewById(R.id.btn);

        // Initialize Firebase instances
        fAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("OurProducts");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");


        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });

        postBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String description = descriptionField.getText().toString().trim();
            String price = priceField.getText().toString().trim();

            if (validateInput(name, description, price)) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImageToFirebase(imageUri, name, description, price);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == requireActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private boolean validateInput(String name, String description, String price) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(price)) {
            priceField.setError("Price is required");
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionField.setError("Description is required");
            return false;
        }

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadImageToFirebase(Uri uri, String name, String description, String price) {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    saveUserDataToRealtimeDB(name, description, price, downloadUri.toString());
                }
            }
        })).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserDataToRealtimeDB(String name, String description, String price, String imageUrl) {
        String userID = fAuth.getCurrentUser() != null ? fAuth.getCurrentUser().getUid() : "Unknown_User";

        // Generate a unique key for the entry
        String entryKey = databaseReference.child(userID).child("userDetails").push().getKey();

        if (entryKey != null) {
            // Prepare the user data to be saved
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("description", description);
            userData.put("price", price);
            userData.put("imageUrl", imageUrl);

            // Save the data under the generated entry key
            databaseReference.child("wau").child("userDetails").child(entryKey).setValue(userData)
                    .addOnSuccessListener(unused -> {
                        // Get the FCM token and save it
                        saveFCMTokenToDatabase(userID);
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void saveFCMTokenToDatabase(String userID) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fcmToken = task.getResult();
                        if (fcmToken != null) {
                            // Save the token to the database
                            databaseReference.child("users").child(userID).child("fcmToken").setValue(fcmToken)
                                    .addOnSuccessListener(unused -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(requireContext(), "FCM Token saved successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(requireContext(), "Failed to save FCM token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
