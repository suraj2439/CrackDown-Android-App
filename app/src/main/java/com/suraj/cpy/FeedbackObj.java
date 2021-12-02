package com.suraj.cpy;

public class FeedbackObj {
    int Rating;
    String userId, mosteLiked, leastLiked, comment, dateTime;

    public FeedbackObj() {
        Rating = -1;
        this.userId = "-";
        this.mosteLiked = "-";
        this.leastLiked = "-";
        this.comment = "-";
        this.dateTime = "-";
    }

    public FeedbackObj(int rating, String userId, String mosteLiked, String leastLiked, String comment) {
        Rating = rating;
        this.userId = userId;
        this.mosteLiked = mosteLiked;
        this.leastLiked = leastLiked;
        this.comment = comment;
    }

    public int getRating() {
        return Rating;
    }

    public void setRating(int rating) {
        Rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMosteLiked() {
        return mosteLiked;
    }

    public void setMosteLiked(String mosteLiked) {
        this.mosteLiked = mosteLiked;
    }

    public String getLeastLiked() {
        return leastLiked;
    }

    public void setLeastLiked(String leastLiked) {
        this.leastLiked = leastLiked;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
