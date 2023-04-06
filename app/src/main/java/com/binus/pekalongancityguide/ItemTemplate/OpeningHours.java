package com.binus.pekalongancityguide.ItemTemplate;

public class OpeningHours {
    private final String dayOfWeek;
    private final String openingHours;

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