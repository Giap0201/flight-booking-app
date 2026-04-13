package com.example.flight_booking_app.ticket.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Class này dùng chung để hứng dữ liệu của cả cục "origin" và "destination" trong JSON
public class Location implements Serializable {

    @SerializedName("code")
    private String code; // Mã sân bay (Ví dụ: HAN, SGN, CGK...)

    @SerializedName("name")
    private String name; // Tên sân bay (Ví dụ: Noi Bai International Airport)

    @SerializedName("cityCode")
    private String cityCode; // Mã thành phố

    @SerializedName("countryCode")
    private String countryCode; // Mã quốc gia (Ví dụ: VN, ID)

    // Constructor rỗng bắt buộc cho Gson
    public Location() {
    }

    public Location(String code, String name, String cityCode, String countryCode) {
        this.code = code;
        this.name = name;
        this.cityCode = cityCode;
        this.countryCode = countryCode;
    }

    // --- GETTER & SETTER ---

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}