package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static Profile currentProfile;
    EditText username;
    EditText password;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    static ArrayList<Profile> profileList = new ArrayList<Profile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        // Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
            mAuth.signInAnonymously();
        } else {
            mAuth.signInAnonymously();
        }

        // Populate login db
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users/");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item_snapshot : dataSnapshot.getChildren()) {
                    Profile pInfo = item_snapshot.getValue(Profile.class);
                    profileList.add(pInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }

        });
    }

    public void login(View view) {
        // Check if valid profile credentials
        String sUser, sPass;
        sUser = username.getText().toString();
        sPass = password.getText().toString();
        Boolean userExist = false;

        for (int i = 0; i < profileList.size(); i++){
            Profile currProf = profileList.get(i);
            if (currProf.getUsername().equals(sUser)){
                userExist = true;
                if (currProf.getPassword().equals(sPass)){
                    // Login Successful
                    Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show();
                    currentProfile = new Profile(currProf);

                    // Move to map activity after successful login
                    Intent it = new Intent(this,MapActivity.class);
                    startActivity(it);
                }
                else {
                    // Incorrect Password
                    Toast.makeText(this, "Password incorrect", Toast.LENGTH_LONG).show();
                }
            }
        }
        if (!userExist) {
            // User not found
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
        }
    }

    public void create(View view) {
        // Just move to profile creation
        Intent it = new Intent(this,CreateActivity.class);
        startActivity(it);
    }
}
