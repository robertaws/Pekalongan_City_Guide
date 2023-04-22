package com.binus.pekalongancityguide.ItemTemplate;

public class Comments{
    String id,destiId,timestamp,comment,uid;
    public Comments(){}

    public Comments(String id, String destiId, String timestamp, String comment, String uid) {
        this.id = id;
        this.destiId = destiId;
        this.timestamp = timestamp;
        this.comment = comment;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestiId() {
        return destiId;
    }

    public void setDestiId(String destiId) {
        this.destiId = destiId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
