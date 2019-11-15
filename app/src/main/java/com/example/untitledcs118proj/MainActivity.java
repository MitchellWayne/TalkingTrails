package com.example.untitledcs118proj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);
    }

    public void login(View view) {
        // Check if valid profile credentials
        // ...

        // Move to map activity after successful login
        Intent it = new Intent(this,MapActivity.class);
        startActivity(it);
    }

    public void create(View view) {
        // Just move to profile creation
        Intent it = new Intent(this,CreateActivity.class);
        startActivity(it);
    }
}
