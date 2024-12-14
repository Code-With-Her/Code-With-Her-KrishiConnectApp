package com.example.krishiconnect.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.krishiconnect.Models.FarmerOrderModel;
import com.example.krishiconnect.R;
import java.util.List;

public class FarmerOrderAdapter extends RecyclerView.Adapter<FarmerOrderAdapter.OrderViewHolder> {

    private List<FarmerOrderModel> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderAccept(int position);
        void onOrderReject(int position);
    }

    public FarmerOrderAdapter(List<FarmerOrderModel> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_each_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        FarmerOrderModel order = orderList.get(position);
        holder.productName.setText(order.getProductName());
        holder.quantity.setText("Quantity: " + order.getTotalQuantity());
        holder.price.setText("Price: " + order.getTotalPrice());

        // Handle accept and reject buttons
        holder.acceptButton.setOnClickListener(v -> listener.onOrderAccept(position));
        holder.rejectButton.setOnClickListener(v -> listener.onOrderReject(position));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity, price;
        Button acceptButton, rejectButton;

        public OrderViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.quantity);
            price = itemView.findViewById(R.id.price);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
