package com.binus.pekalongancityguide.ItemTemplate;

import java.io.Serializable;

public class Destination implements Serializable{
    String uid,id,title,description,categoryId,url,rating,address;
    double desLat,desLong;
    boolean favorite;
    private boolean isLoaded;
    public Destination(){

    }

    public Destination(String uid, String id, String title, String description, String categoryId, String url, String rating, String address, double desLat, double desLong, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.rating = rating;
        this.address = address;
        this.desLat = desLat;
        this.desLong = desLong;
        this.favorite = favorite;
        this.isLoaded = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getDesLat() {
        return desLat;
    }

    public void setDesLat(double desLat) {
        this.desLat = desLat;
    }

    public double getDesLong() {
        return desLong;
    }

    public void setDesLong(double desLong) {
        this.desLong = desLong;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }
}
