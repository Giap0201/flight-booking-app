package com.example.flight_booking_app.authen.activity;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flight_booking_app.R;
import com.example.flight_booking_app.authen.viewmodel.ForgotPasswordViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextInputEditText edtEmail;
    private MaterialButton btnSubmit;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnBack = findViewById(R.id.btnBack);
        edtEmail = findViewById(R.id.edtEmail);
        btnSubmit = findViewById(R.id.btnSubmit);

        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        // Nút Back mũi tên
        btnBack.setOnClickListener(v -> finish()); // Đóng màn hình này, tự động quay về Login

        // Bắt trạng thái Loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnSubmit.setText("Đang gửi...");
                btnSubmit.setEnabled(false);
            } else {
                btnSubmit.setText("Gửi yêu cầu");
                btnSubmit.setEnabled(true);
            }
        });

        // Bắt kết quả API
        viewModel.getForgotPassResult().observe(this, result -> {
            viewModel.hideLoading();

            if ("SUCCESS".equals(result)) {
                Toast.makeText(this, "Kiểm tra email của bạn để đổi mật khẩu nhé!", Toast.LENGTH_LONG).show();
                finish(); // Thành công thì tự đẩy về màn Login
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            }
        });

        // Bấm nút Gửi
        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Email không được để trống!");
                edtEmail.requestFocus();
                return;
            }

            // Check định dạng email cho xịn
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ!");
                edtEmail.requestFocus();
                return;
            }

            viewModel.performForgotPassword(email);
        });
    }
}