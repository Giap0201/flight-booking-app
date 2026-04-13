package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Class này đại diện cho 1 object nằm bên trong mảng "bookingAncillaries" của cục JSON gửi đi.
 * Dùng để lưu thông tin khách hàng mua thêm Hành lý hoặc Đồ ăn.
 * Ví dụ:
 * {
 * "catalogId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
 * "passengerIndex": 0,
 * "segmentNo": 0
 * }
 */
public class AncillaryRequest implements Serializable {

    // ID của dịch vụ (Ví dụ: ID của gói Hành lý 20kg hoặc ID của suất Cơm bò lúc lắc)
    @SerializedName("catalogId")
    private String catalogId;

    // Chỉ số của hành khách mua dịch vụ này (Đếm từ 0).
    // Ví dụ: Mảng passengers gửi đi có 3 người (Bố, Mẹ, Con).
    // Nếu Bố (người đầu tiên) mua hành lý -> passengerIndex = 0.
    // Nếu Mẹ (người thứ hai) mua hành lý -> passengerIndex = 1.
    @SerializedName("passengerIndex")
    private int passengerIndex;

    // Số thứ tự của chặng bay (Đếm từ 0).
    // Thường chuyến bay thẳng (1 chiều) thì chặng bay luôn là 0.
    // Nếu bay nối chuyến (transit) hoặc khứ hồi thì mới có segmentNo = 1, 2...
    @SerializedName("segmentNo")
    private int segmentNo;

    // --- CONSTRUCTOR ---
    // Constructor rỗng bắt buộc cho Gson
    public AncillaryRequest() {
    }

    public AncillaryRequest(String catalogId, int passengerIndex, int segmentNo) {
        this.catalogId = catalogId;
        this.passengerIndex = passengerIndex;
        this.segmentNo = segmentNo;
    }

    // --- GETTER & SETTER ---

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public int getPassengerIndex() {
        return passengerIndex;
    }

    public void setPassengerIndex(int passengerIndex) {
        this.passengerIndex = passengerIndex;
    }

    public int getSegmentNo() {
        return segmentNo;
    }

    public void setSegmentNo(int segmentNo) {
        this.segmentNo = segmentNo;
    }
}