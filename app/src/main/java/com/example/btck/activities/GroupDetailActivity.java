package com.example.btck.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.btck.viewmodel.SettlementViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private EventViewModel eventViewModel;
    private ExpenseViewModel expenseViewModel;
    private SettlementViewModel settlementViewModel;
    private ExpenseAdapter expenseAdapter;
    private List<ExpensePublic> expenseList = new ArrayList<>();
    private String eventId;
    private String eventName;
    private String currentUserId;
    private boolean isFirstLoad = true; // tránh gọi loadData() 2 lần khi khởi tạo
    private int currentTab = 0;
    private final Set<String> pendingSettlementKeys = new HashSet<>();

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
        settlementViewModel = new ViewModelProvider(this).get(SettlementViewModel.class);

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
        currentTab = position;
        switch (position) {
            case 0:
                updateExpenseTabVisibility();
                binding.layoutBalance.setVisibility(View.GONE);
                binding.layoutSimplified.setVisibility(View.GONE);
                break;
            case 1:
                binding.rvExpenses.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.layoutBalance.setVisibility(View.VISIBLE);
                binding.layoutSimplified.setVisibility(View.GONE);
                showBalanceStatus("Đang tải...");
                eventViewModel.loadEventBalances(eventId);
                break;
            case 2:
                binding.rvExpenses.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.layoutBalance.setVisibility(View.GONE);
                binding.layoutSimplified.setVisibility(View.VISIBLE);
                showSimplifiedStatus("Đang tải...");
                eventViewModel.loadSimplifiedDebts(eventId);
                break;
        }
    }

    private void updateExpenseTabVisibility() {
        boolean empty = expenseList.isEmpty();
        binding.layoutEmpty.setVisibility(currentTab == 0 && empty ? View.VISIBLE : View.GONE);
        binding.rvExpenses.setVisibility(currentTab == 0 && !empty ? View.VISIBLE : View.GONE);
    }

    private void observeData() {
        expenseViewModel.expenses.observe(this, result -> {
            if (result != null && result.data != null) {
                expenseList.clear();
                expenseList.addAll(result.data);
                expenseAdapter.notifyDataSetChanged();
                updateExpenseTabVisibility();
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
                renderBalances(balances.balances);
            }
        });

        eventViewModel.simplifiedDebts.observe(this, debts -> {
            if (debts != null && debts.debts != null) {
                renderSimplifiedDebts(debts.debts);
            }
        });

        settlementViewModel.createdSettlement.observe(this, settlement -> {
            if (settlement != null) {
                pendingSettlementKeys.clear();
                Toast.makeText(this, "Đã ghi nhận thanh toán", Toast.LENGTH_SHORT).show();
                settlementViewModel.createdSettlement.setValue(null);
                loadData();
                if (currentTab == 1) eventViewModel.loadEventBalances(eventId);
                if (currentTab == 2) eventViewModel.loadSimplifiedDebts(eventId);
            }
        });

        eventViewModel.addedMember.observe(this, member -> {
            if (member != null) {
                Toast.makeText(this, "Đã thêm thành viên!", Toast.LENGTH_SHORT).show();
                eventViewModel.addedMember.setValue(null);
                loadData();
                if (currentTab == 1) eventViewModel.loadEventBalances(eventId);
                if (currentTab == 2) eventViewModel.loadSimplifiedDebts(eventId);
            }
        });

        expenseViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                expenseViewModel.errorMessage.setValue(null); // reset tránh replay
            }
        });

        eventViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                if (currentTab == 1 && binding.layoutBalance.getVisibility() == View.VISIBLE) {
                    binding.tvBalanceDetail.setText("Không tải được số dư.\n" + msg);
                } else if (currentTab == 2 && binding.layoutSimplified.getVisibility() == View.VISIBLE) {
                    binding.tvSimplifiedDetail.setText("Không tải được đơn giản hóa.\n" + msg);
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                eventViewModel.errorMessage.setValue(null); // reset tránh replay
            }
        });

        settlementViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                pendingSettlementKeys.clear();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                settlementViewModel.errorMessage.setValue(null);
            }
        });

        // Observer mã mời — chỉ đăng ký 1 lần ở đây
        eventViewModel.inviteCode.observe(this, invite -> {
            if (invite != null && invite.code != null) {
                // Debug: in mã ra Logcat để kiểm tra
                android.util.Log.d("INVITE_CODE", "Mã mới tạo: " + invite.code
                        + " | Hết hạn: " + invite.expiresAt);

                String expiry = invite.expiresAt != null
                        ? "\n(Hết hạn: " + invite.expiresAt.replace("T", " ").substring(0, 16) + " UTC)"
                        : "";
                String shareText = "Tham gia nhóm \"" + eventName + "\" với mã: " + invite.code
                        + expiry
                        + "\nHoặc dán vào ô \"Nhập mã mời\" trong app.";

                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("invite_code", invite.code));
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"));
                // Reset để không trigger lại khi Activity resume
                eventViewModel.inviteCode.setValue(null);
            }
        });
    } // end observeData

    private void renderBalances(List<UserBalance> balances) {
        binding.layoutBalanceContent.removeAllViews();

        if (balances.isEmpty()) {
            showBalanceStatus("Chưa có dữ liệu số dư.");
            return;
        }

        TextView title = new TextView(this);
        title.setText("Số dư thành viên");
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(12));
        binding.layoutBalanceContent.addView(title);

        for (UserBalance balance : balances) {
            binding.layoutBalanceContent.addView(createBalanceCard(balance));
        }
    }

    private View createBalanceCard(UserBalance balance) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getColor(R.color.background_white));
        card.setCardElevation(dp(1));
        card.setRadius(dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.addView(row);

        TextView initial = new TextView(this);
        String name = balance.getDisplayName();
        initial.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?");
        initial.setTextColor(getColor(R.color.text_dark));
        initial.setTextSize(16);
        initial.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        initial.setGravity(android.view.Gravity.CENTER);
        initial.setBackgroundResource(R.drawable.bg_mint_circle);
        row.addView(initial, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMarginStart(dp(12));
        row.addView(texts, textParams);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(getColor(R.color.text_dark));
        tvName.setTextSize(15);
        tvName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        texts.addView(tvName);

        TextView status = new TextView(this);
        long net = balance.netBalance;
        if (net > 0) {
            status.setText("Được nhận");
            status.setTextColor(getColor(R.color.color_owed));
        } else if (net < 0) {
            status.setText("Cần trả");
            status.setTextColor(getColor(R.color.color_owe));
        } else {
            status.setText("Đã cân bằng");
            status.setTextColor(getColor(R.color.text_secondary));
        }
        status.setTextSize(13);
        texts.addView(status);

        TextView amount = new TextView(this);
        amount.setText(net == 0 ? "0đ" : String.format("%s%,dđ", net > 0 ? "+" : "-", Math.abs(net)));
        amount.setTextColor(net >= 0 ? getColor(R.color.color_owed) : getColor(R.color.color_owe));
        amount.setTextSize(15);
        amount.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(amount);

        return card;
    }

    private void showBalanceStatus(String message) {
        binding.layoutBalanceContent.removeAllViews();
        binding.layoutBalanceContent.addView(binding.tvBalanceDetail);
        binding.tvBalanceDetail.setText(message);
    }

    private void renderSimplifiedDebts(List<SimplifiedDebt> debts) {
        binding.layoutSimplifiedContent.removeAllViews();

        if (debts.isEmpty()) {
            showSettledState();
            return;
        }

        TextView title = new TextView(this);
        title.setText("Các khoản nên thanh toán");
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(12));
        binding.layoutSimplifiedContent.addView(title);

        for (SimplifiedDebt debt : debts) {
            binding.layoutSimplifiedContent.addView(createDebtCard(debt));
        }
    }

    private View createDebtCard(SimplifiedDebt debt) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getColor(R.color.background_white));
        card.setCardElevation(dp(2));
        card.setRadius(dp(12));
        card.setStrokeColor(getColor(R.color.mint_primary));
        card.setStrokeWidth(dp(1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(content);

        TextView amount = new TextView(this);
        amount.setText(String.format("%,dđ", debt.amount));
        amount.setTextColor(getColor(R.color.mint_dark));
        amount.setTextSize(22);
        amount.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(amount);

        TextView route = new TextView(this);
        route.setText(debt.getFromDisplayName() + " trả cho " + debt.getToDisplayName());
        route.setTextColor(getColor(R.color.text_dark));
        route.setTextSize(15);
        route.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        route.setPadding(0, dp(6), 0, dp(2));
        content.addView(route);

        TextView hint = new TextView(this);
        hint.setText("Quét QR của người nhận hoặc ghi nhận khi tiền đã chuyển.");
        hint.setTextColor(getColor(R.color.text_secondary));
        hint.setTextSize(13);
        hint.setPadding(0, 0, 0, dp(12));
        content.addView(hint);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(actions);

        MaterialButton qrButton = new MaterialButton(this);
        qrButton.setText("Mã QR");
        qrButton.setTextColor(getColor(R.color.mint_dark));
        qrButton.setStrokeColorResource(R.color.mint_primary);
        qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.background_white)));
        qrButton.setOnClickListener(v -> openDebtQr(debt));
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        qrParams.setMarginEnd(dp(8));
        actions.addView(qrButton, qrParams);

        MaterialButton paidButton = new MaterialButton(this);
        paidButton.setText(getSettlementButtonText(debt));
        paidButton.setTextColor(getColor(R.color.text_dark));
        paidButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.mint_primary)));
        paidButton.setEnabled(canConfirmDebt(debt));
        paidButton.setOnClickListener(v -> {
            paidButton.setEnabled(false);
            confirmDebtPaid(debt);
        });
        LinearLayout.LayoutParams paidParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        actions.addView(paidButton, paidParams);

        return card;
    }

    private void showSimplifiedStatus(String message) {
        binding.layoutSimplifiedContent.removeAllViews();
        binding.layoutSimplifiedContent.addView(binding.tvSimplifiedDetail);
        binding.tvSimplifiedDetail.setText(message);
    }

    private void showSettledState() {
        binding.layoutSimplifiedContent.removeAllViews();

        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(getColor(R.color.background_white));
        card.setCardElevation(dp(1));
        card.setRadius(dp(14));
        binding.layoutSimplifiedContent.addView(card, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(22), dp(20), dp(20));
        card.addView(content);

        TextView icon = new TextView(this);
        icon.setText("✓");
        icon.setTextSize(28);
        icon.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        icon.setGravity(android.view.Gravity.CENTER);
        icon.setTextColor(getColor(R.color.color_owed));
        icon.setBackgroundResource(R.drawable.bg_mint_circle_light);
        content.addView(icon, new LinearLayout.LayoutParams(dp(56), dp(56)));

        TextView title = new TextView(this);
        title.setText("Tất cả đã thanh toán xong");
        title.setTextColor(getColor(R.color.text_dark));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, dp(14), 0, dp(6));
        content.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Nhóm hiện không còn khoản cần bù trừ. Khi có chi tiêu mới, đề xuất thanh toán sẽ xuất hiện ở đây.");
        desc.setTextColor(getColor(R.color.text_secondary));
        desc.setTextSize(13);
        desc.setGravity(android.view.Gravity.CENTER);
        desc.setLineSpacing(dp(2), 1f);
        content.addView(desc);

        MaterialButton historyButton = new MaterialButton(this);
        historyButton.setText("Xem lịch sử thanh toán");
        historyButton.setTextColor(getColor(R.color.mint_dark));
        historyButton.setStrokeColorResource(R.color.mint_primary);
        historyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.background_white)));
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettlementActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("event_name", eventName);
            startActivity(intent);
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
        );
        buttonParams.setMargins(0, dp(18), 0, 0);
        content.addView(historyButton, buttonParams);
    }

    private void openDebtQr(SimplifiedDebt debt) {
        Intent intent = new Intent(this, PaymentQrActivity.class);
        intent.putExtra("user_id", debt.toUserId);
        intent.putExtra("amount", debt.amount);
        intent.putExtra("description", "Thanh toan " + eventName);
        startActivity(intent);
    }

    private boolean canConfirmDebt(SimplifiedDebt debt) {
        return currentUserId != null && currentUserId.equals(debt.fromUserId);
    }

    private String getSettlementButtonText(SimplifiedDebt debt) {
        if (currentUserId != null && currentUserId.equals(debt.toUserId)) {
            return "Chờ người trả";
        }
        if (currentUserId != null && currentUserId.equals(debt.fromUserId)) {
            return "Tôi đã gửi tiền";
        }
        return "Ghi nhận";
    }

    private void confirmDebtPaid(SimplifiedDebt debt) {
        if (currentUserId == null || !currentUserId.equals(debt.fromUserId)) {
            Toast.makeText(this, "Chỉ người cần trả mới có thể ghi nhận thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        String pendingKey = debt.fromUserId + ":" + debt.toUserId + ":" + debt.amount;
        if (!pendingSettlementKeys.add(pendingKey)) {
            Toast.makeText(this, "Đang ghi nhận thanh toán, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }
        String note = "Người trả xác nhận đã gửi tiền";
        settlementViewModel.createSettlement(eventId, debt.fromUserId, debt.toUserId, debt.amount, note);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadData() {
        expenseViewModel.loadExpenses(eventId);
        eventViewModel.loadEvent(eventId);
        eventViewModel.loadEventStats(eventId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstLoad) {
            isFirstLoad = false; // onCreate đã gọi rồi, bỏ qua lần đầu
        } else {
            loadData(); // chỉ reload khi thực sự quay lại từ activity khác
        }
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
            // Reset trước để tránh trigger observer cũ
            eventViewModel.inviteCode.setValue(null);
            eventViewModel.createInviteCode(eventId);
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
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
