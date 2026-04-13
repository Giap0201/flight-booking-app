package com.example.flight_booking_app.search.model;

import com.google.gson.annotations.SerializedName;

public class SearchRequest {
    @SerializedName("origin") private String origin;
    @SerializedName("destination") private String destination;
    @SerializedName("date") private String date;
    @SerializedName("passengers") private int passengers;

    public SearchRequest(String origin, String destination, String date, int passengers) {
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.passengers = passengers;
    }

    // Bổ sung các hàm Getter
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public int getPassengers() { return passengers; }
}