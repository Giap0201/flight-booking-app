package com.example.flight_booking_app.authen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // ==============================================================================
    // 1. KHAI BÁO BIẾN
    // ==============================================================================
    private TextInputEditText edtEmail, edtPassword;
    private CheckBox cbRemember;
    private TextView tvForgot;
    private MaterialButton btnSignIn;
    private LoginViewModel loginViewModel;

    // ==============================================================================
    // 2. VÒNG ĐỜI ONCREATE
    // ==============================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ giao diện
        initViews();

        // Khởi tạo ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Lắng nghe các thay đổi từ ViewModel
        observeViewModel();

        // Xử lý các sự kiện Click
        setupClickListeners();
    }

    // ==============================================================================
    // 3. CÁC HÀM HỖ TRỢ (ĐƯỢC GỌI TỪ ONCREATE)
    // ==============================================================================

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        cbRemember = findViewById(R.id.cbRemember);
        tvForgot = findViewById(R.id.tvForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    private void setupClickListeners() {
        // Nút Đăng nhập
        btnSignIn.setOnClickListener(v -> handleLogin());

        // Nút Quên mật khẩu
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập Email!");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập Mật khẩu!");
            edtPassword.requestFocus();
            return;
        }

        // Gọi API qua ViewModel
        loginViewModel.performLogin(email, password);
    }

    private void observeViewModel() {
        // 1. Quản lý trạng thái Loading (Nút bấm)
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnSignIn.setText("Đang xử lý...");
                btnSignIn.setEnabled(false);
            } else {
                btnSignIn.setText("Sign In");
                btnSignIn.setEnabled(true);
            }
        });

        // 2. Lắng nghe kết quả Đăng nhập
        loginViewModel.getLoginResponseLiveData().observe(this, result -> {
            loginViewModel.hideLoading();

            if ("SUCCESS".equals(result)) {
                // LƯU Ý: Nếu LoginViewModel chưa lưu Token, bạn cần gọi SessionManager để lưu token ở đây nhé!
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // ⚡ XỬ LÝ CHUYỂN TRANG THÔNG MINH ⚡
                kiemTraVaChuyenTrang();
            } else {
                Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==============================================================================
    // 4. LOGIC ĐIỀU HƯỚNG ("TRÁI TIM" CỦA LAZY LOGIN)
    // ==============================================================================
    private void kiemTraVaChuyenTrang() {
        // Lấy cờ từ Intent gửi sang. (Có thể dùng chung cờ IS_FROM_BOOKING này cho TẤT CẢ các trang khác)
        // Nếu muốn chuẩn nghĩa hơn, bạn có thể đổi tên thành "REQUIRE_LOGIN_RESULT" ở tất cả các nơi.
        boolean requireResult = getIntent().getBooleanExtra("IS_FROM_BOOKING", false);

        if (requireResult) {
            // NẾU BỊ ĐẨY SANG TỪ MỘT TRANG KHÁC (VD: Hành lý, Hồ sơ...)
            // Trả kết quả "Hoàn thành nhiệm vụ" cho Bưu tá
            setResult(RESULT_OK);

            // Tự đóng cửa màn hình Login. OS sẽ tự ném về trang vừa gọi nó.
            finish();
        } else {
            // NẾU MỞ APP LÊN TỰ ĐĂNG NHẬP
            // Chạy bình thường vào màn hình Chính
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}