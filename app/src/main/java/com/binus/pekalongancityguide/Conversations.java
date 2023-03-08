package com.binus.pekalongancityguide;

public class Conversations {

    //int aksImg;

    String name, aksara, latin, english, indo;

    public Conversations(String name, String aksara, String latin, String english, String indo) {
        this.name = name;
        this.aksara = aksara;
        this.latin = latin;
        this.english = english;
        this.indo = indo;
        //this.aksImg = aksImg;
    }

//    public int getAksImg() {
//        return aksImg;
//    }
//
//    public void setAksImg(int aksImg) {
//        this.aksImg = aksImg;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAksara() {
        return aksara;
    }

    public void setAksara(String aksara) {
        this.aksara = aksara;
    }

    public String getLatin() {
        return latin;
    }

    public void setLatin(String latin) {
        this.latin = latin;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getIndo() {
        return indo;
    }

    public void setIndo(String indo) {
        this.indo = indo;
    }
}
