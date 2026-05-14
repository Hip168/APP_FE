package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.R;
import com.example.btck.adapters.ExpenseAdapter;
import com.example.btck.adapters.MemberAdapter;
import com.example.btck.databinding.ActivityGroupDetailBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.EventPublic;
import com.example.btck.models.UserBalance;
import com.example.btck.models.SimplifiedDebt;
import com.example.btck.models.ExpensePublic;
import com.example.btck.viewmodel.EventViewModel;
import com.example.btck.viewmodel.ExpenseViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private EventViewModel eventViewModel;
    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter expenseAdapter;
    private List<ExpensePublic> expenseList = new ArrayList<>();
    private String eventId;
    private String eventName;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("event_id");
        eventName = getIntent().getStringExtra("event_name");
        currentUserId = new TokenManager(this).getUserId();

        if (eventId == null) { finish(); return; }

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupTabs();
        observeData();
        loadData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventName != null ? eventName : "Chi tiết nhóm");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter(expenseList, expense -> {
            Intent intent = new Intent(this, ExpenseDetailActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("expense_id", expense.id);
            intent.putExtra("event_name", eventName);
            startActivity(intent);
        });
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(expenseAdapter);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chi tiêu"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Số dư"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đơn giản hóa"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { switchTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void switchTab(int position) {
        switch (position) {
            case 0:
                binding.rvExpenses.setVisibility(View.VISIBLE);
                binding.layoutBalance.setVisibility(View.GONE);
                binding.layoutSimplified.setVisibility(View.GONE);
                break;
            case 1:
                binding.rvExpenses.setVisibility(View.GONE);
                binding.layoutBalance.setVisibility(View.VISIBLE);
                binding.layoutSimplified.setVisibility(View.GONE);
                eventViewModel.loadEventBalances(eventId);
                break;
            case 2:
                binding.rvExpenses.setVisibility(View.GONE);
                binding.layoutBalance.setVisibility(View.GONE);
                binding.layoutSimplified.setVisibility(View.VISIBLE);
                eventViewModel.loadSimplifiedDebts(eventId);
                break;
        }
    }

    private void observeData() {
        expenseViewModel.expenses.observe(this, result -> {
            if (result != null && result.data != null) {
                expenseList.clear();
                expenseList.addAll(result.data);
                expenseAdapter.notifyDataSetChanged();
                boolean empty = result.data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvExpenses.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.tvExpenseCount.setText(result.count + " chi tiêu");
            }
        });

        eventViewModel.selectedEvent.observe(this, event -> {
            if (event != null) {
                binding.tvMemberCount.setText(event.memberCount + " thành viên");
                binding.tvExpenseCount.setText(event.expenseCount + " chi tiêu");
                binding.toolbar.setTitle(event.name);
            }
        });

        eventViewModel.eventStats.observe(this, stats -> {
            if (stats != null) {
                binding.tvTotalSpent.setText(String.format("%,dđ", stats.totalSpent));
                long net = stats.yourNetBalance;
                if (net >= 0) {
                    binding.tvYourBalance.setText(String.format("+%,dđ", net));
                    binding.tvYourBalance.setTextColor(getColor(R.color.color_owed));
                } else {
                    binding.tvYourBalance.setText(String.format("-%,dđ", Math.abs(net)));
                    binding.tvYourBalance.setTextColor(getColor(R.color.color_owe));
                }
            }
        });

        eventViewModel.eventBalances.observe(this, balances -> {
            if (balances != null && balances.balances != null) {
                StringBuilder sb = new StringBuilder();
                for (UserBalance b : balances.balances) {
                    String name = b.getDisplayName();
                    long net = b.netBalance;
                    if (net > 0) sb.append(name).append(" được nhận: ").append(String.format("%,dđ", net)).append("\n");
                    else if (net < 0) sb.append(name).append(" cần trả: ").append(String.format("%,dđ", Math.abs(net))).append("\n");
                    else sb.append(name).append(": ✓ Đã cân bằng\n");
                }
                binding.tvBalanceDetail.setText(sb.toString().trim());
            }
        });

        eventViewModel.simplifiedDebts.observe(this, debts -> {
            if (debts != null && debts.debts != null) {
                if (debts.debts.isEmpty()) {
                    binding.tvSimplifiedDetail.setText("✅ Tất cả đã thanh toán xong!");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (SimplifiedDebt d : debts.debts) {
                        sb.append(d.getFromDisplayName()).append(" → ").append(d.getToDisplayName())
                          .append(": ").append(String.format("%,dđ", d.amount)).append("\n");
                    }
                    binding.tvSimplifiedDetail.setText(sb.toString().trim());
                }
            }
        });

        expenseViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadData() {
        expenseViewModel.loadExpenses(eventId);
        eventViewModel.loadEvent(eventId);
        eventViewModel.loadEventStats(eventId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.group_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_expense) {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("event_name", eventName);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add_member) {
            showAddMemberDialog();
            return true;
        } else if (id == R.id.action_invite) {
            eventViewModel.createInviteCode(eventId);
            eventViewModel.inviteCode.observe(this, invite -> {
                if (invite != null) {
                    String shareText = "Tham gia nhóm \"" + eventName + "\" với mã: " + invite.code;
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"));
                    eventViewModel.inviteCode.setValue(null);
                }
            });
            return true;
        } else if (id == R.id.action_settle) {
            Intent intent = new Intent(this, SettlementActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("event_name", eventName);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddMemberDialog() {
        android.widget.EditText etEmail = new android.widget.EditText(this);
        etEmail.setHint("Email thành viên");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        etEmail.setPadding(pad, pad, pad, pad);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thêm thành viên")
                .setView(etEmail)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    if (!email.isEmpty()) {
                        eventViewModel.addMember(eventId, email);
                        Toast.makeText(this, "Đã thêm thành viên!", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
