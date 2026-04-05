package com.example.flight_booking_app.user.model.dto;

import com.google.gson.annotations.SerializedName;


public class UserUpdateRequest {
    private String fullName;
    private String phone;

    public UserUpdateRequest(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    // Nhớ tạo Getter và Setter nhé
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
