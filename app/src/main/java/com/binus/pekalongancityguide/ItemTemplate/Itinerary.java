package com.binus.pekalongancityguide.ItemTemplate;

public class Itinerary {
    private final String date;
    private final String endTime;
    private final String placeName;
    private final String startTime;
    private final double latitude;
    private final double longitude;
    private float distance;

    public Itinerary(String date, String endTime, String placeName, String startTime, double latitude, double longitude, float distance) {
        this.date = date;
        this.endTime = endTime;
        this.placeName = placeName;
        this.startTime = startTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getStartTime() {
        return startTime;
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

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
