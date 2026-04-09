package com.example.flight_booking_app.search.model;

public class Airline {
    private String code;    // Mã IATA (VD: VN)
    private String name;    // Tên hãng (VD: Vietnam Airlines)
    private String logoUrl; // Link ảnh logo [cite: 72, 298]

    // Constructor, Getters và Setters
    public Airline() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}