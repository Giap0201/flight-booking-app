package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Class này dùng để hứng dữ liệu danh sách Dịch vụ (Hành lý, Suất ăn...) từ API GET.
 */
public class AncillaryItem implements Serializable {

    // ID của dịch vụ (Sẽ dùng cái này làm catalogId để gửi đi lúc đặt vé)
    @SerializedName("id")
    private String id;

    // Mã dịch vụ (Ví dụ: BG_20KG, MEAL_BEEF)
    @SerializedName("code")
    private String code;

    // Loại dịch vụ (SEAT, MEAL, BAGGAGE)
    @SerializedName("type")
    private String type;

    // Tên dịch vụ hiển thị lên UI (VD: Hành lý ký gửi 20kg)
    @SerializedName("name")
    private String name;

    // Giá tiền dịch vụ
    @SerializedName("price")
    private double price;

    public AncillaryItem() {
    }

    public AncillaryItem(String id, String code, String type, String name, double price) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}