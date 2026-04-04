package com.example.flight_booking_app.network;
// Package dùng chung cho toàn bộ phần gọi API (network layer)

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient:
 * Class này chịu trách nhiệm tạo ra 1 instance Retrofit duy nhất (Singleton)
 * để gọi API tới backend.
 */
public class ApiClient {

    // URL gốc của backend API
    // 10.0.2.2 = localhost của máy tính khi chạy trên Android Emulator
    // (Emulator không hiểu localhost, nên phải dùng IP đặc biệt này)
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // Biến static để giữ instance Retrofit duy nhất (Singleton pattern)
    private static Retrofit retrofit = null;

    /**
     * Hàm này trả về đối tượng Retrofit để gọi API
     * Nếu chưa có thì tạo mới, nếu rồi thì dùng lại
     */
    public static Retrofit getClient() {

        // Kiểm tra nếu Retrofit chưa được khởi tạo
        if (retrofit == null) {

            // 1. Tạo interceptor để log request/response (debug)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            // Level.BODY = log tất cả:
            // - URL
            // - Header
            // - Body request
            // - Body response
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Tạo OkHttpClient (thư viện HTTP thực sự gửi request)
            OkHttpClient client = new OkHttpClient.Builder()

                    // Gắn interceptor vào để log toàn bộ API call
                    .addInterceptor(logging)

                    // Có thể thêm:
                    // .connectTimeout(...)
                    // .readTimeout(...)
                    // .addInterceptor(authInterceptor) // token

                    .build();

            // 3. Tạo Retrofit instance
            retrofit = new Retrofit.Builder()

                    // Base URL của API (bắt buộc phải có dấu / cuối)
                    .baseUrl(BASE_URL)

                    // Gắn OkHttpClient vào Retrofit
                    .client(client)

                    // Converter: chuyển JSON ↔ Java object (dùng Gson)
                    .addConverterFactory(GsonConverterFactory.create())

                    // Build object Retrofit
                    .build();
        }

        // Trả về instance Retrofit (đã có hoặc vừa tạo)
        return retrofit;
    }
}