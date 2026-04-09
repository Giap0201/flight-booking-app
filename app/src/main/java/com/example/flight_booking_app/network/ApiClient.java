package com.example.flight_booking_app.network; // Nằm ở thư mục dùng chung ngoài cùng

import android.content.Context;

import com.example.flight_booking_app.authen.model.DTO.AuthInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    // Dùng getApplicationContext() để tránh Memory Leak
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                    // Mở comment dòng dưới ra nếu bạn viết thêm TokenAuthenticator như bài trước
                    // .authenticator(new TokenAuthenticator(context.getApplicationContext()))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}