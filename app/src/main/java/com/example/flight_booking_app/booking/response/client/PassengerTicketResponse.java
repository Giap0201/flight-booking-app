package com.example.flight_booking_app.booking.response.client;

import java.util.List;

public class PassengerTicketResponse {
    private String passengerId;
    private String firstName;
    private String lastName;
    private String type;
    private List<TicketDetailResponse> tickets;

    // Getters
    public String getPassengerId() { return passengerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getType() { return type; }
    public List<TicketDetailResponse> getTickets() { return tickets; }

    // Hàm gộp tên cho tiện
    public String getFullName() {
        return (lastName != null ? lastName + " " : "") + (firstName != null ? firstName : "");
    }
}