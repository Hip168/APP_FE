package com.example.btck.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.adapters.MemberSplitAdapter;
import com.example.btck.databinding.ActivityAddExpenseBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.*;
import com.example.btck.viewmodel.EventViewModel;
import com.example.btck.viewmodel.ExpenseViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddExpenseActivity extends AppCompatActivity {

    private ActivityAddExpenseBinding binding;
    private ExpenseViewModel expenseViewModel;
    private EventViewModel eventViewModel;
    private MemberSplitAdapter splitAdapter;
    private List<EventMemberPublic> memberList = new ArrayList<>();
    private List<MemberSplitAdapter.SplitItem> splitItems = new ArrayList<>();
    private String eventId;
    private String currentUserId;
    private String selectedDate;
    private String selectedCategory = "other";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getStringExtra("event_id");
        String eventName = getIntent().getStringExtra("event_name");
        currentUserId = new TokenManager(this).getUserId();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        setupToolbar(eventName);
        setupDatePicker();
        setupCategoryChips();
        setupSplitRecyclerView();
        observeData();
        loadMembers();
    }

    private void setupToolbar(String eventName) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm chi tiêu");
            if (eventName != null) getSupportActionBar().setSubtitle(eventName);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDatePicker() {
        binding.tvDate.setText(selectedDate);
        binding.tilDate.setOnClickListener(v -> showDatePicker());
        binding.tvDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            binding.tvDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupCategoryChips() {
        binding.chipFood.setOnClickListener(v -> selectCategory("food"));
        binding.chipTransport.setOnClickListener(v -> selectCategory("transport"));
        binding.chipEntertainment.setOnClickListener(v -> selectCategory("entertainment"));
        binding.chipShopping.setOnClickListener(v -> selectCategory("shopping"));
        binding.chipAccommodation.setOnClickListener(v -> selectCategory("accommodation"));
        binding.chipOther.setOnClickListener(v -> selectCategory("other"));
        selectCategory("other");
    }

    private void selectCategory(String cat) {
        selectedCategory = cat;
        // Reset all chips
        int[] chips = {com.example.btck.R.id.chipFood, com.example.btck.R.id.chipTransport,
                com.example.btck.R.id.chipEntertainment, com.example.btck.R.id.chipShopping,
                com.example.btck.R.id.chipAccommodation, com.example.btck.R.id.chipOther};
        // Let chip group handle selection visually
    }

    private void setupSplitRecyclerView() {
        splitAdapter = new MemberSplitAdapter(splitItems);
        binding.rvSplits.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSplits.setAdapter(splitAdapter);
        binding.rvSplits.setNestedScrollingEnabled(false);

        binding.rgSplitMethod.setOnCheckedChangeListener((group, checkedId) -> recalculateSplits());
    }

    private void loadMembers() {
        eventViewModel.loadEventBalances(eventId);
        // Load members via balance which includes all members
        // Also try fetching members from event
    }

    private void observeData() {
        eventViewModel.eventBalances.observe(this, balances -> {
            if (balances != null && balances.balances != null) {
                memberList.clear();
                splitItems.clear();
                for (UserBalance b : balances.balances) {
                    EventMemberPublic m = new EventMemberPublic();
                    m.userId = b.userId;
                    m.userEmail = b.userEmail;
                    m.userFullName = b.userFullName;
                    memberList.add(m);

                    MemberSplitAdapter.SplitItem item = new MemberSplitAdapter.SplitItem();
                    item.member = m;
                    item.isSelected = true;
                    item.amountOwed = 0;
                    splitItems.add(item);
                }
                splitAdapter.notifyDataSetChanged();
                recalculateSplits();

                // Set payer spinner
                setupPayerSpinner();
            }
        });

        expenseViewModel.createdExpense.observe(this, expense -> {
            if (expense != null) {
                Toast.makeText(this, "Thêm chi tiêu thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        expenseViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupPayerSpinner() {
        List<String> names = new ArrayList<>();
        for (EventMemberPublic m : memberList) names.add(m.getDisplayName());
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPayer.setAdapter(adapter);

        // Default to current user
        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).userId != null && memberList.get(i).userId.equals(currentUserId)) {
                binding.spinnerPayer.setSelection(i);
                break;
            }
        }
    }

    private void recalculateSplits() {
        String amountStr = binding.etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) return;
        long total;
        try { total = Long.parseLong(amountStr.replace(",", "").replace(".", "")); }
        catch (NumberFormatException e) { return; }

        List<MemberSplitAdapter.SplitItem> selected = new ArrayList<>();
        for (MemberSplitAdapter.SplitItem item : splitItems) {
            if (item.isSelected) selected.add(item);
        }
        if (selected.isEmpty()) return;

        // Equal split
        long each = total / selected.size();
        long remainder = total % selected.size();
        for (int i = 0; i < selected.size(); i++) {
            selected.get(i).amountOwed = each + (i == 0 ? remainder : 0);
        }
        splitAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(com.example.btck.R.menu.add_expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == com.example.btck.R.id.action_save) {
            saveExpense();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveExpense() {
        String description = binding.etDescription.getText().toString().trim();
        String amountStr = binding.etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(description)) {
            binding.tilDescription.setError("Vui lòng nhập mô tả");
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            binding.tilAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        long amount;
        try { amount = Long.parseLong(amountStr.replace(",", "").replace(".", "")); }
        catch (Exception e) { binding.tilAmount.setError("Số tiền không hợp lệ"); return; }

        if (memberList.isEmpty()) { Toast.makeText(this, "Chưa tải được thành viên", Toast.LENGTH_SHORT).show(); return; }

        int payerPos = binding.spinnerPayer.getSelectedItemPosition();
        if (payerPos < 0 || payerPos >= memberList.size()) return;
        String payerId = memberList.get(payerPos).userId;

        List<ExpenseSplitCreate> splits = new ArrayList<>();
        long totalSplit = 0;
        for (MemberSplitAdapter.SplitItem item : splitItems) {
            if (item.isSelected && item.amountOwed > 0) {
                splits.add(new ExpenseSplitCreate(item.member.userId, item.amountOwed));
                totalSplit += item.amountOwed;
            }
        }

        if (splits.isEmpty()) { Toast.makeText(this, "Vui lòng chọn ít nhất một người", Toast.LENGTH_SHORT).show(); return; }
        if (totalSplit != amount) { Toast.makeText(this, "Tổng chia tiền phải bằng tổng chi tiêu", Toast.LENGTH_SHORT).show(); return; }

        ExpenseCreate body = new ExpenseCreate();
        body.description = description;
        body.amount = amount;
        body.category = selectedCategory;
        body.expenseDate = selectedDate;
        body.payerId = payerId;
        body.splits = splits;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        expenseViewModel.createExpense(eventId, body);
    }
}
