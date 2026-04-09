package com.example.flight_booking_app.search.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FlightClass implements Serializable {
    @SerializedName("className")
    private String className;

    @SerializedName("basePrice")
    private double basePrice;

    @SerializedName("availableSeats")
    private int availableSeats;

    // Getters
    public String getClassName() { return className; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
}