package com.example.untitledcs118proj;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
