package com.example.btck.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.databinding.ActivityExpenseDetailBinding;
import com.example.btck.models.ExpensePublic;
import com.example.btck.models.ExpenseSplitPublic;
import com.example.btck.viewmodel.ExpenseViewModel;

public class ExpenseDetailActivity extends AppCompatActivity {

    private ActivityExpenseDetailBinding binding;
    private ExpenseViewModel viewModel;
    private String eventId, expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("event_id");
        expenseId = getIntent().getStringExtra("expense_id");

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        observeData();
        viewModel.loadExpense(eventId, expenseId);
    }

    private void observeData() {
        viewModel.selectedExpense.observe(this, expense -> {
            if (expense != null) displayExpense(expense);
        });

        viewModel.deleteResult.observe(this, deleted -> {
            if (deleted != null && deleted) {
                Toast.makeText(this, "Đã xoá chi tiêu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void displayExpense(ExpensePublic expense) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(expense.description);
        binding.tvDescription.setText(expense.description);
        binding.tvAmount.setText(expense.getFormattedAmount());
        binding.tvPayer.setText(expense.getPayerDisplayName());
        binding.tvDate.setText(expense.expenseDate != null ? expense.expenseDate : "");
        binding.tvCategory.setText(getCategoryLabel(expense.category));

        // Build splits text
        if (expense.splits != null && !expense.splits.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ExpenseSplitPublic split : expense.splits) {
                sb.append(split.getDisplayName())
                  .append(": ").append(String.format("%,dđ", split.amountOwed)).append("\n");
            }
            binding.tvSplits.setText(sb.toString().trim());
        }

        // Load receipt image
        if (expense.imageUrl != null && !expense.imageUrl.isEmpty()) {
            binding.ivReceipt.setVisibility(View.VISIBLE);
            binding.tvNoReceipt.setVisibility(View.GONE);
            com.bumptech.glide.Glide.with(this)
                    .load(com.example.btck.utils.ImageUtils.getFullImageUrl(expense.imageUrl))
                    .into(binding.ivReceipt);

            // Cho phép click vào ảnh để phóng to toàn màn hình
            binding.ivReceipt.setOnClickListener(v -> showFullImage(expense.imageUrl));
        } else {
            binding.ivReceipt.setVisibility(View.GONE);
            binding.tvNoReceipt.setVisibility(View.VISIBLE);
        }

        binding.btnGoToSimplified.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, GroupDetailActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("target_tab", 2); // 2 is the index of "Đơn giản hóa" (Simplified) tab
            startActivity(intent);
        });

        binding.btnDelete.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Xoá chi tiêu")
                    .setMessage("Bạn có chắc muốn xoá chi tiêu này?")
                    .setPositiveButton("Xoá", (d, w) -> viewModel.deleteExpense(eventId, expenseId))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    private String getCategoryLabel(String cat) {
        if (cat == null) return "Khác";
        switch (cat.toLowerCase()) {
            case "food": return "🍜 Ăn uống";
            case "transport": return "🚗 Di chuyển";
            case "entertainment": return "🎉 Giải trí";
            case "shopping": return "🛍️ Mua sắm";
            case "accommodation": return "🏨 Lưu trú";
            default: return "📋 Khác";
        }
    }

    private void showFullImage(String imageUrl) {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        // Tạo root layout dạng FrameLayout bằng code
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackgroundColor(android.graphics.Color.BLACK);
        
        // Tạo ImageView hiển thị ảnh
        android.widget.ImageView imageView = new android.widget.ImageView(this);
        android.widget.FrameLayout.LayoutParams imageParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        root.addView(imageView);
        
        // Tạo nút Đóng (X) góc trên bên phải
        android.widget.ImageView btnClose = new android.widget.ImageView(this);
        int size = (int) (40 * getResources().getDisplayMetrics().density + 0.5f);
        android.widget.FrameLayout.LayoutParams closeParams = new android.widget.FrameLayout.LayoutParams(size, size);
        closeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        int margin = (int) (20 * getResources().getDisplayMetrics().density + 0.5f);
        closeParams.setMargins(0, margin, margin, 0);
        btnClose.setLayoutParams(closeParams);
        btnClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnClose.setColorFilter(android.graphics.Color.WHITE);
        
        // Tạo nền tròn mờ cho nút Đóng
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        bg.setColor(android.graphics.Color.parseColor("#80000000")); // Đen 50% trong suốt
        btnClose.setBackground(bg);
        int padding = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
        btnClose.setPadding(padding, padding, padding, padding);
        root.addView(btnClose);
        
        dialog.setContentView(root);
        
        // Load ảnh hóa đơn chất lượng cao bằng Glide
        com.bumptech.glide.Glide.with(this)
                .load(com.example.btck.utils.ImageUtils.getFullImageUrl(imageUrl))
                .into(imageView);
                
        // Click vào ảnh hoặc nút đóng đều tắt dialog
        imageView.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}
