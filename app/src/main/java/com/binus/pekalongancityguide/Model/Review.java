package com.binus.pekalongancityguide.Model;
public class Review {
    private String reviewAuthor;
    private int reviewRating;
    private String review_text;

    public Review() {}

    public Review(String reviewAuthor, int reviewRating, String review_text) {
        this.reviewAuthor = reviewAuthor;
        this.reviewRating = reviewRating;
        this.review_text = review_text;
    }

    public String getReviewAuthor() {
        return reviewAuthor;
    }

    public void setReviewAuthor(String reviewAuthor) {
        this.reviewAuthor = reviewAuthor;
    }

    public int getReviewRating() {
        return reviewRating;
    }

    public void setReviewRating(int reviewRating) {
        this.reviewRating = reviewRating;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }
}

