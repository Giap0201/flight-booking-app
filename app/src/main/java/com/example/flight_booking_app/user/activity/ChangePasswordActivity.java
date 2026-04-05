package com.example.flight_booking_app.user.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.user.model.dto.ChangePasswordRequest;
import com.example.flight_booking_app.user.viewmodel.ProfileViewModel;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;

    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Ánh xạ View
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // 3. Lắng nghe kết quả từ ViewModel
        viewModel.getChangePasswordStatus().observe(this, isSuccess -> {
            // Mở khóa lại nút bấm
            btnChangePassword.setEnabled(true);
            btnChangePassword.setText("ĐỔI MẬT KHẨU");

            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Thành công thì đóng màn hình
                } else {
                    Toast.makeText(this, "Mật khẩu cũ không đúng hoặc có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. Bắt sự kiện click
        btnChangePassword.setOnClickListener(v -> executeChangePassword());
    }

    private void executeChangePassword() {
        String oldPw = edtOldPassword.getText().toString().trim();
        String newPw = edtNewPassword.getText().toString().trim();
        String confirmPw = edtConfirmPassword.getText().toString().trim();


        if (oldPw.isEmpty()) {
            edtOldPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            edtOldPassword.requestFocus();
            return;
        }
        if (oldPw.length() < 6 || oldPw.length() > 50) {
            edtOldPassword.setError("Mật khẩu phải từ 6 đến 50 ký tự");
            edtOldPassword.requestFocus();
            return;
        }

        if (newPw.isEmpty()) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            edtNewPassword.requestFocus();
            return;
        }
        if (newPw.length() < 6 || newPw.length() > 50) {
            edtNewPassword.setError("Mật khẩu phải từ 6 đến 50 ký tự");
            edtNewPassword.requestFocus();
            return;
        }

        if (confirmPw.isEmpty()) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới");
            edtConfirmPassword.requestFocus();
            return;
        }
        if (!confirmPw.equals(newPw)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmPassword.requestFocus();
            return;
        }
        if (confirmPw.length() < 6 || confirmPw.length() > 50) {
            edtConfirmPassword.setError("Mật khẩu phải từ 6 đến 50 ký tự");
            edtConfirmPassword.requestFocus();
            return;
        }



        // Khóa nút bấm để tránh double-click
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("ĐANG XỬ LÝ...");

        ChangePasswordRequest request = new ChangePasswordRequest(oldPw, newPw, confirmPw);
        viewModel.changePassword(request);
    }
}