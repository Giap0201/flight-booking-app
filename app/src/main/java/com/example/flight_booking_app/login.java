package com.example.flight_booking_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class login extends AppCompatActivity {

    // Khai báo các biến ánh xạ với giao diện
    private TextInputEditText edtEmail, edtPassword;
    private CheckBox cbRemember;
    private TextView tvForgot;
    private MaterialButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ ID từ XML sang Java
        initViews();

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
                Toast.makeText(login.this, "Chuyển sang màn Quên Mật Khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        cbRemember = findViewById(R.id.cbRemember);
        tvForgot = findViewById(R.id.tvForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    private void handleLogin() {
        // Lấy dữ liệu người dùng nhập
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validate cơ bản
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

        // TODO: Chỗ này chính là nơi ông mang đoạn code Retrofit ở bước trước vào đây
        // Gọi API lên Spring Boot truyền email & password đi
        // Ví dụ: RetrofitClient.getApiService().login(email, password).enqueue(...)

        Toast.makeText(this, "Đang đăng nhập với: " + email, Toast.LENGTH_SHORT).show();
    }
}