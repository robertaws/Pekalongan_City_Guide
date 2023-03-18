package com.binus.pekalongancityguide.ItemTemplate;

public class Destination {
    int destiImage;
    double destiLat,destiLong;
    String destiName, destiName2,destiDesc,destiAddress,destiTitle;

    public Destination(int destiImage, double destiLat, double destiLong, String destiName, String destiName2, String destiDesc, String destiAddress, String destiTitle) {
        this.destiImage = destiImage;
        this.destiLat = destiLat;
        this.destiLong = destiLong;
        this.destiName = destiName;
        this.destiName2 = destiName2;
        this.destiDesc = destiDesc;
        this.destiAddress = destiAddress;
        this.destiTitle = destiTitle;
    }

    public int getDestiImage() {
        return destiImage;
    }

    public void setDestiImage(int destiImage) {
        this.destiImage = destiImage;
    }

    public double getDestiLat() {
        return destiLat;
    }

    public void setDestiLat(double destiLat) {
        this.destiLat = destiLat;
    }

    public double getDestiLong() {
        return destiLong;
    }

    public void setDestiLong(double destiLong) {
        this.destiLong = destiLong;
    }

    public String getDestiName() {
        return destiName;
    }

    public void setDestiName(String destiName) {
        this.destiName = destiName;
    }

    public String getDestiName2() {
        return destiName2;
    }

    public void setDestiName2(String destiName2) {
        this.destiName2 = destiName2;
    }

    public String getDestiDesc() {
        return destiDesc;
    }

    public void setDestiDesc(String destiDesc) {
        this.destiDesc = destiDesc;
    }

    public String getDestiAddress() {
        return destiAddress;
    }

    public void setDestiAddress(String destiAddress) {
        this.destiAddress = destiAddress;
    }

    public String getDestiTitle() {
        return destiTitle;
    }

    public void setDestiTitle(String destiTitle) {
        this.destiTitle = destiTitle;
    }
}
