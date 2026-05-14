package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.ActivityJoinEventBinding;
import com.example.btck.models.EventMemberPublic;
import com.example.btck.managers.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinEventActivity extends AppCompatActivity {

    private ActivityJoinEventBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tham gia nhóm");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Xử lý deep link: btck://join?code=XXXX
        handleDeepLink();

        binding.btnJoin.setOnClickListener(v -> {
            String code = binding.etCode.getText() != null
                    ? binding.etCode.getText().toString().trim() : "";
            if (code.isEmpty()) {
                binding.tilCode.setError("Vui lòng nhập mã mời");
                return;
            }
            joinByCode(code);
        });
    }

    private void handleDeepLink() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            String code = intent.getData().getQueryParameter("code");
            if (code != null && !code.isEmpty()) {
                binding.etCode.setText(code);
                joinByCode(code);
            }
        }
    }

    private void joinByCode(String code) {
        TokenManager tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnJoin.setEnabled(false);
        binding.tvStatus.setVisibility(View.GONE);

        RetrofitClient.getApiService()
                .joinEventByCode(code)
                .enqueue(new Callback<EventMemberPublic>() {
                    @Override
                    public void onResponse(Call<EventMemberPublic> call,
                                           Response<EventMemberPublic> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnJoin.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            binding.tvStatus.setVisibility(View.VISIBLE);
                            binding.tvStatus.setText("✅ Tham gia nhóm thành công!");
                            binding.tvStatus.setTextColor(getColor(com.example.btck.R.color.color_owed));

                            // Quay về MainActivity sau 1.5 giây
                            binding.getRoot().postDelayed(() -> {
                                Intent mainIntent = new Intent(JoinEventActivity.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainIntent);
                                finish();
                            }, 1500);
                        } else {
                            binding.tvStatus.setVisibility(View.VISIBLE);
                            binding.tvStatus.setText("❌ Mã mời không hợp lệ hoặc đã hết hạn");
                            binding.tvStatus.setTextColor(getColor(com.example.btck.R.color.color_owe));
                        }
                    }

                    @Override
                    public void onFailure(Call<EventMemberPublic> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnJoin.setEnabled(true);
                        Toast.makeText(JoinEventActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
