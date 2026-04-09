package com.example.flight_booking_app.ticket.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingDetail {
    @SerializedName("id")
    private String id;

    @SerializedName("pnrCode")
    private String pnrCode;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("passengers")
    private List<Passenger> passengers;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPnrCode() { return pnrCode; }
    public void setPnrCode(String pnrCode) { this.pnrCode = pnrCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
}