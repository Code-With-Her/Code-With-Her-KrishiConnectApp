package com.example.krishiconnect.Customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.krishiconnect.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewLocation extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "ViewLocation";

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference locationRef;
    private GoogleMap googleMap;
    private LatLng destinationLatLng;
    private Button startNavButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);

        startNavButton = findViewById(R.id.btnStartNav);

        if (checkLocationPermission()) {
            initializeMap();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Snackbar.make(findViewById(R.id.mapView), "Location permission is required.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void initializeMap() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        locationRef = firebaseDatabase.getReference("orders/destinationLocation");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    googleMap = map;
                    loadDestinationLocation();
                    updateCurrentLocation();
                }
            });
        }

        startNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (destinationLatLng != null) {
                    Snackbar.make(v, "Starting Navigation to: " + destinationLatLng, Snackbar.LENGTH_LONG).show();
                    // Optionally integrate with Google Maps Intent
                    // launchNavigation(destinationLatLng);
                } else {
                    Snackbar.make(v, "Destination not set.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadDestinationLocation() {
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        double lat = snapshot.child("latitude").getValue(Double.class);
                        double lon = snapshot.child("longitude").getValue(Double.class);
                        destinationLatLng = new LatLng(lat, lon);
                        googleMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 12));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing destination location: " + e.getMessage());
                        Snackbar.make(findViewById(R.id.mapView), "Invalid destination data.", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.mapView), "Destination not found.", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Snackbar.make(findViewById(R.id.mapView), "Failed to load destination.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your Location"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                        } else {
                            Snackbar.make(findViewById(R.id.mapView), "Could not fetch current location.", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching current location: " + e.getMessage());
                        Snackbar.make(findViewById(R.id.mapView), "Failed to fetch location.", Snackbar.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error: " + e.getMessage());
            Snackbar.make(findViewById(R.id.mapView), "Location permission denied.", Snackbar.LENGTH_SHORT).show();
        }
    }
}
