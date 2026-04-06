package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class BookingSummary {
    private String id;
    private String pnrCode;
    private String status;
    private BigDecimal totalAmount;
    private String createdAt;
    private String flightNumber;
    private String origin;      // BE trả về tên đầy đủ (Airport Name)
    private String destination; // BE trả về tên đầy đủ (Airport Name)
    private String departureTime;

    @SerializedName("arrivalTime")
    private String arrivalTime;

    @SerializedName("classType")
    private String classType;

    @SerializedName("passengerCount")
    private Integer passengerCount;

    // Getters
    public String getId() { return id; }
    public String getPnrCode() { return pnrCode; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCreatedAt() { return createdAt; }
    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getClassType() { return classType; }
    public Integer getPassengerCount() { return passengerCount; }
}