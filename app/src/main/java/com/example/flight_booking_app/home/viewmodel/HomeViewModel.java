package com.example.flight_booking_app.home.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.flight_booking_app.home.model.AirportTranslation;
import com.example.flight_booking_app.home.repository.HomeRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;
    private LiveData<List<AirportTranslation>> airportsLiveData;

    // Dùng AndroidViewModel thay vì ViewModel để có thể truyền 'application' vào Repository
    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository(application);
    }

    // Giao diện (Activity) sẽ gọi hàm này để lấy dữ liệu
    public LiveData<List<AirportTranslation>> getAirports() {
        // Chỉ gọi API 1 lần duy nhất lúc vừa mở lên.
        // Nếu data đã có sẵn trong RAM (ví dụ lúc xoay màn hình) thì không gọi lại mạng nữa.
        if (airportsLiveData == null) {
            airportsLiveData = repository.fetchAndMergeAirports();
        }
        return airportsLiveData;
    }
}