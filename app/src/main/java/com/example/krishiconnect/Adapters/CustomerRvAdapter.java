package com.example.krishiconnect.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.krishiconnect.Customer.DetailedActivity;
import com.example.krishiconnect.Models.CustomerUserModel;
import com.example.krishiconnect.R;

import java.util.List;

public class CustomerRvAdapter extends RecyclerView.Adapter<CustomerRvAdapter.UserViewHolder> {

    private Context context;
    private List<CustomerUserModel> userList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(CustomerUserModel user);
    }

    public CustomerRvAdapter(Context context, List<CustomerUserModel> userList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_each_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        CustomerUserModel user = userList.get(position);

        holder.userName.setText(user.getName());
        holder.price.setText(user.getPrice());

        // Use Glide to load the image
        Glide.with(context)
                .load(user.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background)) // Placeholder image
                .into(holder.userImage);
//
//        if (onItemClickListener != null) {
//            onItemClickListener.onItemClick(user);  // Notify the click event
//        }

        holder.itemView.setOnClickListener(v -> {

            Log.d("Adapter", "Clicked on item");

            Intent intent = new Intent(context, DetailedActivity.class);
            intent.putExtra("product_name", user.getName());
            intent.putExtra("product_price", user.getPrice());
            intent.putExtra("product_image", user.getImageUrl());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName, price, userDes;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            price = itemView.findViewById(R.id.price);
            userDes = itemView.findViewById(R.id.des);
        }
    }
}

