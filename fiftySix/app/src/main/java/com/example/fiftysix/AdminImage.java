package com.example.fiftysix;
/**
 * Image object used to pass image data from AdminBrowseEvents to ImageAdapter
 * @author Brady
 * @version 1
 * @since SDK34
 */
public class AdminImage {

    private String posterID;
    private String defaultLink;
    private String imageLink;

    /**
     * Creates AdminImage object.
     *
     * @param posterID String of image ID in database.
     * @param imageLink String of imageURL in database.
     * @param defaultLink String of default/Stock imageURL in database.
     */
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
