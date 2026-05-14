package com.example.btck.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.databinding.ActivityForgotPasswordBinding;
import com.example.btck.viewmodel.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel.passwordRecoveryResult.observe(this, result -> {
            if (result != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
                Toast.makeText(this, "Email khôi phục đã được gửi!", Toast.LENGTH_LONG).show();
                binding.layoutSuccess.setVisibility(View.VISIBLE);
                binding.btnSend.setVisibility(View.GONE);
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError("Vui lòng nhập email");
                return;
            }
            binding.tilEmail.setError(null);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSend.setEnabled(false);
            viewModel.recoverPassword(email);
        });
    }
}
