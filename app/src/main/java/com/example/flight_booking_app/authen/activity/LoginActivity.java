package com.example.flight_booking_app.authen.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flight_booking_app.MainActivity;
import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.viewmodel.LoginViewModel;
import com.example.flight_booking_app.user.activity.RegisterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // Khai báo các biến ánh xạ với giao diện
    private TextInputEditText edtEmail, edtPassword;
    private TextView tvForgot, tvSignUp;
    private MaterialButton btnSignIn;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        // 1. Khởi tạo ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 2. Lắng nghe trạng thái Loading để đổi text và khóa nút bấm
        loginViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    btnSignIn.setText("Đang xử lý...");
                    btnSignIn.setEnabled(false);
                } else {
                    btnSignIn.setText("Đăng nhập");
                    btnSignIn.setEnabled(true);
                }
            }
        });

        // 3. Lắng nghe kết quả trả về từ API (Thành công hay Thất bại)
        loginViewModel.getLoginResponseLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                loginViewModel.hideLoading();

                if ("SUCCESS".equals(result)) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // ==============================================================================
                    // ⚡ ĐÃ KHÔI PHỤC: LOGIC LAZY LOGIN (ĐIỀU HƯỚNG THÔNG MINH) ⚡
                    // ==============================================================================
                    // Lấy cờ từ Intent xem có phải bị đẩy sang từ màn hình Đặt vé không
                    boolean isFromBooking = getIntent().getBooleanExtra("IS_FROM_BOOKING", false);

                    if (isFromBooking) {
                        // NẾU ĐANG MUA VÉ DỞ DANG:
                        // Trả kết quả OK về cho bưu tá (loginLauncher) ở màn hình trước
                        setResult(RESULT_OK);

                        // Đóng màn hình Login này lại, nó sẽ tự lùi về đúng chỗ cũ
                        finish();
                    } else {
                        // NẾU ĐĂNG NHẬP BÌNH THƯỜNG TỪ ĐẦU:
                        // Phóng xe thẳng về trang Chủ (MainActivity)
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    // ==============================================================================

                } else {
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Bắt sự kiện khi click nút Sign In
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgot = findViewById(R.id.tvForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
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

        loginViewModel.performLogin(email, password);
    }
}