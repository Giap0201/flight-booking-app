package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Class này là "Trùm cuối" đại diện cho toàn bộ cục JSON gửi đi khi bấm nút Đặt Vé.
 * Bao gồm thông tin người liên hệ và các danh sách: Chuyến bay, Hành khách, Dịch vụ mua thêm.
 */
public class BookingRequest implements Serializable {

    // Tên người liên hệ (Ví dụ: Full Family Round)
    @SerializedName("contactName")
    private String contactName;

    // Email người liên hệ (Ví dụ: giapit02012005@gmail.com)
    @SerializedName("contactEmail")
    private String contactEmail;

    // Số điện thoại người liên hệ
    @SerializedName("contactPhone")
    private String contactPhone;

    // Loại tiền tệ (Thường là "VND" hoặc "USD")
    @SerializedName("currency")
    private String currency;

    // Mã giảm giá (Có thể có hoặc null nếu không nhập)
    @SerializedName("promotionCode")
    private String promotionCode;

    // Danh sách các chuyến bay khách chọn (Sử dụng class FlightRequest vừa tạo ở bước 1)
    @SerializedName("flights")
    private List<FlightRequest> flights;

    // Danh sách hành khách (Sử dụng class PassengerRequest vừa tạo ở bước 2)
    @SerializedName("passengers")
    private List<PassengerRequest> passengers;

    // Danh sách dịch vụ mua thêm (Sử dụng class AncillaryRequest vừa tạo ở bước 3)
    @SerializedName("bookingAncillaries")
    private List<AncillaryRequest> bookingAncillaries;

    // --- CONSTRUCTOR ---
    public BookingRequest() {
    }

    public BookingRequest(String contactName, String contactEmail, String contactPhone, String currency, String promotionCode, List<FlightRequest> flights, List<PassengerRequest> passengers, List<AncillaryRequest> bookingAncillaries) {
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.currency = currency;
        this.promotionCode = promotionCode;
        this.flights = flights;
        this.passengers = passengers;
        this.bookingAncillaries = bookingAncillaries;
    }

    // --- GETTER & SETTER ---

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public List<FlightRequest> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightRequest> flights) {
        this.flights = flights;
    }

    public List<PassengerRequest> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerRequest> passengers) {
        this.passengers = passengers;
    }

    public List<AncillaryRequest> getBookingAncillaries() {
        return bookingAncillaries;
    }

    public void setBookingAncillaries(List<AncillaryRequest> bookingAncillaries) {
        this.bookingAncillaries = bookingAncillaries;
    }
}