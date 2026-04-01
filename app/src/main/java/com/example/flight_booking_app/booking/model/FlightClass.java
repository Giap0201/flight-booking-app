package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Class này đại diện cho 1 cục vé (ví dụ: FIRST_CLASS giá 100$) trong danh sách
// Implements Serializable để sau này mình có thể truyền cả cục vé này qua Intent sang màn hình Nhập thông tin khách
public class FlightClass implements Serializable {

    @SerializedName("id")
    private String id; // ID của hạng vé này (vd: 3fa85f64...)

    @SerializedName("classType")
    private String classType; // Tên hạng vé: FIRST_CLASS, ECONOMY, BUSINESS...

    @SerializedName("basePrice")
    private double basePrice; // Giá vé cơ bản

    @SerializedName("taxPercentage")
    private double taxPercentage; // Phần trăm thuế

    @SerializedName("availableSeats")
    private int availableSeats; // Số ghế còn trống

    // Constructor rỗng bắt buộc cho Gson
    public FlightClass() {
    }

    public FlightClass(String id, String classType, double basePrice, double taxPercentage, int availableSeats) {
        this.id = id;
        this.classType = classType;
        this.basePrice = basePrice;
        this.taxPercentage = taxPercentage;
        this.availableSeats = availableSeats;
    }

    // --- GETTER & SETTER ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(double taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}