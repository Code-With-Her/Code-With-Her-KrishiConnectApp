package com.example.krishiconnect;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.krishiconnect.Customer.PaymentActivity;
import com.example.krishiconnect.Riders.RiderActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class ConfirmOrderActivity extends AppCompatActivity {

    private EditText customerNameEditText, customerNumberEditText;
    private Button saveOrderButton, getLocationButton, buyNowButton;
    private double latitude, longitude;

    private DatabaseReference ordersRef;

    private static final String CHANNEL_ID = "krishi_connect";
    private static final String CHANNEL_NAME = "Krishi Connect";
    private static final String CHANNEL_DESC = "Krishi Connect Notifications";


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

        // Create notification channel
        createNotificationChannel();

        // Save Order Button Listener
        saveOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveOrder();
                    }
                }, 2000);
            }
        });

        // Buy Now Button Listener
        buyNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfirmOrderActivity.this, PaymentActivity.class));
            }
        });

        // Get Location Button Listener
        getLocationButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(ConfirmOrderActivity.this, MapActivity.class);
            startActivity(mapIntent);
        });



    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void displayNotification(String orderId) {
        // Check and request permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        // Create an Intent to open RiderActivity
        Intent intent = new Intent(this, RiderActivity.class);
        intent.putExtra("orderId", orderId); // Pass the orderId to RiderActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Wrap the Intent in a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Ensure logo exists in res/drawable
                .setContentTitle("Order Received")
                .setContentText("You received a new Order. Do you want to pick it up?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Auto-dismiss when clicked
                .setContentIntent(pendingIntent); // Open RiderActivity on click

        // Display notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
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

        // Create Order object
        Order order = new Order(customerName, customerPhone, latitude, longitude);

        // Generate unique orderId and save to Firebase
        String orderId = ordersRef.push().getKey();
        if (orderId != null) {
            ordersRef.child(orderId).setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                        displayNotification(orderId); // Show notification after saving order

                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(ConfirmOrderActivity.this, RiderActivity.class);
                            intent.putExtra("orderId", orderId);
                            startActivity(intent);
                        }, 5000);

                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save order.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Error generating order ID. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Order model class
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
