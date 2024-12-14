package com.example.krishiconnect.Customer.CustomerFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView, numberTextView, addressTextView;
    private ImageView profileImageView;

    public CustomerProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameTextView = view.findViewById(R.id.tv_name);
        emailTextView = view.findViewById(R.id.tv_email);
        numberTextView = view.findViewById(R.id.tv_number);
        addressTextView = view.findViewById(R.id.tv_address);
        profileImageView = view.findViewById(R.id.iv_profile);

        // Get the current user's UID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firebase reference
        DatabaseReference customerRef = FirebaseDatabase.getInstance()
                .getReference("KrishiCustomers")
                .child(currentUserId);

        // Fetch data from Firebase
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch values from Firebase
                    String name = snapshot.child("Name").getValue(String.class);
                    String email = snapshot.child("Email").getValue(String.class);
                    String number = snapshot.child("Number").getValue(String.class);
                    String address = snapshot.child("Address").getValue(String.class);
                    String imageUrl = snapshot.child("ImageUrl").getValue(String.class);

                    // Update UI
                    nameTextView.setText(name != null ? name : "Name not available");
                    emailTextView.setText(email != null ? email : "Email not available");
                    numberTextView.setText(number != null ? number : "Number not available");
                    addressTextView.setText(address != null ? address : "Address not available");

                    if (imageUrl != null) {
                        Glide.with(requireContext()).load(imageUrl).into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.logo);
                    }
                } else {
                    Toast.makeText(requireContext(), "No profile data found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}
