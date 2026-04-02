package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;

public class BookingSummary {
    @SerializedName("id")
    private String id;

    @SerializedName("pnrCode")
    private String pnrCode; // Thay cho orderId trên UI

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("flightNumber")
    private String flightNumber;

    @SerializedName("origin")
    private String origin; // Sân bay đi (CGK)

    @SerializedName("destination")
    private String destination; // Sân bay đến (DPS)

    @SerializedName("departureTime")
    private String departureTime;

    // BỔ SUNG TRƯỜNG HẠNG VÉ
    @SerializedName("flightClass") // Tên key JSON mà Backend sẽ trả về
    private String flightClass;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPnrCode() { return pnrCode; }
    public void setPnrCode(String pnrCode) { this.pnrCode = pnrCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    // GETTER & SETTER CHO HẠNG VÉ
    public String getFlightClass() { return flightClass; }
    public void setFlightClass(String flightClass) { this.flightClass = flightClass; }
}