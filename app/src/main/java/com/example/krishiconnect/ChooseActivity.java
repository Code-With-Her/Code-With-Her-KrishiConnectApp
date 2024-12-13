package com.example.krishiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.krishiconnect.Customer.CustomerActivity;
import com.example.krishiconnect.Customer.CustomerLoginActivity;
import com.example.krishiconnect.Farmers.FarmerActivity;
import com.example.krishiconnect.Farmers.FarmerLoginActivity;
import com.example.krishiconnect.Riders.RiderLoginActivity;

public class ChooseActivity extends AppCompatActivity {

    Button farmerBtn, customerBtn, riderBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose);

        farmerBtn = findViewById(R.id.farmerBtn);
        customerBtn = findViewById(R.id.customerBtn);
        riderBtn = findViewById(R.id.riderBtn);

        farmerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, FarmerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        customerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, CustomerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        riderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, RiderLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}