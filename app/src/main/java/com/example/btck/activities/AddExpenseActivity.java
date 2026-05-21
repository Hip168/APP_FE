package com.example.btck.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.adapters.MemberSplitAdapter;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.ActivityAddExpenseBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.*;
import com.example.btck.viewmodel.EventViewModel;
import com.example.btck.viewmodel.ExpenseViewModel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

    // Camera / Gallery
    private Uri photoUri = null;
    private File photoFile = null;
    private String createdExpenseId = null; // lưu lại để upload ảnh sau khi tạo expense xong

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoFile != null) {
                    binding.ivReceiptPreview.setImageURI(photoUri);
                    binding.ivReceiptPreview.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "✅ Đã chụp ảnh hoá đơn", Toast.LENGTH_SHORT).show();
                    runOCR(photoUri);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    photoFile = null; // gallery file handled differently
                    binding.ivReceiptPreview.setImageURI(uri);
                    binding.ivReceiptPreview.setVisibility(View.VISIBLE);
                    runOCR(uri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            });

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
        setupCameraButton();
        setupSaveActions();
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
        splitAdapter.setOnSplitChangedListener(this::recalculateSplits);
        binding.rvSplits.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSplits.setAdapter(splitAdapter);
        binding.rvSplits.setNestedScrollingEnabled(false);

        binding.rgSplitMethod.setOnCheckedChangeListener((group, checkedId) -> {
            splitAdapter.setEqualSplitMode(checkedId == com.example.btck.R.id.rbEqual);
            recalculateSplits();
        });

        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                binding.tilAmount.setError(null);
                recalculateSplits();
            }
        });
    }

    private void setupSaveActions() {
        binding.btnSave.setOnClickListener(v -> saveExpense());
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
                createdExpenseId = expense.id;
                // Nếu có ảnh, upload lên server
                if (photoUri != null) {
                    uploadReceiptImage(expense.id);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Thêm chi tiêu thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
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

    // ── Camera & Gallery ────────────────────────────────────────────────────────
    private void setupCameraButton() {
        if (binding.btnCamera == null) return; // chỉ chạy nếu layout có nút
        binding.btnCamera.setOnClickListener(v -> showImageOptions());
    }

    private void showImageOptions() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("📷 Chụp ảnh hoá đơn")
                .setItems(new String[]{"Dùng Camera", "Chọn từ Thư viện"}, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            launchCamera();
                        } else {
                            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                        }
                    } else {
                        // Gallery
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void launchCamera() {
        try {
            photoFile = File.createTempFile(
                    "receipt_" + System.currentTimeMillis(),
                    ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    photoFile
            );
            cameraLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Không tạo được file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOCR(Uri uri) {
        if (uri == null) return;
        Toast.makeText(this, "🔍 Đang phân tích hoá đơn...", Toast.LENGTH_SHORT).show();
        try {
            com.google.mlkit.vision.common.InputImage image =
                    com.google.mlkit.vision.common.InputImage.fromFilePath(this, uri);
            com.google.mlkit.vision.text.TextRecognizer recognizer =
                    com.google.mlkit.vision.text.TextRecognition.getClient(
                            com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();
                        parseReceiptText(text);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể đọc hoá đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc tệp ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void parseReceiptText(String text) {
        if (TextUtils.isEmpty(text)) return;

        String[] lines = text.split("\n");
        long detectedAmount = 0;
        String detectedDescription = "";

        List<String> totalKeywords = Arrays.asList(
            "tong cong", "tổng cộng", "thành tiền", "thanh tien",
            "thanh toán", "thanh toan", "tổng tiền", "tong tien",
            "total", "grand total", "net amount", "cộng", "cong"
        );

        List<Long> numbers = new ArrayList<>();
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\b\\d{1,3}([.,]\\d{3})+\\b|\\b\\d{4,9}\\b");

        for (String line : lines) {
            String lowerLine = line.toLowerCase();

            if (TextUtils.isEmpty(detectedDescription)) {
                String trimmed = line.trim();
                if (trimmed.length() > 3 && !trimmed.matches(".*\\d{5,}.*") && !trimmed.contains("/") && !trimmed.contains(":")) {
                    detectedDescription = trimmed;
                }
            }

            boolean hasTotalKeyword = false;
            for (String kw : totalKeywords) {
                if (lowerLine.contains(kw)) {
                    hasTotalKeyword = true;
                    break;
                }
            }

            if (hasTotalKeyword) {
                java.util.regex.Matcher m = numberPattern.matcher(line);
                long largestInLine = 0;
                while (m.find()) {
                    try {
                        String numStr = m.group().replaceAll("[.,]", "");
                        long val = Long.parseLong(numStr);
                        if (val >= 1000 && val <= 50000000) {
                            if (val > largestInLine) {
                                largestInLine = val;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                if (largestInLine > 0) {
                    detectedAmount = largestInLine;
                    break;
                }
            }

            java.util.regex.Matcher m = numberPattern.matcher(line);
            while (m.find()) {
                try {
                    String numStr = m.group().replaceAll("[.,]", "");
                    long val = Long.parseLong(numStr);
                    if (val >= 1000 && val <= 50000000) {
                        numbers.add(val);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        if (detectedAmount == 0 && !numbers.isEmpty()) {
            long max = 0;
            for (long n : numbers) {
                if (n > max) max = n;
            }
            detectedAmount = max;
        }

        if (TextUtils.isEmpty(detectedDescription) && lines.length > 0) {
            detectedDescription = lines[0].trim();
        }

        if (detectedDescription.length() > 50) {
            detectedDescription = detectedDescription.substring(0, 47) + "...";
        }

        if (detectedAmount > 0) {
            binding.etAmount.setText(String.valueOf(detectedAmount));
            Toast.makeText(this, "🔍 Đã quét được số tiền: " + String.format(Locale.getDefault(), "%,dđ", detectedAmount), Toast.LENGTH_LONG).show();
        }
        if (!TextUtils.isEmpty(detectedDescription)) {
            binding.etDescription.setText(detectedDescription);
        }
    }

    private void uploadReceiptImage(String expenseId) {
        try {
            File fileToUpload;
            if (photoFile != null && photoFile.exists()) {
                fileToUpload = photoFile;
            } else {
                // Gallery URI → copy to temp file
                fileToUpload = File.createTempFile("receipt_upload", ".jpg",
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                try (java.io.InputStream in = getContentResolver().openInputStream(photoUri);
                     java.io.OutputStream out = new java.io.FileOutputStream(fileToUpload)) {
                    if (in != null) {
                        byte[] buf = new byte[4096];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    }
                }
            }

            RequestBody reqBody = RequestBody.create(fileToUpload, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", fileToUpload.getName(), reqBody);

            RetrofitClient.getApiService()
                    .uploadExpenseImage(eventId, expenseId, part)
                    .enqueue(new retrofit2.Callback<ExpensePublic>() {
                        @Override
                        public void onResponse(retrofit2.Call<ExpensePublic> call,
                                               retrofit2.Response<ExpensePublic> response) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddExpenseActivity.this,
                                    "Thêm chi tiêu và ảnh thành công!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                        @Override
                        public void onFailure(retrofit2.Call<ExpensePublic> call, Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                            // Expense đã tạo xong, chỉ upload ảnh thất bại
                            Toast.makeText(AddExpenseActivity.this,
                                    "Chi tiêu đã lưu, nhưng upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        } catch (IOException e) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
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
        if (binding.rbCustom.isChecked()) {
            splitAdapter.notifyDataSetChanged();
            return;
        }

        String amountStr = binding.etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            for (MemberSplitAdapter.SplitItem item : splitItems) item.amountOwed = 0;
            splitAdapter.notifyDataSetChanged();
            return;
        }
        long total;
        try { total = Long.parseLong(amountStr.replace(",", "").replace(".", "")); }
        catch (NumberFormatException e) { return; }

        List<MemberSplitAdapter.SplitItem> selected = new ArrayList<>();
        for (MemberSplitAdapter.SplitItem item : splitItems) {
            if (item.isSelected) selected.add(item);
            else item.amountOwed = 0;
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
