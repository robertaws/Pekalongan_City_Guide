package com.binus.pekalongancityguide;

public class Destination {
    int destiImage;
    String destiName;

    public Destination(int destiImage, String destiName) {
        this.destiImage = destiImage;
        this.destiName = destiName;
    }

    public int getDestiImage() {
        return destiImage;
    }

    public void setDestiImage(int destiImage) {
        this.destiImage = destiImage;
    }

    public String getDestiName() {
        return destiName;
    }

    public void setDestiName(String destiName) {
        this.destiName = destiName;
    }
}
