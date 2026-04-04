package com.example.flight_booking_app.booking.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Class này đại diện cho 1 object nằm bên trong mảng "passengers" của cục JSON gửi đi.
 * Ví dụ:
 * {
 * "firstName": "Father",
 * "lastName": "Hoang",
 * "dateOfBirth": "1988-01-01",
 * "gender": "MALE",
 * "type": "ADULT"
 * }
 */
public class PassengerRequest implements Serializable {

    // Tên của hành khách (Ví dụ: Giap)
    @SerializedName("firstName")
    private String firstName;

    // Họ của hành khách (Ví dụ: Nguyen Huu)
    @SerializedName("lastName")
    private String lastName;

    // Ngày sinh, format chuẩn thường là YYYY-MM-DD (Ví dụ: 2005-01-02)
    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    // Giới tính (Thường Backend sẽ yêu cầu gửi "MALE" hoặc "FEMALE")
    @SerializedName("gender")
    private String gender;

    // Loại hành khách (Ví dụ: "ADULT" - Người lớn, "CHILD" - Trẻ em, "INFANT" - Em bé)
    @SerializedName("type")
    private String type;

    // --- CONSTRUCTOR ---
    // Constructor rỗng bắt buộc cho Gson
    public PassengerRequest() {
    }

    // Constructor có tham số để tạo đối tượng nhanh chóng
    public PassengerRequest(String firstName, String lastName, String dateOfBirth, String gender, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.type = type;
    }

    // --- GETTER & SETTER ---

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}