package com.example.krishiconnect.Farmers;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.krishiconnect.Customer.CustomerFragment.CustomerCartFragment;
import com.example.krishiconnect.Customer.CustomerFragment.CustomerHomeFragment;
import com.example.krishiconnect.Customer.CustomerFragment.CustomerProfileFragment;
import com.example.krishiconnect.Farmers.FarmerFragment.FarmerHomeFragment;
import com.example.krishiconnect.Farmers.FarmerFragment.FarmerOrderFragment;
import com.example.krishiconnect.Farmers.FarmerFragment.FarmerProfileFragment;
import com.example.krishiconnect.R;
import com.example.krishiconnect.databinding.ActivityCustomerBinding;
import com.example.krishiconnect.databinding.ActivityFarmerBinding;

public class FarmerActivity extends AppCompatActivity {

    ActivityFarmerBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFarmerBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = new FarmerHomeFragment();
            } else if (item.getItemId() == R.id.post) {
                selectedFragment = new FarmerOrderFragment();
            } else if (item.getItemId() == R.id.order) {
                selectedFragment = new FarmerOrderFragment();
            } else if (item.getItemId() == R.id.profile) {
                selectedFragment = new FarmerProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.container.getId(), selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set HomeFragment as default when activity starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.container.getId(), new FarmerHomeFragment())
                    .commit();
            binding.bottomNavigation.setSelectedItemId(R.id.home);
        }
    }
}