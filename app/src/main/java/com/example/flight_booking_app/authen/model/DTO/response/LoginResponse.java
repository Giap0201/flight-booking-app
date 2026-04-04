package com.example.flight_booking_app.authen.model.DTO.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;
    public String getToken() { return token; }
}
