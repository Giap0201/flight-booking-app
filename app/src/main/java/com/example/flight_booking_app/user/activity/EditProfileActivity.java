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
import com.example.flight_booking_app.user.model.dto.UserResponse;
import com.example.flight_booking_app.user.model.dto.UserUpdateRequest;
import com.example.flight_booking_app.user.viewmodel.ProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail;
    private Button btnSave;

    private UserResponse currentUserInfo;
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Ánh xạ View
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSave = findViewById(R.id.btnSave);


        currentUserInfo = (UserResponse) getIntent().getSerializableExtra("EXTRA_USER_INFO");

        if (currentUserInfo != null) {
            loadDataToUI();
        } else {
            Toast.makeText(this, "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Lắng nghe kết quả cập nhật từ ViewModel
        viewModel.getUpdateStatus().observe(this, isSuccess -> {
            // Mở khóa lại nút Lưu khi có kết quả
            btnSave.setEnabled(true);
            btnSave.setText("LƯU THAY ĐỔI");

            if (isSuccess != null) {
                if (isSuccess) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Thành công thì đóng màn hình này lại, quay về ProfileFragment
                    // ProfileFragment có hàm onResume() sẽ tự động load lại tên mới!
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 5. Sự kiện bấm nút Lưu
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadDataToUI() {

        edtFullName.setText(currentUserInfo.getFullName());
        edtPhone.setText(currentUserInfo.getPhone());


        edtEmail.setText(currentUserInfo.getEmail());
    }

    private void saveProfileChanges() {
        String newName = edtFullName.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();


        if (newName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }
        if (newName.length() < 2 || newName.length() > 50) {
            edtFullName.setError("Họ tên phải từ 2 đến 50 ký tự");
            edtFullName.requestFocus();
            return;
        }

        // 2. Kiểm tra Số điện thoại (10 số, bắt đầu bằng 0)
        if (newPhone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }
        if (!newPhone.matches("^(0[0-9]{9})$")) {
            edtPhone.setError("SĐT không hợp lệ (10 số, bắt đầu bằng 0)");
            edtPhone.requestFocus();
            return;
        }



        btnSave.setEnabled(false);
        btnSave.setText("ĐANG XỬ LÝ...");

        // Đóng gói data gửi đi
        UserUpdateRequest request = new UserUpdateRequest(newName, newPhone);

        // Gọi hàm update
        viewModel.updateProfile(request);
    }
}