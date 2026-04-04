package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Class này đại diện cho 1 object nằm bên trong mảng "flights" của cục JSON gửi đi.
 * Ví dụ:
 * {
 * "flightId": "3faf40b5-d5c7-45e1-90d4-d1a035f16f53",
 * "flightClassId": "0077fef7-9357-47ea-872b-aba3ad2adde6"
 * }
 */
public class FlightRequest implements Serializable {

    // ID của chuyến bay (Lấy từ màn hình FlightDetail trước đó truyền sang)
    @SerializedName("flightId")
    private String flightId;

    // ID của hạng vé (Ví dụ ID của vé ECONOMY hoặc BUSINESS mà người dùng vừa bấm "Select")
    @SerializedName("flightClassId")
    private String flightClassId;

    // --- CONSTRUCTOR ---
    // Constructor rỗng (Bắt buộc phải có để thư viện Gson dùng khi parse dữ liệu)
    public FlightRequest() {
    }

    // Constructor có tham số để lát nữa ở màn hình Java mình khởi tạo cho lẹ
    public FlightRequest(String flightId, String flightClassId) {
        this.flightId = flightId;
        this.flightClassId = flightClassId;
    }

    // --- GETTER & SETTER ---
    // Dùng để lấy hoặc sửa dữ liệu sau khi khởi tạo object
    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightClassId() {
        return flightClassId;
    }

    public void setFlightClassId(String flightClassId) {
        this.flightClassId = flightClassId;
    }
}