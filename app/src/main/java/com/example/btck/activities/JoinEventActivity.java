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
import com.example.btck.utils.InviteCodeUtils;
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
                    ? InviteCodeUtils.normalize(binding.etCode.getText().toString()) : "";
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
                String normalizedCode = InviteCodeUtils.normalize(code);
                binding.etCode.setText(normalizedCode);
                joinByCode(normalizedCode);
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
                            // Parse lỗi thực từ backend thay vì hardcode message
                            String errorMsg = parseErrorDetail(response);
                            binding.tvStatus.setVisibility(View.VISIBLE);
                            binding.tvStatus.setText("❌ " + errorMsg);
                            binding.tvStatus.setTextColor(getColor(com.example.btck.R.color.color_owe));
                        }
                    }

                    @Override
                    public void onFailure(Call<EventMemberPublic> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnJoin.setEnabled(true);
                        Toast.makeText(JoinEventActivity.this,
                                "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Đọc field "detail" từ error body của backend */
    private String parseErrorDetail(retrofit2.Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                // Parse thủ công để tránh phụ thuộc thêm thư viện
                org.json.JSONObject json = new org.json.JSONObject(raw);
                if (json.has("detail")) {
                    String detail = json.getString("detail");
                    // Dịch các message phổ biến sang tiếng Việt
                    switch (detail) {
                        case "Invalid or expired invite code":
                            return "Mã mời không hợp lệ hoặc đã hết hạn";
                        case "Already a member of this event":
                            return "Bạn đã là thành viên của nhóm này rồi";
                        case "Event not found":
                            return "Không tìm thấy nhóm";
                        default:
                            return detail; // Hiển thị nguyên văn nếu chưa có bản dịch
                    }
                }
            }
        } catch (Exception ignored) {}
        return "Tham gia thất bại (lỗi " + response.code() + ")";
    }
}
