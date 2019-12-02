package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Arrays;

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

    // Background thread stuff
    static ArrayList<ImageUploadInfo> popList;
    static boolean[] inProx;
    ArrayList<ImageUploadInfo> plMarkerList = new ArrayList<ImageUploadInfo>();

    // Start Background Threads
    private PopulateList pl = new PopulateList();
    // ProxCheck will be delayed by 3s to give PopList time
    private ProximityCheck pc = new ProximityCheck();


    // Arraylist "populate"
    Runnable plRunOnce = new Runnable() {
        @Override
        public void run() {
            // Clear and repopulate arraylist "imgList"
            plMarkerList.clear();

            // get data from firebase and store to array.
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot item_snapshot:dataSnapshot.getChildren()) {
                        ImageUploadInfo markerData = item_snapshot.getValue(ImageUploadInfo.class);
                        plMarkerList.add(markerData);
                        //Log.d("PL populate", markerData.getimageCaption());
                    }

                    Log.d("PL size", "" + plMarkerList.size() );

                    // Cull array based off of large proximity subset
                    // 1 mile max proximity, or 1609 meters
                    for (int i = 0; i < plMarkerList.size(); i++){
                        ImageUploadInfo dmarker = plMarkerList.get(i);
                        // Get Location
                        String dloc = dmarker.getloc();
                        String[] latlong =  dloc.split("\\(");
                        latlong = latlong[1].split("\\)");
                        latlong = latlong[0].split(",");
                        double latitude = Double.parseDouble(latlong[0]);
                        double longitude = Double.parseDouble(latlong[1]);
                        final LatLng location = new LatLng(latitude, longitude);
                        //Log.d("PL iterate", "Loop" + i);

                        // Proximity Check (1mi)
                        // If within max radius, cull
                        if (SphericalUtil.computeDistanceBetween(location, MapActivity.locLatLng) > 1609) {
                            Log.d("PL remove", "Loop" + i);
                            plMarkerList.remove(i);
                            Log.d("PL remove cap", "" + plMarkerList.get(i).imageCaption);
                        }
                    }

                    Log.d("PL size post cull", "" + plMarkerList.size() );

                    // Use method for copy constructor here
                    populatepopList(plMarkerList);
                    // Also init new bool array
                    inProx = new boolean[plMarkerList.size()];

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//            // Use method for copy constructor here
//            populatepopList(plMarkerList);
//            // Also init new bool array
//            inProx = new boolean[plMarkerList.size()];
        }
    };

    /*
    // Live-ish Location
    // To re-implement, potentially just init user location right after the clear lmao
    Circle userProx, user;
    CircleOptions proxOptions = new CircleOptions()
            .radius(30) // in meters
            .strokeWidth(3)
            .strokeColor(0xffffffff)
            .fillColor(0x113ddbff);

    CircleOptions userOptions = new CircleOptions()
            .radius(4) // in meters
            .strokeWidth(2)
            .fillColor(0xff0061bd)
            .strokeColor(0xffffffff);
     */

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
    protected void onStart() {
        super.onStart();
//        if (pl.getStatus() != AsyncTask.Status.RUNNING) {
//            pl.execute();
//        }
//        if (pc.getStatus() != AsyncTask.Status.RUNNING) {
//            pc.execute();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (pl.getStatus() != AsyncTask.Status.RUNNING) {
//            pl.execute();
//        }
//        if (pc.getStatus() != AsyncTask.Status.RUNNING) {
//            pc.execute();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (pl.getStatus() == AsyncTask.Status.RUNNING) {
//            pl.stop();
//            pl.cancel(true);
//        }
//        if (pc.getStatus() == AsyncTask.Status.RUNNING) {
//            pc.stop();
//            pc.cancel(true);
//        }
    }

    @Override
    protected void onStop(){
        super.onStop();
//        if (pl.getStatus() == AsyncTask.Status.RUNNING) {
//            pl.stop();
//            pl.cancel(true);
//        }
//        if (pc.getStatus() == AsyncTask.Status.RUNNING) {
//            pc.stop();
//            pc.cancel(true);
//        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMyLocationEnabled(true);
        //Dummy circle
//        CircleOptions dummyOpts = new CircleOptions()
//                .center(new LatLng(36.997409,-122.055591))
//                .strokeColor(0x00000000);
//        userProx = mMap.addCircle(dummyOpts);
//        user = mMap.addCircle(dummyOpts);

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

        // Start Background Threads
        // ProxCheck will be delayed by 3s to give PopList time
        Log.d("Where", "Before .exe");

        if (pl.getStatus() != AsyncTask.Status.RUNNING) {
            Log.d("Where", "in pl cancel");

            pl.execute();
        }
        if (pc.getStatus() != AsyncTask.Status.RUNNING) {
            Log.d("Where", "in pc cancel");

            pc.execute();
        }
        Log.d("Where", "After .exe");


        // -----------------------------------------------------------------------------------------

        // Location Services
        locListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
//                    user.remove();
//                    userProx.remove();
                    locLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(locLatLng).title("Location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 17));
//                    userProx = mMap.addCircle(proxOptions.center(locLatLng));
//                    user = mMap.addCircle(userOptions.center(locLatLng));
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

//        if (pl.getStatus() == AsyncTask.Status.RUNNING) {
//            pl.stop();
//            pl.cancel(true);
//        }
//        if (pc.getStatus() == AsyncTask.Status.RUNNING) {
//            pc.stop();
//            pc.cancel(true);
//        }

        startActivity(it);
    }

    // Background Threads -------------------------------------------------------------------------\

    // Returns latest culled list
    private class PopulateList extends AsyncTask<Void, Void,ArrayList<ImageUploadInfo>> {

        Handler plHandler0 = new Handler(); // I don't know why I made two but I'm afraid it will break
        Handler plHandler = new Handler();
        int plDelay = 10000;

        Runnable plRun = new Runnable() {
            @Override
            public void run() {
                plHandler0.post(plRunOnce);
                plHandler0.postDelayed(this, plDelay);
            }
        };

        @Override
        protected ArrayList<ImageUploadInfo> doInBackground(Void... voids) {
            // Run plRun with no delay on startup
            plHandler.post(plRun);
            return null;
        }

        public void stop(){
            plHandler.removeCallbacks(plRun);
        }
    }

    private class ProximityCheck extends AsyncTask<Void, Void,ArrayList<ImageUploadInfo>> {

        // Stuff for checking array
        // Copy latest updated popList
        ArrayList<ImageUploadInfo> pcProxList;
        // To determine whether or not the map is updated, we compare latest constructed bool array
        // to a previous bool array and see if there were any changes
        //boolean[] inProx;
        boolean[] prevProx = new boolean[0];
        Handler pcHandler = new Handler();
        int pcDelay = 2000;

        // Arraylist Proximity Check
        Runnable pcRun = new Runnable() {
            @Override
            public void run() {
                // Re-initialize proxList and array
                pcProxList = new ArrayList<ImageUploadInfo>(popList);
                inProx = new boolean[pcProxList.size()];

                // If size of inProx != prevProx, database was updated
                // Then, we must re-initialize our prevProx
                if (inProx.length != prevProx.length) {
                    prevProx = new boolean[pcProxList.size()];
                }

                //Log.d("PC", "Run + popsize" + popList.size());


                // Run proximity calc on pcProxList
                for (int i = 0; i < pcProxList.size(); i++) {
                    ImageUploadInfo dmarker = pcProxList.get(i);
                    // Get Location
                    String dloc = dmarker.getloc();
                    String[] latlong = dloc.split("\\(");
                    latlong = latlong[1].split("\\)");
                    latlong = latlong[0].split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    final LatLng location = new LatLng(latitude, longitude);
                    //Log.d("TAG iterate", "Loop" + i);

                    // Proximity Check (30m)
                    // If within user radius, toggle bool true
                    if (SphericalUtil.computeDistanceBetween(location, MapActivity.locLatLng) < 30) {
                        inProx[i] = true;
                    }
                    // Else toggle bool false
                    else {
                        inProx[i] = false;
                    }
                }

                // Compare boolean arrays and decide whether to refresh map
                if (!(Arrays.equals(prevProx,inProx))) {
                    Log.d("PC", "Clearing Map");
                    mMap.clear();

                    for (int i = 0; i < pcProxList.size(); i++) {
                        ImageUploadInfo dmarker = pcProxList.get(i);
                        // Get Location
                        String dloc = dmarker.getloc();
                        String[] latlong = dloc.split("\\(");
                        latlong = latlong[1].split("\\)");
                        latlong = latlong[0].split(",");
                        double latitude = Double.parseDouble(latlong[0]);
                        double longitude = Double.parseDouble(latlong[1]);
                        final LatLng location = new LatLng(latitude, longitude);
                        //Log.d("TAG iterate", "Loop" + i);

                        if (inProx[i] == true) {
                            // Get Caption
                            final String dcap = dmarker.getimageCaption();

                            // Get filepath
                            String dfilepath = dmarker.getimageURL();
                            Uri dURI = Uri.parse(dfilepath);

                            // Get bitmap image
                            final Bitmap[] dbitmap = new Bitmap[1];
                            StorageReference ref = storageReference.child("images/"+dURI.getLastPathSegment());
                            try {
                                final File localFile = File.createTempFile("Images", "bmp");
                                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        dbitmap[0] = BitmapFactory.decodeFile(localFile.getAbsolutePath());

                                        // Add marker
                                        Marker marker = MapActivity.mMap.addMarker(new MarkerOptions().position(location)
                                                .title(dcap)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
                            // Should probably move this outside
                            CircleOptions circleOptions = new CircleOptions()
                                    .center(location)
                                    .radius(5) // in meters
                                    .fillColor(0xffad0000)
                                    .strokeWidth(4);

                            CircleOptions proxOptions = new CircleOptions()
                                    .center(location)
                                    .radius(30) // in meters
                                    .strokeWidth(3)
                                    .strokeColor(0xffffffff)
                                    .fillColor(0x113ddbff);

                            // Get back the mutable Circle
                            mMap.addCircle(circleOptions);
                            mMap.addCircle(proxOptions);
                        }
                    }
                }

                // Set new previous prox boolean array
                prevProx = inProx.clone();

                pcHandler.postDelayed(this, pcDelay);
            }
        };

        @Override
        protected ArrayList<ImageUploadInfo> doInBackground(Void... voids) {
            //Log.d("Where", "pc dib");
            pcHandler.postDelayed(pcRun, pcDelay);
            return null;
        }

        public void stop(){
            pcHandler.removeCallbacks(pcRun);
        }

    }
    // --------------------------------------------------------------------------------------------/

    // Helper method for re-populating popList
    static public void populatepopList(ArrayList<ImageUploadInfo> plList){
        popList = new ArrayList<ImageUploadInfo>(plList);
    }
}

