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
        } else {
            binding.ivReceipt.setVisibility(View.GONE);
            binding.tvNoReceipt.setVisibility(View.VISIBLE);
        }

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
}
