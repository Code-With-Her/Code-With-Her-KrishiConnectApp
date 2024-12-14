package com.example.krishiconnect.Customer.CustomerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.krishiconnect.Adapters.MyCartAdapter;
import com.example.krishiconnect.Models.MyCartModel;
import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for managing the customer's cart.
 */
public class CustomerCartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private MyCartAdapter myCartAdapter;
    private List<MyCartModel> cartList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    // Firebase Cloud Messaging (FCM) constants
    private static final String FCM_SERVER_KEY = "d479aeac636d87ef43160efb08171f2027ebb5c7"; // Replace with your Firebase server key
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public CustomerCartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_cart, container, false);

        // Initialize RecyclerView and Adapter
        cartRecyclerView = view.findViewById(R.id.cart_rv);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartList = new ArrayList<>();
        myCartAdapter = new MyCartAdapter(getContext(), cartList);
        cartRecyclerView.setAdapter(myCartAdapter);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Subscribe to the "farmers" topic for notifications
        FirebaseMessaging.getInstance().subscribeToTopic("farmers");

        fetchCartData();

        return view;
    }

    /**
     * Fetches cart data for the current user from Firebase Realtime Database.
     */
    private void fetchCartData() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Cart").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MyCartModel cartItem = dataSnapshot.getValue(MyCartModel.class);

                    // Apply default values if data is missing
                    if (cartItem != null) {
                        if (cartItem.getTotalPrice() == 0) {
                            cartItem.setTotalPrice(50); // Default price
                        }
                        if (cartItem.getTotalQuantity() <= 0) {
                            cartItem.setTotalQuantity(1); // Default quantity
                        }
                        if (cartItem.getImageUrl() == null || cartItem.getImageUrl().isEmpty()) {
                            cartItem.setImageUrl("default_image_url"); // Default image
                        }
                        cartList.add(cartItem);
                    }
                }
                myCartAdapter.notifyDataSetChanged(); // Update the RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch cart data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }




    /**
     * Sends a notification to the farmer about a new order.
     *
     * @param item The cart item for which the order is placed.
     */
    private void notifyFarmer(MyCartModel item) {
        new Thread(() -> {
            try {
                // Create FCM message payload
                Map<String, Object> notification = new HashMap<>();
                notification.put("title", "New Order Received");
                notification.put("body", "Order for " + item.getProductName() + " has been placed!");

                Map<String, Object> payload = new HashMap<>();
                payload.put("to", "/topics/farmers"); // Target the "farmers" topic
                payload.put("notification", notification);

                // Send POST request to FCM
                URL url = new URL(FCM_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "key=" + FCM_SERVER_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(new com.google.gson.Gson().toJson(payload));
                writer.flush();
                writer.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("CustomerCartFragment", "Notification sent successfully");
                } else {
                    Log.e("CustomerCartFragment", "Error sending notification: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("CustomerCartFragment", "Error sending notification", e);
            }
        }).start();
    }
}
