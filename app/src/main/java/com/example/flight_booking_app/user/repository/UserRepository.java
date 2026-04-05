package com.example.flight_booking_app.user.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;
import com.example.flight_booking_app.user.api.UserApiService;
import com.example.flight_booking_app.user.model.dto.ChangePasswordRequest;
import com.example.flight_booking_app.user.model.dto.UserResponse;
import com.example.flight_booking_app.user.model.dto.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private UserApiService apiService;

    public UserRepository(Application application) {
        apiService = ApiClient.getClient(application).create(UserApiService.class);
    }
    public void fetchMyInfo(MutableLiveData<UserResponse> userInfoLiveData) {
        apiService.getMyInfo().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    userInfoLiveData.setValue(response.body().getResult());
                } else {
                    userInfoLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                userInfoLiveData.setValue(null);
            }
        });
    }
    public void updateMyProfile(UserUpdateRequest request, MutableLiveData<Boolean> updateStatus) {
        apiService.updateProfile(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    updateStatus.setValue(true);
                } else {
                    updateStatus.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                updateStatus.setValue(false);
            }
        });
    }
    public void changeUserPassword(ChangePasswordRequest request, MutableLiveData<Boolean> changePwStatus) {
        apiService.changePassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                    changePwStatus.setValue(true);
                } else {
                    changePwStatus.setValue(false);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                changePwStatus.setValue(false);
            }
        });
    }
    }

