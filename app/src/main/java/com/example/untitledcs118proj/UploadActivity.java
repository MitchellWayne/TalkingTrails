package com.example.untitledcs118proj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.StorageMetadata;
// Custom Info Window Stuff
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;


import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;


public class UploadActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    ImageView imageToUpload;
    Button uploadButton;
    EditText caption;
    Uri filepath;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        //Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        uploadButton = (Button) findViewById(R.id.uploadImage);
        caption = (EditText) findViewById(R.id.caption);

        imageToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            filepath = data.getData();
            imageToUpload.setImageURI(filepath);
        }
    }

    private void uploadImage() {
        if(filepath != null)
        {
            // Add image to map (TEMP WIP) ---------------------------------------------------------
            // this is on single user side only
            String cap = caption.getText().toString();
            try {
                // Change image to generic symbol
                Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filepath);
//                MapActivity.mMap.addMarker(new MarkerOptions().position(MapActivity.locLatLng).title(cap)
//                        .icon(BitmapDescriptorFactory.fromBitmap(b)));

                Marker marker = MapActivity.mMap.addMarker(new MarkerOptions().position(MapActivity.locLatLng).title(cap));

                // Add caption and image to marker metadata
                MarkerData mData = (MarkerData) new MarkerData();
                mData.setCaption(cap);
                mData.setImage(b);
                marker.setTag(mData);

            } catch (IOException e) {
                e.printStackTrace();
            }
            // -------------------------------------------------------------------------------------

            // Store to firebase storage
            StorageReference ref = storageReference.child("images/"+filepath.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(filepath);

            // Get caption
            // Get location
            // (This requires splitting lat and long to cast to string)
            // (Remember to reverse this when retrieving locations from db)

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("caption", "caption goes here")
                    .setCustomMetadata("location", "location values here")
                    .build();

            // Add metadata properties
            ref.updateMetadata(metadata).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Updated metadata is in storageMetadata
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(UploadActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
