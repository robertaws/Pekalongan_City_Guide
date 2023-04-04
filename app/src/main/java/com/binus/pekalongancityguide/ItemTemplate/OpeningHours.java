package com.binus.pekalongancityguide.ItemTemplate;

import java.util.List;
public class OpeningHours {
    private List<String> weekday;

    public OpeningHours() {}

    public OpeningHours(List<String> weekday) {
        this.weekday = weekday;
    }

    public List<String> getWeekday() {
        return weekday;
    }

    public void setWeekday(List<String> weekday) {
        this.weekday = weekday;
    }
}








