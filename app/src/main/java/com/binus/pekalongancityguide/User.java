package com.binus.pekalongancityguide;

public class User {
    private String uid;
    private String email;
    private String username;
    private String profileImage; // new field

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String email, String username) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.profileImage = ""; // initialize the profile image field to an empty string
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() { // new getter method
        return profileImage;
    }

    public void setProfileImage(String profileImage) { // new setter method
        this.profileImage = profileImage;
    }
}