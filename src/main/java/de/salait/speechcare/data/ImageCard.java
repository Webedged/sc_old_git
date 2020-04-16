package de.salait.speechcare.data;

/**
 * Created by speechcare on 23.04.18.
 */

public class ImageCard {


    private String title;
    private String imagemdediaID;
    private String mediapath;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagemdediaID() {
        return imagemdediaID;
    }

    public void setImagemdediaID(String mediaid) {
        this.imagemdediaID = mediaid;
    }


    public String getMediapath() {
        return mediapath;
    }

    public void setMediapath(String mediafile) {
        this.mediapath = mediafile;
    }

}
