package com.example.flight_booking_app.authen.model.DTO;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "FlightBookingPrefs";
    private static final String KEY_USER_TOKEN = "jwt_token";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();
    }

    public String fetchAuthToken() {
        return prefs.getString(KEY_USER_TOKEN, null);
    }
    // Thêm hàm này vào dưới hàm fetchAuthToken()
    public void clearAuthToken() {
        editor.remove(KEY_USER_TOKEN);
        // Hoặc dùng editor.clear() nếu bạn muốn xóa TẤT CẢ dữ liệu trong file SharedPreferences này
        editor.apply();
    }
}
