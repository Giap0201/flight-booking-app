package com.example.flight_booking_app.home.model;

public class TravelGuide {
    private String id;
    private String title;
    private String category;
    private String date;
    private String imageUrl;
    private String articleUrl;

    public TravelGuide(String id, String title, String category, String date, String imageUrl, String articleUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.imageUrl = imageUrl;
        this.articleUrl = articleUrl;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public String getArticleUrl() { return articleUrl; }
}