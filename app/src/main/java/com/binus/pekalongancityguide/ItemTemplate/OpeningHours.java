package com.binus.pekalongancityguide.ItemTemplate;

public class OpeningHours {
    private String dayOfWeek;
    private String openingHours;

    public OpeningHours(String dayOfWeek, String openingHours) {
        this.dayOfWeek = dayOfWeek;
        this.openingHours = openingHours;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getOpeningHours() {
        return openingHours;
    }
}