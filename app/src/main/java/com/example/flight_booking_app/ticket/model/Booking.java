package com.example.flight_booking_app.ticket.model;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Booking {
    @SerializedName("orderId")
    private String orderId; // Mã vé (VD: TBW7FWZ)

    @SerializedName("status")
    private String status; // Trạng thái: PENDING, UPCOMING, COMPLETED, CANCELED

    @SerializedName("totalPrice")
    private double totalPrice; // Tổng tiền (VD: 800)

    @SerializedName("remainingPaymentTime")
    private long remainingPaymentTime; // Thời gian đếm ngược tính bằng milliseconds (cho vé PENDING)

    @SerializedName("flight")
    private Flight flight; // Object chuyến bay

    @SerializedName("passengers")
    private List<Passenger> passengers; // Danh sách người bay

    // TODO: Bạn hãy Generate Getters và Setters
}