package com.example.untitledcs118proj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class UploadActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    ImageView imageToUpload;
    ImageButton uploadButton, cameraButton;
    EditText caption;
    Uri filepath;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        //Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

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

            ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PackageManager.PERMISSION_GRANTED);

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
            // this is on current user side only
            String cap = caption.getText().toString().trim();
            try {
                // Change image to generic symbol
                Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filepath);

                Marker marker = MapActivity.mMap.addMarker(new MarkerOptions().position(MapActivity.locLatLng).title(cap)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // Add caption and image to marker metadata
                // Attach user as well
                // For info window
                MarkerData mData = (MarkerData) new MarkerData();
                mData.setCaption(cap);
                mData.setImage(b);
                mData.setUser(MainActivity.currentProfile.getUsername());
                mData.setViews(0);
                mData.setFilepath(filepath.toString());
                marker.setTag(mData);

            } catch (IOException e) {
                e.printStackTrace();
            }
            // ------------------------------------------------------------------------------------^

            // Get location
            // (This requires splitting lat and long to cast to string)
            // (Remember to reverse this when retrieving locations from db)
            String loc = ((MapActivity.locLatLng).toString()).substring(8);

            // Store to firebase ------------------------------------------------------------------v

            // Store to storage
            final StorageReference ref = storageReference.child("images/"+filepath.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(filepath);

            // Store to database
            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(cap, filepath.toString(), loc,
                    MainActivity.currentProfile.getUsername(), 0);
            String imageUploadID = databaseReference.push().getKey();
            databaseReference.child("images/" + filepath.getLastPathSegment()).setValue(imageUploadInfo);

            // ------------------------------------------------------------------------------------^

            // Arraylist "populate"
            final ArrayList<ImageUploadInfo> pMarkerList = new ArrayList<ImageUploadInfo>();

            Runnable plRunOnce = new Runnable() {
                @Override
                public void run() {
                    // Clear and repopulate arraylist "imgList"
                    pMarkerList.clear();

                    // get data from firebase and store to array.
                    MapActivity.databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot item_snapshot:dataSnapshot.getChildren()) {
                                ImageUploadInfo markerData = item_snapshot.getValue(ImageUploadInfo.class);
                                pMarkerList.add(markerData);
                            }

                            // Cull array based off of large proximity subset
                            // 1 mile max proximity, or 1609 meters
                            for (int i = 0; i < pMarkerList.size(); i++){
                                ImageUploadInfo dmarker = pMarkerList.get(i);
                                // Get Location
                                String dloc = dmarker.getloc();
                                String[] latlong =  dloc.split("\\(");
                                latlong = latlong[1].split("\\)");
                                latlong = latlong[0].split(",");
                                double latitude = Double.parseDouble(latlong[0]);
                                double longitude = Double.parseDouble(latlong[1]);
                                final LatLng location = new LatLng(latitude, longitude);

                                // Proximity Check (1mi)
                                // If within max radius, cull
                                if (SphericalUtil.computeDistanceBetween(location, MapActivity.locLatLng) < 1609) {
                                    pMarkerList.remove(i);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    // Use method for copy constructor here
                    MapActivity.populatepopList(pMarkerList);
                    // Also init new bool array
                    MapActivity.inProx = new boolean[pMarkerList.size()];
                }
            };

            // Update stuff on main popList
            Handler outerHandle = new Handler();
            outerHandle.post(plRunOnce);



            finish();
        }
    }
}
