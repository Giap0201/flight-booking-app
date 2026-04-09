package com.example.flight_booking_app.search.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Flight implements Serializable {
    @SerializedName("flightNumber")
    private String flightNumber;

    @SerializedName("airlineName")
    private String airlineName;

    @SerializedName("origin")
    private String origin;

    @SerializedName("destination")
    private String destination;

    @SerializedName("departureTime")
    private String departureTime; // ISO-8601 String từ LocalDateTime

    @SerializedName("arrivalTime")
    private String arrivalTime;

    @SerializedName("status")
    private String status;

    @SerializedName("classes")
    private List<FlightClass> classes; // Danh sách các hạng vé

    // Getters
    public String getFlightNumber() { return flightNumber; }
    public String getAirlineName() { return airlineName; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getStatus() { return status; }
    public List<FlightClass> getClasses() { return classes; }
}