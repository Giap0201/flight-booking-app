package com.example.flight_booking_app.authen.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flight_booking_app.authen.repository.AuthRepository;

public class ForgotPasswordViewModel extends AndroidViewModel {

    private AuthRepository repository;
    private MutableLiveData<String> forgotPassResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<String> getForgotPassResult() { return forgotPassResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void hideLoading() { isLoading.postValue(false); }

    public void performForgotPassword(String email) {
        isLoading.setValue(true);
        repository.forgotPassword(email, forgotPassResult);
    }
}