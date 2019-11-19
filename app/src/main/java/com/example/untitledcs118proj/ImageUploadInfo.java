package com.example.untitledcs118proj;

public class ImageUploadInfo {

    public String imageCaption;
    public String imageURL;
    public String loc;

    public ImageUploadInfo() {
        // Default constructor
    }

    public ImageUploadInfo(ImageUploadInfo i) {
        this.imageCaption = i.imageCaption;
        this.imageURL = i.imageURL;
        this.loc = i.loc;
    }

    public ImageUploadInfo(String imageCaption, String imageURL, String loc) {
        this.imageCaption = imageCaption;
        this.imageURL = imageURL;
        this.loc = loc;
    }

    public String getimageCaption() {
        return imageCaption;
    }

    public String getimageURL() {
        return imageURL;
    }

    public String getloc() {
        return loc;
    }
}
