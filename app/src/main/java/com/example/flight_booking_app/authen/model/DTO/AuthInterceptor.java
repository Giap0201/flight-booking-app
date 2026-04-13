package com.example.flight_booking_app.authen.model.DTO;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthInterceptor implements Interceptor {
    private SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();

        // Sử dụng đúng hàm fetchAuthToken() bạn đã viết
        String token = sessionManager.fetchAuthToken();

        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(requestBuilder.build());

        // Bắt lỗi 401 (Token hết hạn / Không hợp lệ)
        if (response.code() == 401) {
            // Sử dụng hàm xóa token vừa thêm
            sessionManager.clearAuthToken();

            // TODO: Bắn sự kiện (BroadcastReceiver / EventBus / Intent)
            // để báo cho UI biết mà văng ra màn hình Login.
        }

        return response;
    }
}