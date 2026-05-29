package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.R;
import com.example.btck.databinding.ActivityLoginBinding;
import com.example.btck.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String email = result.getData().getStringExtra("email");
                    String password = result.getData().getStringExtra("password");
                    if (email != null) binding.etEmail.setText(email);
                    if (password != null) binding.etPassword.setText(password);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.loginResult.observe(this, token -> {
            if (token != null) {
                viewModel.saveTokens(token.accessToken, token.refreshToken);
                viewModel.fetchCurrentUser();
            }
        });

        viewModel.currentUser.observe(this, user -> {
            if (user != null) {
                viewModel.saveUserInfo(user.id, user.email, user.fullName);
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading.observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);

            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError("Vui lòng nhập email");
                return;
            }
            if (email.contains(" ")) {
                binding.tilEmail.setError("Email không được chứa khoảng trắng");
                return;
            }
            if (!email.contains("@")) {
                binding.tilEmail.setError("Email thiếu ký tự @");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Email không đúng định dạng (VD: example@email.com)");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                binding.tilPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }
            viewModel.login(email, password);
        });


        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            registerLauncher.launch(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
