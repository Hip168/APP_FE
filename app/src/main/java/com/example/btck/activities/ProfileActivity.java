package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.R;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.ActivityProfileBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.BankInfo;
import com.example.btck.models.BanksResponse;
import com.example.btck.models.UpdatePasswordRequest;
import com.example.btck.models.UpdateUserMeRequest;
import com.example.btck.models.UserPublic;
import com.example.btck.repository.UserRepository;
import com.example.btck.viewmodel.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AuthViewModel authViewModel;
    private UserRepository userRepository;
    private TokenManager tokenManager;
    private UserPublic currentUser;
    private List<BankInfo> bankList = new ArrayList<>();
    private BankInfo selectedBankInfo = null; // Ngân hàng đang được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userRepository = new UserRepository(this);

        setupToolbar();
        setupClickListeners();
        observeData();
        loadUserInfo();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ cá nhân");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserInfo() {
        String name  = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();

        if (name != null)  binding.tvFullName.setText(name);
        if (email != null) binding.tvEmail.setText(email);

        if (name != null && !name.isEmpty()) {
            binding.tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        authViewModel.fetchCurrentUser();
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnBankInfo.setOnClickListener(v -> showBankInfoDialog());
        binding.btnShowQr.setOnClickListener(v -> openQrScreen());
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirm());
    }

    private void observeData() {
        authViewModel.currentUser.observe(this, user -> {
            if (user != null) {
                currentUser = user;
                binding.tvFullName.setText(user.getDisplayName());
                binding.tvEmail.setText(user.email != null ? user.email : "");
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    binding.tvAvatarInitial.setText(
                            String.valueOf(user.getDisplayName().charAt(0)).toUpperCase());
                }
            }
        });

        authViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Edit Profile ────────────────────────────────────────────────────────────
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etFullName);

        if (currentUser != null) etName.setText(currentUser.getDisplayName());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chỉnh sửa hồ sơ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText() != null
                            ? etName.getText().toString().trim() : "";
                    if (!newName.isEmpty()) {
                        UpdateUserMeRequest req = new UpdateUserMeRequest();
                        req.fullName = newName;
                        userRepository.updateMe(req, new UserRepository.UserCallback() {
                            @Override
                            public void onSuccess(UserPublic user) {
                                tokenManager.saveUserInfo(user.id, user.email, user.getDisplayName());
                                runOnUiThread(() -> {
                                    binding.tvFullName.setText(user.getDisplayName());
                                    Toast.makeText(ProfileActivity.this,
                                            "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                });
                            }
                            @Override
                            public void onError(String message) {
                                runOnUiThread(() ->
                                        Toast.makeText(ProfileActivity.this,
                                                message, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ── Change Password ─────────────────────────────────────────────────────────
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew     = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Đổi", (dialog, which) -> {
                    String current = etCurrent.getText() != null ? etCurrent.getText().toString() : "";
                    String newPw   = etNew.getText() != null ? etNew.getText().toString() : "";
                    String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

                    if (current.isEmpty() || newPw.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPw.equals(confirm)) {
                        Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UpdatePasswordRequest req = new UpdatePasswordRequest(current, newPw);
                    userRepository.updatePassword(req, new UserRepository.SimpleCallback() {
                        @Override public void onSuccess() {
                            runOnUiThread(() ->
                                    Toast.makeText(ProfileActivity.this,
                                            "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show());
                        }
                        @Override public void onError(String message) {
                            runOnUiThread(() ->
                                    Toast.makeText(ProfileActivity.this,
                                            message, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ── Bank Info ───────────────────────────────────────────────────────────────
    private void showBankInfoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bank_info, null);
        AutoCompleteTextView spinnerBank  = dialogView.findViewById(R.id.spinnerBank);
        TextInputEditText etAccountNumber = dialogView.findViewById(R.id.etAccountNumber);
        TextInputEditText etAccountHolder = dialogView.findViewById(R.id.etAccountHolder);

        // Reset selectedBankInfo
        selectedBankInfo = null;

        // Pre-fill existing values
        if (currentUser != null) {
            if (currentUser.bankName != null)      spinnerBank.setText(currentUser.bankName, false);
            if (currentUser.accountNumber != null) etAccountNumber.setText(currentUser.accountNumber);
            if (currentUser.accountHolder != null) etAccountHolder.setText(currentUser.accountHolder);
        }

        // Load bank list for dropdown
        if (bankList.isEmpty()) {
            RetrofitClient.getApiService().getBanks().enqueue(new Callback<BanksResponse>() {
                @Override
                public void onResponse(@NonNull Call<BanksResponse> call,
                                       @NonNull Response<BanksResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().data != null) {
                        bankList = response.body().data;
                        runOnUiThread(() -> populateBankDropdown(spinnerBank));
                    }
                }
                @Override
                public void onFailure(@NonNull Call<BanksResponse> call, @NonNull Throwable t) {
                    // Nếu API ngân hàng lỗi, người dùng vẫn có thể gõ tay
                }
            });
        } else {
            populateBankDropdown(spinnerBank);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("🏦 Thông tin ngân hàng")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // Dùng selectedBankInfo nếu đã chọn từ dropdown, ngược lại dùng text gõ tay
                    String bankCode = "";
                    if (selectedBankInfo != null) {
                        bankCode = selectedBankInfo.getDisplayName(); // shortName hoặc code
                    } else if (spinnerBank.getText() != null) {
                        bankCode = spinnerBank.getText().toString().trim();
                    }
                    String acctNum  = etAccountNumber.getText() != null
                            ? etAccountNumber.getText().toString().trim() : "";
                    String holder   = etAccountHolder.getText() != null
                            ? etAccountHolder.getText().toString().trim().toUpperCase() : "";

                    if (acctNum.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tài khoản", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UpdateUserMeRequest req = new UpdateUserMeRequest();
                    req.bankName      = bankCode;
                    req.accountNumber = acctNum;
                    req.accountHolder = holder;

                    userRepository.updateMe(req, new UserRepository.UserCallback() {
                        @Override
                        public void onSuccess(UserPublic user) {
                            currentUser = user;
                            runOnUiThread(() ->
                                    Toast.makeText(ProfileActivity.this,
                                            "✅ Đã lưu thông tin ngân hàng!", Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onError(String message) {
                            runOnUiThread(() ->
                                    Toast.makeText(ProfileActivity.this,
                                            "Lỗi: " + message, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void populateBankDropdown(AutoCompleteTextView spinner) {
        List<String> names = new ArrayList<>();
        for (BankInfo b : bankList) {
            names.add(b.getDisplayName() + " (" + b.bin + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, names);
        spinner.setAdapter(adapter);

        // Khi người dùng chọn từ danh sách, lưu lại đối tượng BankInfo tương ứng
        spinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedString = (String) parent.getItemAtPosition(position);
            for (BankInfo b : bankList) {
                if ((b.getDisplayName() + " (" + b.bin + ")").equals(selectedString)) {
                    selectedBankInfo = b;
                    break;
                }
            }
        });
    }

    // ── Show QR ─────────────────────────────────────────────────────────────────
    private void openQrScreen() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng chờ tải thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser.bankName == null || currentUser.accountNumber == null) {
            Toast.makeText(this, "Hãy thiết lập thông tin ngân hàng trước!", Toast.LENGTH_LONG).show();
            showBankInfoDialog();
            return;
        }
        Intent intent = new Intent(this, PaymentQrActivity.class);
        intent.putExtra("user_id",        currentUser.id);
        intent.putExtra("amount",         0);
        intent.putExtra("description",    "Thanh toan chia tien");
        intent.putExtra("bank_name",      currentUser.bankName);
        intent.putExtra("account_number", currentUser.accountNumber);
        intent.putExtra("account_holder", currentUser.accountHolder);
        startActivity(intent);
    }

    // ── Logout ──────────────────────────────────────────────────────────────────
    private void showLogoutConfirm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    authViewModel.logout();
                    tokenManager.clearAll();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
