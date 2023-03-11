package com.binus.pekalongancityguide.Misc;

import java.util.List;

public class Article {
    private String title;
    private String summary;
    private String url;
    private List<Media> media;

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

    public List<Media> getMedia() {
        return media;
    }
}
