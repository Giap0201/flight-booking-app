package com.example.flight_booking_app.home.model;

import com.google.gson.annotations.SerializedName;

public class SearchRequest {

    @SerializedName("origin")
    private String origin;

    @SerializedName("destination")
    private String destination;

    @SerializedName("date")
    private String date; // Ngày đi "YYYY-MM-DD"

    @SerializedName("passengers")
    private int passengers;

    // --- BỔ SUNG CHO TÍNH NĂNG KHỨ HỒI ---
    @SerializedName("isRoundTrip")
    private boolean isRoundTrip;

    @SerializedName("returnDate")
    private String returnDate; // Ngày về "YYYY-MM-DD" (Nếu có)

    // Cập nhật lại Constructor để nhận thêm 2 biến mới
    public SearchRequest(String origin, String destination, String date, int passengers, boolean isRoundTrip, String returnDate) {
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.passengers = passengers;
        this.isRoundTrip = isRoundTrip;
        this.returnDate = returnDate;
    }

    // Bạn có thể tự Generate thêm các hàm Getter/Setter (Alt + Insert) ở dưới này nếu cần thiết nhé

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public void setRoundTrip(boolean roundTrip) {
        isRoundTrip = roundTrip;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public int getPassengers() { return passengers; }
    public boolean isRoundTrip() { return isRoundTrip; }
    public String getReturnDate() { return returnDate; }
}