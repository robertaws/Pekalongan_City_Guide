package com.binus.pekalongancityguide.ItemTemplate;

import java.io.Serializable;

public class Itinerary implements Serializable {
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String placeName;
    private final String destiId;
    private final String url;
    private final String durationText;
    private final String uid;
    private final double latitude;
    private final double longitude;
    private final float distance;

    public Itinerary(String date, String startTime, String endTime, String placeName, String destiId, String url, String durationText, String uid, double latitude, double longitude, float distance) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.placeName = placeName;
        this.destiId = destiId;
        this.url = url;
        this.durationText = durationText;
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
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

    public String getDestiId() {
        return destiId;
    }

    public String getUrl() {
        return url;
    }

    public String getDurationText() {
        return durationText;
    }

    public String getUid() {
        return uid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getDistance() {
        return distance;
    }
}
