package com.example.krishiconnect.Customer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.krishiconnect.Customer.CustomerFragment.CustomerCartFragment;
import com.example.krishiconnect.Customer.CustomerFragment.CustomerHomeFragment;
import com.example.krishiconnect.Customer.CustomerFragment.CustomerProfileFragment;
import com.example.krishiconnect.R;
import com.example.krishiconnect.databinding.ActivityCustomerBinding;

public class CustomerActivity extends AppCompatActivity {

    ActivityCustomerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = new CustomerHomeFragment();
            } else if (item.getItemId() == R.id.cart) {
                selectedFragment = new CustomerCartFragment();
            } else if (item.getItemId() == R.id.profile) {
                selectedFragment = new CustomerProfileFragment();
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
                    .replace(binding.container.getId(), new CustomerHomeFragment())
                    .commit();
            binding.bottomNavigation.setSelectedItemId(R.id.home);
        }
    }
}
