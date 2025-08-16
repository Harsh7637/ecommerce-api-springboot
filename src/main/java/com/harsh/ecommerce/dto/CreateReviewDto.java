package com.harsh.ecommerce.dto;

public class CreateReviewDto {
    private String comment;
    private int rating;
    private String title; // Missing field causing issues

    public CreateReviewDto() {}

    public CreateReviewDto(String comment, int rating, String title) {
        this.comment = comment;
        this.rating = rating;
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}