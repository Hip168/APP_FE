package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.databinding.ActivityRegisterBinding;
import com.example.btck.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.registerResult.observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                
                String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
                String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
                Intent data = new Intent();
                data.putExtra("email", email);
                data.putExtra("password", password);
                setResult(RESULT_OK, data);

                finish();
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading.observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!loading);
        });
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnRegister.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPwd = binding.etConfirmPassword.getText().toString().trim();

            binding.tilFullName.setError(null);
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirmPassword.setError(null);

            if (TextUtils.isEmpty(fullName)) { binding.tilFullName.setError("Vui lòng nhập họ tên"); return; }
            if (TextUtils.isEmpty(email)) { binding.tilEmail.setError("Vui lòng nhập email"); return; }
            if (email.contains(" ")) { binding.tilEmail.setError("Email không được chứa khoảng trắng"); return; }
            if (!email.contains("@")) { binding.tilEmail.setError("Email thiếu ký tự @"); return; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.setError("Email không đúng định dạng (VD: example@email.com)"); return; }
            if (password.length() < 8) { binding.tilPassword.setError("Mật khẩu tối thiểu 8 ký tự"); return; }
            if (!password.equals(confirmPwd)) { binding.tilConfirmPassword.setError("Mật khẩu không khớp"); return; }

            viewModel.register(email, password, fullName);
        });

        binding.tvLogin.setOnClickListener(v -> finish());
    }
}
