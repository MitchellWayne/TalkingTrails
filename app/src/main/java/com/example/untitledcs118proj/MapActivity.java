package com.example.untitledcs118proj;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.content.Intent;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.maps.android.SphericalUtil;

public class MapActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    // Location Services
    static GoogleMap mMap;
    private LocationListener locListen;
    private LocationManager locManager;
    private final long MIN_UPDATE_TIME = 1000; // 1000ms = 1s
    private final long MIN_DISTANCE = 5; // 5 meters
    static LatLng locLatLng;
    private static final String TAG = MapActivity.class.getSimpleName();

    // Background thread stuff
    static ArrayList<ImageUploadInfo> popList = new ArrayList<ImageUploadInfo>();
    static ArrayList<ImageUploadInfo> usrList = new ArrayList<ImageUploadInfo>();
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
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot item_snapshot:dataSnapshot.getChildren()) {
                        ImageUploadInfo markerData = item_snapshot.getValue(ImageUploadInfo.class);
                        plMarkerList.add(markerData);
                        if (markerData.getuser().equals(MainActivity.currentProfile.getUsername())){
                            usrList.add(markerData);
                        }
                    }

                    /*
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

                        // Proximity Check (1mi)
                        // If exceeds max radius, cull
                        if (SphericalUtil.computeDistanceBetween(location, MapActivity.locLatLng) > 1609) {
                            Log.d("PL remove", "Loop" + i);
                            plMarkerList.remove(i);
                            Log.d("PL remove cap", "" + plMarkerList.get(i).imageCaption);
                        }
                    }
                    */

                    // Use method for copy constructor here
                    populatepopList(plMarkerList);
                    // Also init new bool array
                    inProx = new boolean[plMarkerList.size()];

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    };

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseDatabase database;
    static DatabaseReference databaseReference;

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

        // AR switch and listener
        // Replace changed intent
        /*
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
        */
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMyLocationEnabled(true);


        // Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("images/");

        // Custom Info Window
        CustomWindowInfo customInfoWindow = new CustomWindowInfo(this);
        mMap.setInfoWindowAdapter(customInfoWindow);
        mMap.setOnMarkerClickListener(this);

        // Add custom Map Style
        boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        // Start Background Threads
        // ProxCheck will be delayed by 3s to give PopList time

        if (pl.getStatus() != AsyncTask.Status.RUNNING) {
            pl.execute();
        }
        if (pc.getStatus() != AsyncTask.Status.RUNNING) {
            pc.execute();
        }


        // -----------------------------------------------------------------------------------------

        // Location Services
        locListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    locLatLng = new LatLng(location.getLatitude(), location.getLongitude());
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

    public void upload(View view) {
        Intent it = new Intent(this,UploadActivity.class);
        startActivity(it);
    }

    public void profile(View view) {
        Intent it = new Intent(this, ProfileActivity.class);
        startActivity(it);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final MarkerData m = (MarkerData) marker.getTag();

        Uri fp = Uri.parse(m.getFilepath());
        final DatabaseReference dR = FirebaseDatabase.getInstance()
                .getReference("images/" + fp.getLastPathSegment());

        dR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ImageUploadInfo markerData = new ImageUploadInfo(dataSnapshot.getValue(ImageUploadInfo.class));
                markerData.setviews(markerData.getviews() + 1);
                dR.setValue(markerData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return false;
    }

    // Background Threads -------------------------------------------------------------------------\

    // Returns latest culled list
    private class PopulateList extends AsyncTask<Void, Void,ArrayList<ImageUploadInfo>> {

        Handler plHandler0 = new Handler(); // I don't know why I made two but I'm afraid it will break
        Handler plHandler = new Handler();
        int plDelay = 60000;

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
                            final String dfilepath = dmarker.getimageURL();
                            Uri dURI = Uri.parse(dfilepath);

                            // Get Username
                            final String duser = dmarker.getuser();

                            // Get Views
                            final int dviews = dmarker.getviews();


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
                                        mData.setUser(duser);
                                        mData.setViews(dviews);
                                        mData.setFilepath(dfilepath);
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

