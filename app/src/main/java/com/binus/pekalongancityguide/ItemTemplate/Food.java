package com.binus.pekalongancityguide.ItemTemplate;

public class Food {
    int foodImage, foodImage1, foodImage2, foodImage3;
    String foodName, foodName2,foodDesc;

    public Food(int foodImage, int foodImage1, int foodImage2, int foodImage3, String foodName, String foodName2, String foodDesc) {
        this.foodImage = foodImage;
        this.foodImage1 = foodImage1;
        this.foodImage2 = foodImage2;
        this.foodImage3 = foodImage3;
        this.foodName = foodName;
        this.foodName2 = foodName2;
        this.foodDesc = foodDesc;
    }

    public int getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(int foodImage) {
        this.foodImage = foodImage;
    }

    public int getFoodImage1() {
        return foodImage1;
    }

    public void setFoodImage1(int foodImage1) {
        this.foodImage1 = foodImage1;
    }

    public int getFoodImage2() {
        return foodImage2;
    }

    public void setFoodImage2(int foodImage2) {
        this.foodImage2 = foodImage2;
    }

    public int getFoodImage3() {
        return foodImage3;
    }

    public void setFoodImage3(int foodImage3) {
        this.foodImage3 = foodImage3;
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

    public String getFoodDesc() {
        return foodDesc;
    }

    public void setFoodDesc(String foodDesc) {
        this.foodDesc = foodDesc;
    }
}