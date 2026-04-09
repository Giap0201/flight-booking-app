package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Class này dùng để hứng cục "result" từ Server trả về sau khi POST Đặt vé thành công.
 * Ví dụ Server trả về:
 * {
 * "id": "f8648cdc-4d5b-4191-b2f2-c50748ebe85d",
 * "pnrCode": "4ZD5P6",
 * "totalAmount": 12629375.00000,
 * "currency": "VND",
 * "status": "PENDING",
 * "expireAt": "2026-04-02T02:10:38.665048"
 * }
 */
public class BookingResult implements Serializable {

    // ID của toàn bộ đơn đặt chỗ này (Dùng để gọi API GET /bookings/{id} ở màn hình sau)
    @SerializedName("id")
    private String id;

    // Mã đặt chỗ (Mã PNR - Khách sẽ đọc mã này cho nhân viên sân bay để lấy vé)
    @SerializedName("pnrCode")
    private String pnrCode;

    // Tổng tiền của đơn hàng
    @SerializedName("totalAmount")
    private double totalAmount;

    // Đơn vị tiền tệ (VD: VND)
    @SerializedName("currency")
    private String currency;

    // Trạng thái đơn hàng (VD: PENDING - đang chờ thanh toán, CONFIRMED - đã thanh toán)
    @SerializedName("status")
    private String status;

    // Thời hạn cuối cùng để thanh toán trước khi vé bị hủy (VD: "2026-04-02T02:10:38")
    @SerializedName("expireAt")
    private String expireAt;

    // --- CONSTRUCTOR ---
    public BookingResult() {
    }

    public BookingResult(String id, String pnrCode, double totalAmount, String currency, String status, String expireAt) {
        this.id = id;
        this.pnrCode = pnrCode;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.expireAt = expireAt;
    }

    // --- GETTER & SETTER ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPnrCode() {
        return pnrCode;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }
}