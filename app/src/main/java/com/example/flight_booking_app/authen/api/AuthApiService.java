package com.example.flight_booking_app.authen.api;


import com.example.flight_booking_app.authen.model.DTO.request.ForgotPasswordRequest;
import com.example.flight_booking_app.authen.model.DTO.request.LoginRequest;
import com.example.flight_booking_app.authen.model.DTO.request.LogoutRequest;
import com.example.flight_booking_app.authen.model.DTO.response.LoginResponse;
import com.example.flight_booking_app.common.ApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);
    @POST("users/forgot-password")
    Call<ApiResponse<Object>> forgotPassword(@Body ForgotPasswordRequest request);
    @POST("auth/logout")
    Call<Void> logout(@Body LogoutRequest request);
}