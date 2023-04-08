package com.binus.pekalongancityguide.ItemTemplate;

public class Itinerary {
    private String date,endTime,placeId,startTime;
    public Itinerary() {
    }
    public Itinerary(String date, String endTime, String placeId, String startTime) {
        this.date = date;
        this.endTime = endTime;
        this.placeId = placeId;
        this.startTime = startTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}


