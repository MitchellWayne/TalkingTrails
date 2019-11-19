package com.example.untitledcs118proj;

import android.graphics.Bitmap;

// For custom info window
public class MarkerData {
    private Bitmap image;
    private String caption;
    // Add lnglat

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
