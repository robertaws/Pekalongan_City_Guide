package com.binus.pekalongancityguide.ItemTemplate;

public class Iter {
    private String itineraryName;
    private String date;
    private String destiId;
    private String url;

    public Iter(String itineraryName, String date, String destiId, String url) {
        this.itineraryName = itineraryName;
        this.date = date;
        this.destiId = destiId;
        this.url = url;
    }

    public String getItineraryName() {
        return itineraryName;
    }

    public void setItineraryName(String itineraryName) {
        this.itineraryName = itineraryName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDestiId() {
        return destiId;
    }

    public void setDestiId(String destiId) {
        this.destiId = destiId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
