package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
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

        // Check if existing username
        for (int i = 0; i < MainActivity.profileList.size(); i++){

            Profile currProf = new Profile(MainActivity.profileList.get(i));
            if (currProf.getUsername().equals(sUser)){

                Toast.makeText(this, "Username already exists. Choose another.", Toast.LENGTH_LONG).show();
                userExists = true;
                break;
            }
        }


        // Check if equal passwords
        if (!userExists) {

            if (sPass.equals(ePass)) {
                equal = true;
            }
            else {
                Toast.makeText(this, "Make sure your password matches.", Toast.LENGTH_LONG).show();
            }
        }

        // If user doesn't exist, create account
        if (!userExists && equal){

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
