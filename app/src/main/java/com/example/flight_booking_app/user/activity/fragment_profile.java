package com.example.flight_booking_app.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;



import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.activity.LoginActivity;

import com.example.flight_booking_app.authen.model.DTO.SessionManager;

import com.example.flight_booking_app.user.model.dto.UserResponse;
import com.example.flight_booking_app.user.viewmodel.ProfileViewModel;


public class fragment_profile extends Fragment {

    private TextView menuLogout;
    private TextView tvName;
    private TextView tvEditSubtitle;
    private View cardMenu1;
    private TextView menuChangeProfileInfo;

    private ProfileViewModel viewModel;
    private SessionManager sessionManager;
    private UserResponse currentUser;
    private TextView menuChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel và SessionManager
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sessionManager = new SessionManager(requireContext());

        // 2. Ánh xạ View
        menuLogout = view.findViewById(R.id.menuLogout);
        tvName = view.findViewById(R.id.tvName);
        tvEditSubtitle = view.findViewById(R.id.tvEditSubtitle);
        cardMenu1 = view.findViewById(R.id.cardMenu1);
        menuChangeProfileInfo = view.findViewById(R.id.menuChangeProfileInfo);
        menuChangePassword = view.findViewById(R.id.menuChangePassword);

        // 3. Lắng nghe dữ liệu User đổ về từ API
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null) {
                this.currentUser = userResponse;
                tvName.setText(userResponse.getFullName());
            } else {
                tvName.setText("Lỗi tải thông tin");
            }
        });

        // 4. Lắng nghe sự kiện đăng xuất từ ViewModel
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), isLogoutReady -> {
            if (isLogoutReady != null && isLogoutReady) {
                executeLocalLogout();
            }
        });


        View.OnClickListener goToEditProfileListener = v -> {
            if (currentUser != null) {
                Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                intent.putExtra("EXTRA_USER_INFO", currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Đang tải thông tin, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            }
        };
        tvEditSubtitle.setOnClickListener(goToEditProfileListener);
        menuChangeProfileInfo.setOnClickListener(goToEditProfileListener);
        menuChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        updateUIBasedOnAuthStatus();
    }

    // Hàm cập nhật giao diện (Khách / Đã đăng nhập)
    private void updateUIBasedOnAuthStatus() {
        String token = sessionManager.fetchAuthToken();

        if (token != null && !token.isEmpty()) {
            tvName.setText("Đang tải...");
            tvEditSubtitle.setVisibility(View.VISIBLE);
            cardMenu1.setVisibility(View.VISIBLE);

            // Nút thành ĐĂNG XUẤT (Màu đỏ)
            menuLogout.setText("Đăng xuất");
            menuLogout.setTextColor(Color.parseColor("#E53935"));

            // Gọi API lấy thông tin ngay lập tức (ViewModel sẽ đẩy data lên tvName qua observe)
            viewModel.loadMyInfo();

            // Sự kiện Đăng xuất
            menuLogout.setOnClickListener(v -> {
                viewModel.performLogout(token);
            });

        } else {
            // ================= TRẠNG THÁI: KHÁCH =================
            this.currentUser = null; // Xóa data user cũ
            tvName.setText("Khách");
            tvEditSubtitle.setVisibility(View.GONE);
            cardMenu1.setVisibility(View.GONE);


            menuLogout.setText("Đăng nhập");
            menuLogout.setTextColor(Color.parseColor("#7F00FF"));


            menuLogout.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
            });
        }
    }


    private void executeLocalLogout() {
        if (!isAdded()) return;

        // Xóa token lưu trong máy
        viewModel.clearSession();

        updateUIBasedOnAuthStatus();
    }
}