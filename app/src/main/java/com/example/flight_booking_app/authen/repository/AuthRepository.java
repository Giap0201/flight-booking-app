package com.example.flight_booking_app.authen.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.authen.api.AuthApiService;
import com.example.flight_booking_app.authen.model.DTO.SessionManager;
import com.example.flight_booking_app.authen.model.DTO.request.ForgotPasswordRequest;
import com.example.flight_booking_app.authen.model.DTO.request.LoginRequest;
import com.example.flight_booking_app.authen.model.DTO.request.LogoutRequest;
import com.example.flight_booking_app.authen.model.DTO.response.LoginResponse;
import com.example.flight_booking_app.common.ApiResponse;
import com.example.flight_booking_app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class AuthRepository {
    private AuthApiService apiService;
    private SessionManager sessionManager;

    public AuthRepository(Application application) {
        apiService = ApiClient.getClient(application).create(AuthApiService.class);
        sessionManager = new SessionManager(application);
    }
    public void loginUser(String email, String password, MutableLiveData<String> loginResult) {

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        String token = apiResponse.getResult().getToken();
                        sessionManager.saveAuthToken(token);

                        // Bơm kết quả vào cái ống ViewModel đưa cho
                        loginResult.postValue("SUCCESS");
                    } else {
                        loginResult.postValue(apiResponse.getMessage());
                    }
                } else {
                    loginResult.postValue("Lỗi máy chủ hoặc sai cấu trúc API!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                loginResult.postValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
    public void forgotPassword(String email, MutableLiveData<String> resultLiveData) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        apiService.forgotPassword(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();

                    // Mã 1000 là thành công như API Login ông đã test
                    if (apiResponse.getCode() == 1000) {
                        resultLiveData.postValue("SUCCESS");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Gửi yêu cầu thất bại!";
                        resultLiveData.postValue(errorMsg);
                    }
                } else {
                    resultLiveData.postValue("Lỗi máy chủ hoặc sai cấu trúc API!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                resultLiveData.postValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
    // Hàm gọi API Logout
    public void logoutUser(String token, MutableLiveData<Boolean> logoutEvent) {
        LogoutRequest request = new LogoutRequest(token);

        apiService.logout(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Dù server báo lỗi hay thành công thì cũng cho phép đăng xuất local
                logoutEvent.setValue(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Lỗi mạng, vẫn cho phép đăng xuất local
                logoutEvent.setValue(true);
            }
        });
    }

    // Hàm xóa token có thể viết ở đây để gom chung logic xử lý dữ liệu
    public void clearLocalSession() {
        sessionManager.clearAuthToken();
    }
}