package com.example.exe1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ScoreLocationsFragment extends Fragment {

    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_score_locations, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                googleMap = gMap;
                // Initialize the map and display the locations
                Bundle args = getArguments();
                if (args != null) {
                    String location = args.getString("location");
                    Log.d("ScoreLocationsFragment", "Received location: " + location); // Debugging
                    if (location != null && !location.isEmpty()) {
                        String[] latLng = location.split(",");
                        if (latLng.length == 2) {
                            try {
                                double latitude = Double.parseDouble(latLng[0]);
                                double longitude = Double.parseDouble(latLng[1]);
                                LatLng position = new LatLng(latitude, longitude);
                                googleMap.clear();
                                googleMap.addMarker(new MarkerOptions().position(position).title("Score Location"));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                            } catch (NumberFormatException e) {
                                Log.e("ScoreLocationsFragment", "Invalid location format", e);
                            }
                        } else {
                            Log.e("ScoreLocationsFragment", "Invalid location format");
                        }
                    } else {
                        // Handle the case where location is empty or null
                        Log.e("ScoreLocationsFragment", "No location received");
                        Toast.makeText(getContext(), "No location received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where getArguments() returns null
                    Log.e("ScoreLocationsFragment", "Arguments bundle is null");
                    Toast.makeText(getContext(), "No location received", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
