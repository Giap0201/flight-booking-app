package com.example.flight_booking_app.user.api;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.user.model.dto.ChangePasswordRequest;
import com.example.flight_booking_app.user.model.dto.RegisterRequest;
import com.example.flight_booking_app.user.model.dto.UserResponse;
import com.example.flight_booking_app.user.model.dto.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UserApiService {
    @GET("users/my-infor")
    Call<ApiResponse<UserResponse>> getMyInfo();
    @PUT("users/my-infor")
    Call<ApiResponse<UserResponse>> updateProfile(@Body UserUpdateRequest request);
    @PATCH("users/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);
    @POST("users")
    Call<ApiResponse<UserResponse>> registerUser(@Body RegisterRequest request);

}
