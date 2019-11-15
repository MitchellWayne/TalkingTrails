package com.example.untitledcs118proj;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    // Location Services
    static GoogleMap mMap;
    private LocationListener locListen;
    private LocationManager locManager;
    private final long MIN_UPDATE_TIME = 1000; // 1000ms = 1s
    private final long MIN_DISTANCE = 5; // 5 meters
    static LatLng locLatLng;
    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get handle to fragment
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                PackageManager.PERMISSION_GRANTED);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LatLng UCSC = new LatLng(36.997541, -122.055628);
        mMap.addMarker(new MarkerOptions().position(UCSC).title("UCSC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(UCSC));

        mMap.setMyLocationEnabled(true);

        // Add custom Map Style
        boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        // Location Services
        locListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    locLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(locLatLng).title("Location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_DISTANCE, locListen);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void profile(View view) {
        Intent it = new Intent(this,ProfileActivity.class);
        startActivity(it);
    }

    public void ar(View view) {
        Intent it = new Intent(this,ARActivity.class);
        startActivity(it);
    }

    public void upload(View view) {
        Intent it = new Intent(this,UploadActivity.class);
        startActivity(it);
    }
}
