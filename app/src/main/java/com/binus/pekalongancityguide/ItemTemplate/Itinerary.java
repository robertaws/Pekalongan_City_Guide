package com.binus.pekalongancityguide.ItemTemplate;

public class Itinerary {
    private String date;
    private String startTime;
    private String endTime;
    private String placeName;

    public Itinerary(){};

    public Itinerary(String date, String startTime, String endTime, String placeName) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.placeName = placeName;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPlaceName() {
        return placeName;
    }
}


