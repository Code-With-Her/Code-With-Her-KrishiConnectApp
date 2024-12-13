package com.example.krishiconnect.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.krishiconnect.Customer.PaymentActivity;
import com.example.krishiconnect.Models.MyCartModel;
import com.example.krishiconnect.R;

import java.util.List;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.ViewHolder> {
    private Context context;
    private List<MyCartModel> cartList;

    public MyCartAdapter(Context context, List<MyCartModel> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyCartModel cartItem = cartList.get(position);

        holder.productName.setText(cartItem.getProductName());
        holder.productPrice.setText("Rs. " + cartItem.getTotalPrice());
        holder.productQuantity.setText("Qty: " + cartItem.getTotalQuantity());
        holder.dateTime.setText(cartItem.getCurrentDate() + " " + cartItem.getCurrentTime());

        Glide.with(context).load(cartItem.getImageUrl()).into(holder.productImage);

        // Set onClickListener for itemView
        holder.itemView.setOnClickListener(v -> showActionDialog(cartItem));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity, dateTime;
        ImageView productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productQuantity = itemView.findViewById(R.id.cartProductQuantity);
            dateTime = itemView.findViewById(R.id.cartDateTime);
            productImage = itemView.findViewById(R.id.cartProductImage);
        }
    }

    // Function to show a dialog box with Buy and Remove options
    private void showActionDialog(MyCartModel cartItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Action");
        builder.setMessage("What would you like to do with this item?");

        builder.setPositiveButton("Buy", (dialog, which) -> {
            // Navigate to PaymentActivity
            Intent intent = new Intent(context, PaymentActivity.class);
            intent.putExtra("productName", cartItem.getProductName());
            intent.putExtra("productPrice", cartItem.getTotalPrice());
            intent.putExtra("productQuantity", cartItem.getTotalQuantity());
            intent.putExtra("imageUrl", cartItem.getImageUrl());
            context.startActivity(intent);
        });

        builder.setNegativeButton("Remove", (dialog, which) -> {
            // Remove the item from cart
            if (context instanceof RemoveItemListener) {
                ((RemoveItemListener) context).removeItem(cartItem);
            } else {
                Toast.makeText(context, "Failed to remove item. Listener not implemented.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // Interface for removing item from cart
    public interface RemoveItemListener {
        void removeItem(MyCartModel cartItem);
    }
}
