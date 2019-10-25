package com.example.untitledcs118proj;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.content.Intent;

public class MapActivity extends MainActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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
