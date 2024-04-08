package com.example.fiftysix;

public class AdminImage {

    private String posterID;
    private String defaultLink;
    private String imageLink;

    AdminImage(String posterID, String imageLink, String defaultLink){
        this.posterID = posterID;
        this.imageLink = imageLink;
        this.defaultLink = defaultLink;
    }

    public String getPosterID() {
        return posterID;
    }

    public String getDefaultLink() {
        return defaultLink;
    }

    public String getImageLink() {
        return imageLink;
    }
}
