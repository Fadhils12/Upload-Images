package com.example.uploadimages;

public class DetailUpload {

    public String imageName;

    public String imageURL;

    private String key;

    public DetailUpload(){

    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }


    public String getImageName() {
        return imageName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public DetailUpload(String name, String url) {
        this.imageName = name;
        this.imageURL= url;
    }


}
