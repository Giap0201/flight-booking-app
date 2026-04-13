package com.example.flight_booking_app.user.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.authen.repository.AuthRepository;
import com.example.flight_booking_app.user.model.dto.ChangePasswordRequest;
import com.example.flight_booking_app.user.model.dto.RegisterRequest;
import com.example.flight_booking_app.user.model.dto.UserResponse;
import com.example.flight_booking_app.user.model.dto.UserUpdateRequest;
import com.example.flight_booking_app.user.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private UserRepository userRepository;

    private MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();
    private MutableLiveData<UserResponse> userInfo = new MutableLiveData<>();

    private MutableLiveData<Boolean> updateStatus = new MutableLiveData<>();

    private MutableLiveData<Boolean> changePasswordStatus = new MutableLiveData<>();

    private MutableLiveData<Boolean> registerStatus = new MutableLiveData<>();


    public MutableLiveData<Boolean> getChangePasswordStatus() {
        return changePasswordStatus;
    }


    public void changePassword(ChangePasswordRequest request) {
        userRepository.changeUserPassword(request, changePasswordStatus);
    }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userRepository = new UserRepository(application);
    }

    public MutableLiveData<Boolean> getRegisterStatus() {
        return registerStatus;
    }

    public void register(RegisterRequest request) {
        // Gọi hàm đăng ký từ UserRepository như ý của ông
        userRepository.registerUser(request, registerStatus);
    }

    // ================= CÁC HÀM CỦA USER =================

    public MutableLiveData<UserResponse> getUserInfo() {
        return userInfo;
    }

    public void loadMyInfo() {
        userRepository.fetchMyInfo(userInfo);
    }

    // 2. Thêm Getter cho updateStatus để Activity có thể Observe
    public MutableLiveData<Boolean> getUpdateStatus() {
        return updateStatus;
    }

    public void updateProfile(UserUpdateRequest request) {
        userRepository.updateMyProfile(request, updateStatus);
    }

    // ================= CÁC HÀM CỦA AUTH =================

    public MutableLiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }

    public void performLogout(String token) {
        if (token == null || token.isEmpty()) {
            logoutEvent.setValue(true);
        } else {
            authRepository.logoutUser(token, logoutEvent);
        }
    }

    public void clearSession() {
        authRepository.clearLocalSession();
    }
}