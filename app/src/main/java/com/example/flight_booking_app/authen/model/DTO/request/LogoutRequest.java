package com.example.flight_booking_app.authen.model.DTO.request;

public class LogoutRequest {
    private String token;

    public LogoutRequest(String token) {
        this.token = token;
    }
}
