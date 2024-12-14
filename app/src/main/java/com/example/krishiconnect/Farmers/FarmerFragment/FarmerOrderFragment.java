package com.example.krishiconnect.Farmers.FarmerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.krishiconnect.Adapters.FarmerOrderAdapter;
import com.example.krishiconnect.Models.FarmerOrderModel;
import com.example.krishiconnect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmerOrderFragment extends Fragment {

    private RecyclerView recyclerView;
    private FarmerOrderAdapter adapter;
    private List<FarmerOrderModel> orderList;
    String userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farmer_order, container, false);

        recyclerView = view.findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new FarmerOrderAdapter(orderList, new FarmerOrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderAccept(int position) {
                acceptOrder(position);
            }

            @Override
            public void onOrderReject(int position) {
                rejectOrder(position);
            }
        });
        recyclerView.setAdapter(adapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");

        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    FarmerOrderModel order = orderSnapshot.getValue(FarmerOrderModel.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void acceptOrder(int position) {
        // Update the order status to "Accepted" in the database
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("KrishiOrders");
        String orderId = orderList.get(position).getProductName();  // Use a unique ID or another identifier
        orderReference.child(orderId).child("status").setValue("Accepted");

        // Send notifications to the customer and rider
        sendNotification(orderList.get(position), "accepted");

        // Optionally, remove the order from the list
        orderList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void rejectOrder(int position) {
        // Update the order status to "Rejected" in the database
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("Orders");
        String orderId = orderList.get(position).getProductName();  // Use a unique ID or another identifier
        orderReference.child(orderId).child("status").setValue("Rejected");

        // Optionally, remove the order from the list
        orderList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void sendNotification(FarmerOrderModel order, String action) {
        // Assume we have customer and rider FCM tokens saved in the database
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("OurProducts");
        userRef.child("users").child(userId).child("fcmToken").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String customerToken = task.getResult().getValue(String.class);
                if (customerToken != null) {
                    sendPushNotification(customerToken, "Your order is " + action + ": " + order.getProductName());
                }
            }
        });

        userRef.child("rider").child(order.getProductName()).child("fcmToken").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String riderToken = task.getResult().getValue(String.class);
                if (riderToken != null) {
                    sendPushNotification(riderToken, "A new order is " + action + " for you: " + order.getProductName());
                }
            }
        });
    }

    private void sendPushNotification(String token, String message) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("to", token);

            JSONObject notification = new JSONObject();
            notification.put("title", "Order Update");
            notification.put("body", message);

            payload.put("notification", notification);

            // Send the request to FCM
            String url = "https://fcm.googleapis.com/fcm/send";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, payload,
                    response -> Log.d("FCM", "FCM response: " + response.toString()),
                    error -> Log.e("FCM", "FCM Error: " + error.toString())) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=d479aeac636d87ef43160efb08171f2027ebb5c7"); // Replace with your Firebase Server Key
                    return headers;
                }
            };

            // Add request to the queue
            Volley.newRequestQueue(getContext()).add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
