package com.example.untitledcs118proj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;


public class UploadActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    ImageView imageToUpload;
    ImageButton uploadButton, cameraButton;
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
        uploadButton = (ImageButton) findViewById(R.id.uploadImage);

        cameraButton = (ImageButton) findViewById(R.id.btnCamera);

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

        //button to open up camera view
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
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
        else {
            Bitmap cameraPic = (Bitmap)data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            cameraPic.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), cameraPic,"Title", null);
            filepath = Uri.parse(path);
            imageToUpload.setImageURI(filepath);
        }
    }

    private void uploadImage() {
        if(filepath != null)
        {
            // Add image to map (TEMP WIP) --------------------------------------------------------v
            // this is on curret user side only
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
                //add lnglat

            } catch (IOException e) {
                e.printStackTrace();
            }
            // ------------------------------------------------------------------------------------^

            // NOTE!!!
            // In addition to firebase storage, we should utilize firestore collections to store
            // the same filepath segment and necessary metadata for easier access.
            // In map activity, we will iterate on firestore then sort out the records with
            // locations that fit in the user radius. THEN we take those record's filepaths
            // to retrieve the actual image from firebase storage. EZ

            // Store to firebase storage ----------------------------------------------------------v
            final StorageReference ref = storageReference.child("images/"+filepath.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(filepath);

            // Get location
            // (This requires splitting lat and long to cast to string)
            // (Remember to reverse this when retrieving locations from db)
            String loc = ((MapActivity.locLatLng).toString()).substring(8);

            // CODE FOR REVERSING DONT DELETE
//            String[] latlong =  "lat/lng: (36.96378,-122.01857999999999)".split("(");
//            String[] latlong =  loc.split("(");
//            latlong = latlong[1].split(")");
//            latlong = latlong[0].split(",");;
//            double latitude = Double.parseDouble(latlong[0]);
//            double longitude = Double.parseDouble(latlong[1]);
//            LatLng location = new LatLng(latitude, longitude);

            // Create file metadata including the content type
            final StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("caption", cap)
                    .setCustomMetadata("location", loc)
                    .build();


            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(UploadActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                    // Update metadata properties right after the upload -----------
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
                    // -------------------------------------------------------------
                    finish();
                }
            });

            // ------------------------------------------------------------------------------------^
        }
    }
}
