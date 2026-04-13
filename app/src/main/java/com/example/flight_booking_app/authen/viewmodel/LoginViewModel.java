package com.example.flight_booking_app.authen.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.authen.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private AuthRepository repository;

    private MutableLiveData<String> loginResponseLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<String> getLoginResponseLiveData() {
        return loginResponseLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }


    public void hideLoading() {
        isLoading.postValue(false);
    }

    public void performLogin(String email, String password) {
        isLoading.setValue(true);

        repository.loginUser(email, password, loginResponseLiveData);
    }
}