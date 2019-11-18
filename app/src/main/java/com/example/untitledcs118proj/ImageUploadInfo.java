package com.example.untitledcs118proj;

public class ImageUploadInfo {
    public String imageCaption;

    public String imageURL;

    public ImageUploadInfo() {

    }

    public ImageUploadInfo(String caption, String url) {
        this.imageCaption = caption;
        this.imageURL = url;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    public String getImageURL() {
        return imageURL;
    }
}
