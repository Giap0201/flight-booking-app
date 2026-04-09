package com.example.flight_booking_app.search.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CheapestDate implements Serializable {
    @SerializedName("date")
    private String date; // Định dạng "YYYY-MM-DD" từ LocalDate của Backend

    @SerializedName("minPrice")
    private double minPrice; // Ánh xạ từ BigDecimal của Backend

    public CheapestDate(String date, double minPrice) {
        this.date = date;
        this.minPrice = minPrice;
    }

    // Getters
    public String getDate() { return date; }
    public double getMinPrice() { return minPrice; }
}