package com.example.btck.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.ActivityPaymentQrBinding;
import com.example.btck.models.PaymentQrResponse;
import java.io.ByteArrayOutputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentQrActivity extends AppCompatActivity {

    private ActivityPaymentQrBinding binding;
    private Bitmap qrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mã QR Thanh toán");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String userId  = getIntent().getStringExtra("user_id");
        int    amount  = getIntent().getIntExtra("amount", 0);
        String desc    = getIntent().getStringExtra("description");
        String bankName = getIntent().getStringExtra("bank_name");
        String acctNum  = getIntent().getStringExtra("account_number");
        String acctHolder = getIntent().getStringExtra("account_holder");

        // Show static bank info
        binding.tvBankName.setText(bankName != null && !bankName.isEmpty() ? bankName : "—");
        binding.tvAccountNumber.setText(acctNum != null && !acctNum.isEmpty() ? acctNum : "—");
        binding.tvAccountHolder.setText(acctHolder != null && !acctHolder.isEmpty() ? acctHolder : "—");
        binding.tvQrAmount.setText(amount > 0 ? String.format("%,dđ", amount) : "Số tiền tùy ý");
        binding.tvQrDesc.setText(desc != null && !desc.isEmpty() ? desc : "Thanh toán chi tiêu nhóm");

        if (userId != null) {
            loadQr(userId, amount, desc);
        }

        binding.btnShareQr.setOnClickListener(v -> shareQr());
    }

    private void loadQr(String userId, int amount, String desc) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String safeDesc = (desc != null && !desc.isEmpty()) ? desc : "Thanh toan chia tien";

        RetrofitClient.getApiService()
                .getPaymentQr(userId, amount, safeDesc)
                .enqueue(new Callback<PaymentQrResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PaymentQrResponse> call,
                                           @NonNull Response<PaymentQrResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            String qrUrl = response.body().qrUrl;
                            if (qrUrl != null && !qrUrl.isEmpty()) {
                                loadQrImage(qrUrl);
                            } else {
                                Toast.makeText(PaymentQrActivity.this,
                                        "Không tải được mã QR. Hãy cập nhật thông tin ngân hàng.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaymentQrResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(PaymentQrActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadQrImage(String url) {
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        qrBitmap = resource;
                        binding.ivQrCode.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void shareQr() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Mã QR chưa tải xong", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(
                    getContentResolver(), qrBitmap, "payment_qr", null);
            Uri uri = Uri.parse(path);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Quét mã QR để thanh toán cho tôi qua " +
                    binding.tvBankName.getText());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã QR"));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể chia sẻ", Toast.LENGTH_SHORT).show();
        }
    }
}
