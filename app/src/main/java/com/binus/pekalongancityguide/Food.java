package com.binus.pekalongancityguide;

public class Food {
    int foodImage;
    String foodName, foodName2;

    public Food(int foodImage, String foodName, String foodName2) {
        this.foodImage = foodImage;
        this.foodName = foodName;
        this.foodName2 = foodName2;
    }

    public int getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(int foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodName2() {
        return foodName2;
    }

    public void setFoodName2(String foodName2) {
        this.foodName2 = foodName2;
    }
}
