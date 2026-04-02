package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;

public class Ticket {
    @SerializedName("ticketNumber")
    private String ticketNumber;

    @SerializedName("seatNumber")
    private String seatNumber;

    @SerializedName("flightNumber")
    private String flightNumber;

    @SerializedName("departureAirport")
    private String departureAirport;

    @SerializedName("arrivalAirport")
    private String arrivalAirport;

    @SerializedName("departureTime")
    private String departureTime;

    @SerializedName("arrivalTime")
    private String arrivalTime;

    @SerializedName("classType")
    private String classType;

    // Getters and Setters
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getDepartureAirport() { return departureAirport; }
    public void setDepartureAirport(String departureAirport) { this.departureAirport = departureAirport; }

    public String getArrivalAirport() { return arrivalAirport; }
    public void setArrivalAirport(String arrivalAirport) { this.arrivalAirport = arrivalAirport; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
}