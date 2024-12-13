package com.example.krishiconnect.Customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.krishiconnect.ConfirmOrderActivity;
import com.example.krishiconnect.Customer.CustomerFragment.CustomerCartFragment;
import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DetailedActivity extends AppCompatActivity {

    private TextView productName, productPrice, productDescription, quantityText;
    private ImageView productImage;
    private RatingBar productRating;
    private Button addToCartButton, decreaseQuantityButton, increaseQuantityButton, buyNowButton, placeOrder;

    private FirebaseAuth fAuth;
    private DatabaseReference dRef;

    private int quantity = 1; // Default quantity
    private int originalPrice = 0; // To store the original price
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        // Initialize views
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        productImage = findViewById(R.id.productImage);
        productRating = findViewById(R.id.productRating);
        quantityText = findViewById(R.id.quantityText);

        addToCartButton = findViewById(R.id.addToCartButton);
        decreaseQuantityButton = findViewById(R.id.decreaseQuantity);
        increaseQuantityButton = findViewById(R.id.increaseQuantity);
        buyNowButton = findViewById(R.id.buyNowButton);
        placeOrder = findViewById(R.id.placeOrder);

        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference().child("Cart").child(fAuth.getCurrentUser().getUid());

        // Get data from the intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("product_name");
        String priceString = intent.getStringExtra("product_price");
        imageUrl = intent.getStringExtra("product_image");

        // Set data to views
        productName.setText(name);
        productDescription.setText("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s...");
        quantityText.setText(String.valueOf(quantity));

        // Parse price
        try {
            originalPrice = Integer.parseInt(priceString);
        } catch (NumberFormatException e) {
            originalPrice = 0;
        }
        productPrice.setText("Price: Rs." + originalPrice);

        // Load image using Glide
        Glide.with(this).load(imageUrl).into(productImage);

        // Set rating
        productRating.setRating(4.5f); // Example rating

        // Button listeners
        addToCartButton.setOnClickListener(v -> addToCart(name, originalPrice, quantity));

        increaseQuantityButton.setOnClickListener(v -> increaseQuantity());

        decreaseQuantityButton.setOnClickListener(v -> decreaseQuantity());

        buyNowButton.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(DetailedActivity.this, PaymentActivity.class);
            startActivity(paymentIntent);
        });

        placeOrder.setOnClickListener(v -> {
            startActivity(new Intent(DetailedActivity.this, ConfirmOrderActivity.class));
        });
    }

    private void addToCart(String productName, int productPrice, int quantity) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("productName", productName);
        cartMap.put("TotalQuantity", quantity);
        cartMap.put("TotalPrice", productPrice * quantity);
        cartMap.put("currentDate", saveCurrentDate);
        cartMap.put("currentTime", saveCurrentTime);
        cartMap.put("ImageUrl", imageUrl);

        dRef.child(productName).updateChildren(cartMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(DetailedActivity.this, CustomerCartFragment.class));
                Toast.makeText(DetailedActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailedActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void increaseQuantity() {
        quantity++;
        quantityText.setText(String.valueOf(quantity));
        updatePriceDisplay();
    }

    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityText.setText(String.valueOf(quantity));
            updatePriceDisplay();
        }
    }

    private void updatePriceDisplay() {
        int totalPrice = originalPrice * quantity;
        productPrice.setText("Price: Rs." + totalPrice);
    }

}