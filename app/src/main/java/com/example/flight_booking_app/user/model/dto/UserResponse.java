package com.example.flight_booking_app.user.model.dto;

import java.io.Serializable;
import java.util.Set;

public class UserResponse implements Serializable {
    private String id; // UUID bên Java backend thường trả về String trên JSON
    private String email;
    private String fullName;
    private String phone;


    // --- Tạo Getter và Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
