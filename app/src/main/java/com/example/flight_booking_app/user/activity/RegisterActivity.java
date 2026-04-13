package com.example.flight_booking_app.user.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.user.model.dto.RegisterRequest;
import com.example.flight_booking_app.user.viewmodel.ProfileViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginNow;

    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Ánh xạ View
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);

        // Lắng nghe kết quả từ Server
        viewModel.getRegisterStatus().observe(this, isSuccess -> {
            btnRegister.setEnabled(true);
            btnRegister.setText("ĐĂNG KÝ");

            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                finish(); // Đóng màn hình đăng ký, tự động quay về màn hình Login
            } else {
                Toast.makeText(this, "Đăng ký thất bại! Email có thể đã tồn tại.", Toast.LENGTH_SHORT).show();
            }
        });

        // Bắt sự kiện Click
        btnRegister.setOnClickListener(v -> executeRegister());
        tvLoginNow.setOnClickListener(v -> finish()); // Ấn vào Đăng nhập thì đóng màn hình Đăng ký lại
    }

    private void executeRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // --- VALIDATION BẮT LỖI ---

        if (fullName.length() < 2 || fullName.length() > 50) {
            edtFullName.setError("Họ tên phải từ 2 - 50 ký tự");
            edtFullName.requestFocus(); return;
        }

        if (!phone.matches("^(0[0-9]{9})$")) {
            edtPhone.setError("SĐT không hợp lệ (10 số, bắt đầu bằng 0)");
            edtPhone.requestFocus(); return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ hoặc để trống");
            edtEmail.requestFocus();
            return;
        }

        if (password.length() < 6 || password.length() > 50) {
            edtPassword.setError("Mật khẩu phải từ 6 - 50 ký tự");
            edtPassword.requestFocus(); return;
        }

        if (!confirmPassword.equals(password)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmPassword.requestFocus(); return;
        }

        // --- GỌI API ---
        btnRegister.setEnabled(false);
        btnRegister.setText("ĐANG XỬ LÝ...");

        RegisterRequest request = new RegisterRequest(fullName, phone, email, password);
        viewModel.register(request);
    }
}