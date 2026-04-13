package com.example.flight_booking_app.common;

import com.google.gson.annotations.SerializedName;

// Chữ <T> ở đây gọi là Generic.
// T nghĩa là Type (Kiểu dữ liệu). Nhờ có <T>, class này có thể bọc bất kỳ dữ liệu gì ở phần "result"
// Ví dụ: ApiResponse<FlightDetail>, ApiResponse<List<Flight>>...
public class ApiResponse<T> {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private T result; // Dữ liệu thực sự trả về (có thể là 1 Object, hoặc 1 List)

    // Constructor rỗng bắt buộc cho thư viện Gson
    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    // --- GETTER & SETTER ---

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}