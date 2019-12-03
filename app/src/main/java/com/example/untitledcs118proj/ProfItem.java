package com.example.untitledcs118proj;

import android.graphics.Bitmap;

public class ProfItem {

    private Bitmap img;
    private String viewcount;
    private String caption;


    public ProfItem(String count, String caption, Bitmap img) {
        this.img = img;
        this.viewcount = count;
        this.caption = caption;
    }

    public Bitmap getImg() {
        return this.img;
    }

    public String getViewCount() {
        return this.viewcount;
    }

    public String getCaption() {
        return this.caption;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public void setViewCount(String count) {
        this.viewcount = count;
    }

    public void setCaption(String title) {
        this.caption = title;
    }

}
