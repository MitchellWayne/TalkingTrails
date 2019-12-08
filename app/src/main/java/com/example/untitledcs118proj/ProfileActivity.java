package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    TextView usrName;
    ListView listView;
    ArrayList<ProfItem> listItem;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void onResume() {
        super.onResume();
        usrName = findViewById(R.id.profileNameView);
        usrName.setText(MainActivity.currentProfile.getUsername() + "'s Profile:");

        // Get Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        listItem = new ArrayList<ProfItem>();
        listView = (ListView) findViewById(R.id.imgList);

        // Need to make an ArrayList of list items
        // Populate it with data from the db
        fillArrayList();


        // Display images
        ProfItemAdapter imgAdapter = new ProfItemAdapter(getApplicationContext(), listItem);
        listView.setAdapter(imgAdapter);
    }

    private void fillArrayList() {
        // Populate row iteratively with each db entry and do listItem.add(row)
        for (int i = 0; i < MapActivity.usrList.size(); i++){
            String caption = MapActivity.usrList.get(i).getimageCaption();
            int viewcount = MapActivity.usrList.get(i).getviews();

            // Get bitmap
            String url = MapActivity.usrList.get(i).getimageURL();
            Uri dURI = Uri.parse(url);

            final Bitmap[] dbitmap = new Bitmap[1];
            StorageReference ref = storageReference.child("images/"+dURI.getLastPathSegment());
            try {
                final File localFile = File.createTempFile("Images", "bmp");
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        dbitmap[0] = BitmapFactory.decodeFile(localFile.getAbsolutePath());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("Image Get", "Failure");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            ProfItem newItem = new ProfItem(String.valueOf(viewcount), caption, dbitmap[0]);
            listItem.add(newItem);
        }
    }

}
