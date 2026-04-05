package com.example.flight_booking_app.booking.response.client;

import java.math.BigDecimal;

public class TicketDetailResponse {
    private String ticketNumber;
    private String status;
    private String seatNumber;
    private BigDecimal totalAmount;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
    private String classType;

    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public String getStatus() { return status; }
    public String getSeatNumber() { return seatNumber; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getFlightNumber() { return flightNumber; }
    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getClassType() { return classType; }
}