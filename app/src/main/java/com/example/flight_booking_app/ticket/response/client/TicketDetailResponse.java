package com.example.flight_booking_app.ticket.response.client;

import java.util.List;

public class TicketDetailResponse {
    private String ticketNumber;
    private String status;
    private String seatNumber;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime; // ĐÃ CÓ
    private String classType;   // ĐÃ CÓ
    private List<Ancillary> ancillaries;
    private double totalAmount;

    public double getTotalAmount() { return totalAmount; }

    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public String getStatus() { return status; }
    public String getSeatNumber() { return seatNumber; }
    public String getFlightNumber() { return flightNumber; }
    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getClassType() { return classType; }
    public List<Ancillary> getAncillaries() { return ancillaries; }
}