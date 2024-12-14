package com.example.krishiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.Customer.PaymentActivity;
import com.example.krishiconnect.Farmers.FarmerActivity;
import com.example.krishiconnect.Riders.RiderActivity;
import com.example.krishiconnect.Riders.RiderLoginActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmOrderActivity extends AppCompatActivity {

    private EditText customerNameEditText, customerNumberEditText;
    private Button saveOrderButton, getLocationButton, buyNowButton;
    private double latitude, longitude;

    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        // Initialize Firebase reference
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Initialize UI components
        customerNameEditText = findViewById(R.id.CustomerName);
        customerNumberEditText = findViewById(R.id.CustomerNumber);
        saveOrderButton = findViewById(R.id.ConfirmOrder);
        getLocationButton = findViewById(R.id.getLocationBtn);
        buyNowButton = findViewById(R.id.BuyNowButton);

        // Retrieve latitude and longitude from the Intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        // Set save order button listener
        saveOrderButton.setOnClickListener(v -> saveOrder());

        buyNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfirmOrderActivity.this, PaymentActivity.class));
            }
        });

        // Set get location button listener
        getLocationButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(ConfirmOrderActivity.this, MapActivity.class);
            startActivity(mapIntent);
        });
    }

    private void saveOrder() {
        // Get input from user
        String customerName = customerNameEditText.getText().toString();
        String customerPhone = customerNumberEditText.getText().toString();

        // Validate inputs
        if (customerName.isEmpty() || customerPhone.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Order object (destination address is omitted)
        Order order = new Order(customerName, customerPhone, latitude, longitude);

        // Generate unique orderId and save to Firebase
        String orderId = ordersRef.push().getKey();
        if (orderId != null) {
            ordersRef.child(orderId).setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                        // Pass orderId to RiderActivity
                        Intent intent = new Intent(ConfirmOrderActivity.this, RiderActivity.class);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save order.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Error generating order ID. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class Order {
        public String customerName, customerPhone;
        public double latitude, longitude;

        public Order(String customerName, String customerPhone, double latitude, double longitude) {
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
