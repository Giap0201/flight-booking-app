package com.example.flight_booking_app.home.model;

public class AirportTranslation {
    private String city;
    private String country;
    private String name;

    // Thêm một biến để lưu Mã sân bay (Dùng cho lúc ghép dữ liệu)
    private transient String code;

    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }

    public void setName(String name) { this.name = name; }
}
