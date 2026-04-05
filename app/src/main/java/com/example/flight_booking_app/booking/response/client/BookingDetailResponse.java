package com.example.flight_booking_app.booking.response.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BookingDetailResponse {
    private UUID id;
    private String pnrCode;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private List<PassengerTicketResponse> passengers;

    // Getters
    public UUID getId() { return id; }
    public String getPnrCode() { return pnrCode; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public List<PassengerTicketResponse> getPassengers() { return passengers; }
}