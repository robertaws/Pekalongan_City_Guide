package com.binus.pekalongancityguide.Model;

public class Bookmark {
    private String destiId;
    private long timestamp;
    private String uid;

    public Bookmark() {
    }

    public Bookmark(String destiId, long timestamp, String uid) {
        this.destiId = destiId;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getDestiId() {
        return destiId;
    }

    public void setDestiId(String destiId) {
        this.destiId = destiId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
