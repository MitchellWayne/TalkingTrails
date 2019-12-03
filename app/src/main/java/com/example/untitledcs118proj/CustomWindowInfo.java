package com.example.untitledcs118proj;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowInfo implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomWindowInfo(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Get custom window layout from xml
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_infowindow, null);

        ImageView markerImg = view.findViewById(R.id.markerImg);
        TextView markerCaption = view.findViewById(R.id.markerCaption);
        TextView markerProfName = view.findViewById(R.id.profName);

        // Get marker info from data object
        MarkerData data = (MarkerData) marker.getTag();

        // Get caption from marker metadata and set to custom info window
        markerCaption.setText(data.getCaption());

        // Get image and set to custom info window
        markerImg.setImageBitmap(data.getImage());

        // Set Profile Name
        markerProfName.setText(data.getUser() + ": ");

        return view;
    }
}
 