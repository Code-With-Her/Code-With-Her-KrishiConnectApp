package com.example.krishiconnect.Customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

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
        EdgeToEdge.enable(this);
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



        increaseQuantityButton.setOnClickListener(v -> increaseQuantity());

        decreaseQuantityButton.setOnClickListener(v -> decreaseQuantity());

        buyNowButton.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(DetailedActivity.this, PaymentActivity.class);
            startActivity(paymentIntent);
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