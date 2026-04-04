package com.example.flight_booking_app.home.model;

public class AirportTranslation {
    private String city;
    private String country;
    private String name;

    // Thêm một biến để lưu Mã sân bay (Dùng cho lúc ghép dữ liệu)
    private transient String code;

    // Constructor, Getters và Setters
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
