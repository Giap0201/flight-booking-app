package com.example.flight_booking_app.home.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Flight implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("flightNumber")
    private String flightNumber;

    @SerializedName("airlineName")
    private String airlineName;

    @SerializedName("origin")
    private String origin;

    @SerializedName("destination")
    private String destination;

    @SerializedName("departureTime")
    private String departureTime;

    @SerializedName("arrivalTime")
    private String arrivalTime;

    @SerializedName("status")
    private String status;

    @SerializedName("classes")
    private List<FlightClass> classes; // Mảng chứa các hạng vé

    // Lớp con đại diện cho 1 object bên trong mảng "classes"
    public static class FlightClass implements Serializable {
        @SerializedName("id")
        private String id;

        @SerializedName("className")
        private String className;

        @SerializedName("basePrice")
        private double basePrice;

        @SerializedName("availableSeats")
        private int availableSeats;

        public double getBasePrice() { return basePrice; }
        public String getClassName() { return className; }
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public String getAirlineName() { return airlineName; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public List<FlightClass> getClasses() { return classes; }
}