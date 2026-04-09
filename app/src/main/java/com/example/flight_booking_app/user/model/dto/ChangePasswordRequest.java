package com.example.flight_booking_app.user.model.dto;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("oldPassword")
    private String oldPassword;

    @SerializedName("newPassword")
    private String newPassword;

    @SerializedName("confirmPassword")
    private String confirmPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Các hàm Getter và Setter (ông có thể tự generate ra nhé)
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
