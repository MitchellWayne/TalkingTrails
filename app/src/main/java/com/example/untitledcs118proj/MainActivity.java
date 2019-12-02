package com.example.untitledcs118proj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        // Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
            mAuth.signInAnonymously();
        } else {
            mAuth.signInAnonymously();
        }

        // LOGIN AND CREATE INFO
        /*
            We will simply create a new db with username and password linked.
            We will also add username to image metadata, alongside like count.
            On create account, first we make an arraylist of all usernames
            then check if it is an existing account. If not, it is valid.
            Then we check the pass word to see if they match (and optionally
            are valid passwords). In login, we will determine if the username exists
            and if there is a password match. Otherwise display a toast message.
         */
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
