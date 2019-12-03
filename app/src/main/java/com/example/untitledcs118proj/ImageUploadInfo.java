package com.example.untitledcs118proj;

public class ImageUploadInfo {

    public String imageCaption;
    public String imageURL;
    public String loc;
    public String user;
    public int views;

    public ImageUploadInfo() {
        // Default constructor
    }

    public ImageUploadInfo(ImageUploadInfo i) {
        this.imageCaption = i.imageCaption;
        this.imageURL = i.imageURL;
        this.loc = i.loc;
        this.user = i.user;
        this.views = i.views;
    }

    public ImageUploadInfo(String imageCaption, String imageURL, String loc, String user, int views) {
        this.imageCaption = imageCaption;
        this.imageURL = imageURL;
        this.loc = loc;
        this.user = user;
        this.views = views;
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

    public String getuser() {
        return user;
    }

    public int getviews() {
        return views;
    }

    public void setviews(int i){
        this.views = i;
    }
}
