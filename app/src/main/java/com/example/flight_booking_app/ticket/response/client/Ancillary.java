package com.example.flight_booking_app.ticket.response.client;

public class Ancillary {
    private String catalogName;
    private String type; // VD: "BAGGAGE"
    private double amount;

    // Getters
    public String getCatalogName() { return catalogName; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
}
