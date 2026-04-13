package com.example.flight_booking_app.home.model;

public class Destination {
    private String id;
    private String name;
    private String price;
    private String imageUrl;

    public Destination(String id, String name, String price, String imageUrl) {
        this.id = id; this.name = name; this.price = price; this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}