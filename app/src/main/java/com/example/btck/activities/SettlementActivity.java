package com.example.btck.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.adapters.SettlementAdapter;
import com.example.btck.databinding.ActivitySettlementBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.*;
import com.example.btck.viewmodel.EventViewModel;
import com.example.btck.viewmodel.SettlementViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class SettlementActivity extends AppCompatActivity {

    private ActivitySettlementBinding binding;
    private SettlementViewModel settlementViewModel;
    private EventViewModel eventViewModel;
    private SettlementAdapter adapter;
    private List<SettlementPublic> settlementList = new ArrayList<>();
    private List<SimplifiedDebt> debtList = new ArrayList<>();
    private String eventId;
    private String currentUserId;
    private List<UserBalance> memberBalances = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettlementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("event_id");
        String eventName = getIntent().getStringExtra("event_name");
        currentUserId = new TokenManager(this).getUserId();

        settlementViewModel = new ViewModelProvider(this).get(SettlementViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
            if (eventName != null) getSupportActionBar().setSubtitle(eventName);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();
        observeData();
        loadData();

        binding.fabSettle.setOnClickListener(v -> showSettleDialog());
    }

    private void setupRecyclerView() {
        adapter = new SettlementAdapter(settlementList);
        binding.rvSettlements.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSettlements.setAdapter(adapter);
    }

    private void observeData() {
        settlementViewModel.settlements.observe(this, result -> {
            if (result != null && result.data != null) {
                settlementList.clear();
                settlementList.addAll(result.data);
                adapter.notifyDataSetChanged();
                boolean empty = result.data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvSettlements.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        settlementViewModel.createdSettlement.observe(this, s -> {
            if (s != null) {
                Toast.makeText(this, "Ghi nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                loadData();
                settlementViewModel.createdSettlement.setValue(null);
            }
        });

        eventViewModel.simplifiedDebts.observe(this, debts -> {
            if (debts != null && debts.debts != null) {
                debtList.clear();
                debtList.addAll(debts.debts);
                updateDebtSummary();
            }
        });

        eventViewModel.eventBalances.observe(this, balances -> {
            if (balances != null && balances.balances != null) {
                memberBalances.clear();
                memberBalances.addAll(balances.balances);
            }
        });

        settlementViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateDebtSummary() {
        if (debtList.isEmpty()) {
            binding.tvDebtSummary.setText("✅ Tất cả đã thanh toán!");
            binding.tvDebtSummary.setTextColor(getColor(com.example.btck.R.color.color_owed));
        } else {
            StringBuilder sb = new StringBuilder("Cần thanh toán:\n");
            for (SimplifiedDebt d : debtList) {
                sb.append("• ").append(d.getFromDisplayName()).append(" → ")
                  .append(d.getToDisplayName()).append(": ")
                  .append(String.format("%,dđ", d.amount)).append("\n");
            }
            binding.tvDebtSummary.setText(sb.toString().trim());
            binding.tvDebtSummary.setTextColor(getColor(com.example.btck.R.color.text_dark));
        }
    }

    private void showSettleDialog() {
        if (memberBalances.isEmpty()) {
            Toast.makeText(this, "Đang tải thông tin thành viên...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build payer/receiver options from memberBalances
        List<String> names = new ArrayList<>();
        for (UserBalance b : memberBalances) names.add(b.getDisplayName());
        String[] nameArr = names.toArray(new String[0]);

        final int[] fromIndex = {-1}, toIndex = {-1};
        final long[] amount = {0};

        View dialogView = getLayoutInflater().inflate(com.example.btck.R.layout.dialog_settle, null);
        android.widget.Spinner spFrom = dialogView.findViewById(com.example.btck.R.id.spinnerFrom);
        android.widget.Spinner spTo = dialogView.findViewById(com.example.btck.R.id.spinnerTo);
        android.widget.EditText etAmount = dialogView.findViewById(com.example.btck.R.id.etSettleAmount);
        android.widget.EditText etNote = dialogView.findViewById(com.example.btck.R.id.etNote);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nameArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrom.setAdapter(adapter);
        spTo.setAdapter(adapter);

        // Pre-fill from current user's debt
        for (int i = 0; i < memberBalances.size(); i++) {
            if (memberBalances.get(i).userId != null && memberBalances.get(i).userId.equals(currentUserId)) {
                spFrom.setSelection(i);
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ghi nhận thanh toán")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String fromId = memberBalances.get(spFrom.getSelectedItemPosition()).userId;
                    String toId = memberBalances.get(spTo.getSelectedItemPosition()).userId;
                    String amtStr = etAmount.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (fromId.equals(toId)) { Toast.makeText(this, "Không thể tự thanh toán cho mình", Toast.LENGTH_SHORT).show(); return; }
                    if (amtStr.isEmpty()) { Toast.makeText(this, "Nhập số tiền", Toast.LENGTH_SHORT).show(); return; }

                    long amt;
                    try { amt = Long.parseLong(amtStr); } catch (Exception e) { return; }
                    settlementViewModel.createSettlement(eventId, fromId, toId, amt, note.isEmpty() ? null : note);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadData() {
        settlementViewModel.loadSettlements(eventId);
        eventViewModel.loadSimplifiedDebts(eventId);
        eventViewModel.loadEventBalances(eventId);
    }
}
