package com.example.untitledcs118proj;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get handle to fragment
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng UCSC = new LatLng(36.997541, -122.055628);
        map.addMarker(new MarkerOptions().position(UCSC).title("UCSC"));
        map.moveCamera(CameraUpdateFactory.newLatLng(UCSC));
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
