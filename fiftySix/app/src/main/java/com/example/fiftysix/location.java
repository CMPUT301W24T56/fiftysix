package com.example.fiftysix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class location extends AppCompatActivity {

    SupportMapFragment supportMapFragment;
    private ImageButton back;

    private List<LatLng> attendeeLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendees_location);

        Log.d("location", "inflate success");

        // Initialize the map fragment
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_attendees);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hardcoded geolocation data for testing
        attendeeLocations.add(new LatLng(40.7128, -74.0060)); // New York City
        attendeeLocations.add(new LatLng(34.0522, -118.2437)); // Los Angeles
        attendeeLocations.add(new LatLng(51.5074, -0.1278)); // London
        attendeeLocations.add(new LatLng(48.8566, 2.3522)); // Paris

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                // Add markers for each attendee's geolocation
                for (LatLng attendeeLocation : attendeeLocations) {
                    googleMap.addMarker(new MarkerOptions().position(attendeeLocation).title("Attendee"));
                }
            }
        });
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
