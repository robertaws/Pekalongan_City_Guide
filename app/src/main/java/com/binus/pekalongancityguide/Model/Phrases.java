package com.binus.pekalongancityguide.Model;

public class Phrases {
    String aksara, latin, english, indo;

    public Phrases(String aksara, String latin, String english, String indo) {
        this.aksara = aksara;
        this.latin = latin;
        this.english = english;
        this.indo = indo;
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
