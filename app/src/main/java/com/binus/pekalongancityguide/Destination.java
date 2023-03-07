package com.binus.pekalongancityguide;

public class Destination {
    int destiImage;
    String destiName, destiName2;

    public Destination(int destiImage, String destiName, String destiName2) {
        this.destiImage = destiImage;
        this.destiName = destiName;
        this.destiName2 = destiName2;
    }

    public String getDestiName2() {
        return destiName2;
    }

    public void setDestiName2(String destiName2) {
        this.destiName2 = destiName2;
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
