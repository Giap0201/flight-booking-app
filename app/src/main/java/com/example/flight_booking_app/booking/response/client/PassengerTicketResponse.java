package com.example.flight_booking_app.booking.response.client;

import java.util.List;
import java.util.UUID;

public class PassengerTicketResponse {
    private UUID passengerId;
    private String firstName;
    private String lastName;
    private String type;
    private List<TicketDetailResponse> tickets;

    // Getters
    public UUID getPassengerId() { return passengerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getType() { return type; }
    public List<TicketDetailResponse> getTickets() { return tickets; }
}