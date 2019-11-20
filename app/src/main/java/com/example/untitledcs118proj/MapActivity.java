package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

// ....
//import com.google.maps.android.SphericalUtil;

import java.net.URI;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

// Firebase
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.maps.android.SphericalUtil;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback{

    // Location Services
    static GoogleMap mMap;
    private LocationListener locListen;
    private LocationManager locManager;
    private final long MIN_UPDATE_TIME = 1000; // 1000ms = 1s
    private final long MIN_DISTANCE = 5; // 5 meters
    static LatLng locLatLng;
    private static final String TAG = MapActivity.class.getSimpleName();

    // Looping stuff for updating array
    ArrayList<ImageUploadInfo> markerList = new ArrayList<ImageUploadInfo>();
    Handler handler = new Handler();
    int delay = 10000; //milliseconds (10s)

    // Looping stuff for updating proximity
    Handler proxHandler = new Handler();
    int proxDelay = 5000; //5s

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    //widget initialization
    Switch ar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get handle to fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
            mAuth.signInAnonymously();
        } else {
            mAuth.signInAnonymously();
        }

        // AR switch and listener
        // Replace changed intent
        ar = (Switch) findViewById(R.id.mapAR);
        ar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
                //reset switch
                ar.setChecked(false);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMyLocationEnabled(true);

        // Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        // Custom Info Window
        CustomWindowInfo customInfoWindow = new CustomWindowInfo(this);
        mMap.setInfoWindowAdapter(customInfoWindow);

        // Add custom Map Style
        boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        // Refresh object array every 30s and determine which objects get saved as markers or drawables ------
        handler.postDelayed(new Runnable(){
            public void run(){
                // Clear and repopulate arraylist "imgList"
                markerList.clear();
                mMap.clear();

                // get data from firebase and store to array.
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot item_snapshot:dataSnapshot.getChildren()) {
                            ImageUploadInfo markerData = item_snapshot.getValue(ImageUploadInfo.class);
                            markerList.add(markerData);
                            Log.d("TAG populate", markerData.getimageCaption());
                        }

                        // (TEMP) Update Markers on Map
                        for (int i = 0; i < markerList.size(); i++){
                            ImageUploadInfo dmarker = markerList.get(i);
                            // Get Location
                            String dloc = dmarker.getloc();
                            String[] latlong =  dloc.split("\\(");
                            latlong = latlong[1].split("\\)");
                            latlong = latlong[0].split(",");;
                            double latitude = Double.parseDouble(latlong[0]);
                            double longitude = Double.parseDouble(latlong[1]);
                            final LatLng location = new LatLng(latitude, longitude);
                            Log.d("TAG iterate", "Loop" + i);

                            // Proximity Check (10m)
                            // If within user radius, parse rest
                            if (SphericalUtil.computeDistanceBetween(location, MapActivity.locLatLng) < 10) {
                                // Get Caption
                                final String dcap = dmarker.getimageCaption();

                                // Get filepath
                                String dfilepath = dmarker.getimageURL();
                                Uri dURI = Uri.parse(dfilepath);

                                // Get bitmap image
                                final Bitmap[] dbitmap = new Bitmap[1];
                                StorageReference ref = storageReference.child("images/"+dURI.getLastPathSegment());
                                Log.d("URI", "images/"+dURI.getLastPathSegment());
                                try {
                                    final File localFile = File.createTempFile("Images", "bmp");
                                    ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            dbitmap[0] = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            Log.d("Image Get", "Success");

                                            // Add marker
                                            Marker marker = MapActivity.mMap.addMarker(new MarkerOptions().position(location).title(dcap));

                                            // Add caption and image to marker metadata
                                            // For info window
                                            MarkerData mData = (MarkerData) new MarkerData();
                                            mData.setCaption(dcap);
                                            mData.setImage(dbitmap[0]);
                                            marker.setTag(mData);
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
                            }

                            // Else set as circle
                            else {
                                CircleOptions circleOptions = new CircleOptions()
                                        .center(location)
                                        .radius(10); // In meters

                                // Get back the mutable Circle
                                Circle circle = mMap.addCircle(circleOptions);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                handler.postDelayed(this, delay);
            }
        }, delay);

        // -----------------------------------------------------------------------------------------

        // Location Services
        locListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    locLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(locLatLng).title("Location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_DISTANCE, locListen);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

   /* public void profile(View view) {
        Intent it = new Intent(this,ProfileActivity.class);
        startActivity(it);
    }*/


    public void upload(View view) {
        Intent it = new Intent(this,UploadActivity.class);
        startActivity(it);
    }


}

