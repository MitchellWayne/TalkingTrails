package com.example.untitledcs118proj;

import android.graphics.Bitmap;

// For custom info window
public class MarkerData {
    private Bitmap image;
    private String caption;
    private String user;
    private int views;
    private String filepath;

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

    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }

    public int getViews() { return views; }

    public void setViews(int views) { this.views = views; }

    public String getFilepath() { return filepath; }

    public void setFilepath(String filepath) { this.filepath = filepath; }

}
