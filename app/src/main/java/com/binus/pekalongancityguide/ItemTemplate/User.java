package com.binus.pekalongancityguide.ItemTemplate;

public class User {
    private String Email;
    private Favorites Favorites;
    private Itinerary itinerary;
    private String profileImage;
    private long timestamp;
    private String uid;
    private String userType;

    public User(){}

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public Favorites getFavorites() {
        return Favorites;
    }

    public void setFavorites(Favorites favorites) {
        Favorites = favorites;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}