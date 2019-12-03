package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateActivity extends AppCompatActivity {

    EditText newUser;
    EditText newPassword;
    EditText eqlPassword;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        newUser = findViewById(R.id.newUser);
        newPassword = findViewById(R.id.newPassword);
        eqlPassword = findViewById(R.id.confirmPassword);

    }

    public void confirmCreate(View view) {

        String sUser = newUser.getText().toString();
        String sPass = newPassword.getText().toString();
        String ePass = eqlPassword.getText().toString();
        Boolean userExists = false;
        Boolean equal = false;

        // Check if valid user and password format
        // ... not current priority

        // Check if existing username
        Log.d("profList Size = ", "" + MainActivity.profileList.size());

        for (int i = 0; i < MainActivity.profileList.size(); i++){
            Log.d("create stops = ", "1");

            Profile currProf = new Profile(MainActivity.profileList.get(i));
            if (currProf.getUsername().equals(sUser)){
                Log.d("create stops = ", "2");

                Toast.makeText(this, "Username already exists. Choose another.", Toast.LENGTH_LONG).show();
                userExists = true;
                break;
            }
        }

        Log.d("create stops = ", "1.5");

        // Check if equal passwords
        if (!userExists) {
            Log.d("create stops = ", "4");

            if (sPass.equals(ePass)) {
                Log.d("create stops = ", "5");

                equal = true;
            }
            else {
                Log.d("create stops = ", "6");

                Toast.makeText(this, "Make sure your password matches.", Toast.LENGTH_LONG).show();
            }
        }

        Log.d("create stops = ", "6.5");


        // If user doesn't exist, create account
        if (!userExists && equal){
            Log.d("create stops = ", "7");

            database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("users/");

            // Store to db
            Profile createProf = new Profile(sUser, sPass);
            String profUploadID = databaseReference.push().getKey();
            databaseReference.child(profUploadID).setValue(createProf);

            // Reset profile list
            MainActivity.profileList.clear();
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item_snapshot : dataSnapshot.getChildren()) {
                        Profile pInfo = item_snapshot.getValue(Profile.class);
                        MainActivity.profileList.add(pInfo);
                        Log.d("profList Size = ", "" + MainActivity.profileList.size());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }

            });

            // Finish activity
            Toast.makeText(this, "Profile created!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
