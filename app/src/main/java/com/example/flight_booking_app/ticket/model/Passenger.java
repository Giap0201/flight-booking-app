package com.example.flight_booking_app.ticket.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Passenger {
    @SerializedName("passengerId")
    private String passengerId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("type")
    private String type;

    @SerializedName("tickets")
    private List<Ticket> tickets;

    // Getters and Setters
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    // Hàm tiện ích hỗ trợ hiển thị UI
    public String getFullName() {
        return firstName + " " + lastName;
    }
}