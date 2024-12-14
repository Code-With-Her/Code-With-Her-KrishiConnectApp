package com.example.krishiconnect.Riders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderActivity extends AppCompatActivity {

    private static final String TAG = "RiderActivity";

    private TextView addressTextView, phoneTextView;
    private Button btnViewLocation, btnDeliverySuccess, btnDeliveryFailed;
    private FirebaseDatabase database;
    private DatabaseReference ordersRef;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");

        // Retrieve orderId from Intent
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Order ID received: " + orderId);

        // Initialize Views
        addressTextView = findViewById(R.id.address);
        phoneTextView = findViewById(R.id.phone);
        btnViewLocation = findViewById(R.id.btn_view_location);
        btnDeliverySuccess = findViewById(R.id.btn_delivery_success);
        btnDeliveryFailed = findViewById(R.id.btn_delivery_failed);

        // Fetch order details
        getOrderDetails();

        // View location
        btnViewLocation.setOnClickListener(v -> viewLocation());

        // Update delivery status
        btnDeliverySuccess.setOnClickListener(v -> updateDeliveryStatus("Success"));
        btnDeliveryFailed.setOnClickListener(v -> updateDeliveryStatus("Failed"));
    }

    private void getOrderDetails() {
        ordersRef.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Order found: " + snapshot.getValue());

                    String address = snapshot.child("destinationAddress").getValue(String.class);
                    String phone = snapshot.child("customerPhone").getValue(String.class);

                    if (address != null) {
                        addressTextView.setText("Address: " + address);
                    } else {
                        Log.e(TAG, "Missing destinationAddress for orderId: " + orderId);
                        Toast.makeText(RiderActivity.this, "Address missing!", Toast.LENGTH_SHORT).show();
                    }

                    if (phone != null) {
                        phoneTextView.setText("Phone: " + phone);
                    } else {
                        Log.e(TAG, "Missing customerPhone for orderId: " + orderId);
                        Toast.makeText(RiderActivity.this, "Phone missing!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Order not found for ID: " + orderId);
                    Toast.makeText(RiderActivity.this, "Order not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(RiderActivity.this, "Error fetching order details!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewLocation() {
        ordersRef.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference locationRef = database.getReference("orders").child(orderId);

                    locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Retrieve latitude and longitude values
                                Double destlatitude = snapshot.child("latitude").getValue(Double.class);
                                Double destlongitude = snapshot.child("longitude").getValue(Double.class);

                                openMap(destlatitude, destlongitude);

                                if (destlatitude != null && destlongitude != null) {
                                    // Log or use the retrieved latitude and longitude
                                    Log.d("LocationData", "destlatitude: " + destlatitude + ", destlongitude: " + destlongitude);
                                    Toast.makeText(getApplicationContext(),
                                            "Lat: " + destlatitude + ", Lon: " + destlongitude,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("LocationData", "Latitude or Longitude is null");
                                }
                            } else {
                                Log.e("LocationData", "Snapshot does not exist for the given orderId");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("LocationData", "Failed to read data: " + error.getMessage());
                        }
                    });


                } else {
                    Log.e(TAG, "Location data missing for orderId: " + orderId);
                    Toast.makeText(RiderActivity.this, "Location data missing!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error fetching location data: " + error.getMessage());
                Toast.makeText(RiderActivity.this, "Error fetching location data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMap(Double destLatitude, Double destLongitude) {
        // Construct the Google Maps URL with only destination
        String mapsUrl = "https://www.google.com/maps/search/?api=1" +
                "&query=" + destLatitude + "," + destLongitude;

        // Parse the URL
        Uri gmmIntentUri = Uri.parse(mapsUrl);

        // Create an Intent to launch Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if the Google Maps app is installed
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent); // Launch Google Maps
        } else {
            // Notify the user if Google Maps is not installed
            Toast.makeText(this, "Google Maps is not installed!", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateDeliveryStatus(String status) {
        ordersRef.child(orderId).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RiderActivity.this, "Delivery Status Updated: " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to update delivery status for orderId: " + orderId);
                        Toast.makeText(RiderActivity.this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
