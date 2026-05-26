package com.example.btck.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private BankInfo selectedBankInfo = null;

    // Tham chiếu đến avatar và chữ cái viết tắt trong Dialog (để update preview ngay khi chọn ảnh)
    private android.widget.ImageView activeDialogAvatar = null;
    private android.widget.TextView activeDialogAvatarInitial = null;

    // Launcher chọn ảnh từ gallery
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            });

    // Launcher chụp ảnh bằng camera của điện thoại
    private Uri cameraImageUri = null;
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess && cameraImageUri != null) {
                    uploadAvatar(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCameraCapture();
                } else {
                    Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            });

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
        // Click vào avatar hoặc nút camera → chọn ảnh hoặc chụp ảnh mới
        android.view.View.OnClickListener pickPhotoMain = v -> {
            Toast.makeText(ProfileActivity.this, "Đã chạm nút chọn ảnh!", Toast.LENGTH_SHORT).show();
            showImageSourceOptions(v);
        };
        binding.btnEditAvatar.setOnClickListener(pickPhotoMain);
        binding.cardAvatar.setOnClickListener(pickPhotoMain);
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
                // Hiển thị avatar nếu có
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    showAvatarImage(user.avatarUrl);
                }
            }
        });

        authViewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Avatar ──────────────────────────────────────────────────────────────────
    private void showAvatarImage(String url) {
        binding.ivAvatar.setVisibility(View.VISIBLE);
        binding.tvAvatarInitial.setVisibility(View.GONE);
        Glide.with(this)
                .load(com.example.btck.utils.ImageUtils.getFullImageUrl(url))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .placeholder(R.drawable.ic_groups)
                .into(binding.ivAvatar);
    }

    private void uploadAvatar(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            // Xác định MIME type
            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            String ext = mimeType.contains("png") ? ".png" : ".jpg";

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "avatar" + ext, requestBody);

            // Show preview ngay lập tức ở cả màn hình chính và dialog
            binding.ivAvatar.setVisibility(View.VISIBLE);
            binding.tvAvatarInitial.setVisibility(View.GONE);
            Glide.with(this).load(imageUri).circleCrop().into(binding.ivAvatar);

            if (activeDialogAvatar != null && activeDialogAvatarInitial != null) {
                activeDialogAvatar.setVisibility(View.VISIBLE);
                activeDialogAvatarInitial.setVisibility(View.GONE);
                Glide.with(this).load(imageUri).circleCrop().into(activeDialogAvatar);
            }

            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            RetrofitClient.getApiService()
                    .uploadAvatar(part)
                    .enqueue(new Callback<UserPublic>() {
                        @Override
                        public void onResponse(@NonNull Call<UserPublic> call,
                                               @NonNull Response<UserPublic> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                currentUser = response.body();
                                runOnUiThread(() -> {
                                    Toast.makeText(ProfileActivity.this,
                                            "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                                    if (currentUser.avatarUrl != null) {
                                        showAvatarImage(currentUser.avatarUrl);
                                        if (activeDialogAvatar != null && activeDialogAvatarInitial != null) {
                                            Glide.with(ProfileActivity.this)
                                                    .load(com.example.btck.utils.ImageUtils.getFullImageUrl(currentUser.avatarUrl))
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .circleCrop()
                                                    .into(activeDialogAvatar);
                                        }
                                    }
                                });
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(ProfileActivity.this,
                                                "Tải ảnh thất bại. Thử lại sau.", Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<UserPublic> call, @NonNull Throwable t) {
                            runOnUiThread(() ->
                                    Toast.makeText(ProfileActivity.this,
                                            "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show());
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Hiển thị lựa chọn nguồn ảnh: Camera hoặc Thư viện dưới dạng Dialog
    private void showImageSourceOptions(View anchorView) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Chọn nguồn ảnh đại diện")
                .setItems(new String[]{"Dùng Camera", "Chọn từ Thư viện"}, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            startCameraCapture();
                        } else {
                            requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                        }
                    } else {
                        // Gallery
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    // Khởi chạy camera để chụp hình
    private void startCameraCapture() {
        Toast.makeText(this, "Đang mở máy ảnh...", Toast.LENGTH_SHORT).show();
        cameraImageUri = createCameraImageUri();
        if (cameraImageUri != null) {
            takePictureLauncher.launch(cameraImageUri);
        }
    }

    // Tạo URI tạm thời để lưu ảnh chụp từ Camera thông qua FileProvider
    private Uri createCameraImageUri() {
        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                storageDir = new File(getFilesDir(), "Pictures");
            }
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    imageFile
            );
        } catch (Exception e) {
            Toast.makeText(this, "Không tạo được file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // ── Edit Profile ────────────────────────────────────────────────────────────
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etFullName);
        android.widget.ImageView ivDialogAvatar = dialogView.findViewById(R.id.ivDialogAvatar);
        android.widget.TextView tvDialogInitial = dialogView.findViewById(R.id.tvDialogAvatarInitial);
        com.google.android.material.card.MaterialCardView btnDialogCamera =
                dialogView.findViewById(R.id.btnDialogEditAvatar);
        com.google.android.material.card.MaterialCardView cardDialogAvatar =
                dialogView.findViewById(R.id.cardDialogAvatar);

        // Lưu tham chiếu
        activeDialogAvatar = ivDialogAvatar;
        activeDialogAvatarInitial = tvDialogInitial;

        // Điền tên hiện tại
        if (currentUser != null) {
            etName.setText(currentUser.getDisplayName());
            // Hiển thị avatar trong dialog
            if (currentUser.avatarUrl != null && !currentUser.avatarUrl.isEmpty()) {
                ivDialogAvatar.setVisibility(View.VISIBLE);
                tvDialogInitial.setVisibility(View.GONE);
                Glide.with(this).load(com.example.btck.utils.ImageUtils.getFullImageUrl(currentUser.avatarUrl)).circleCrop().into(ivDialogAvatar);
            } else if (currentUser.getDisplayName() != null) {
                tvDialogInitial.setText(
                        String.valueOf(currentUser.getDisplayName().charAt(0)).toUpperCase());
            }
        }

        // Click avatar hoặc camera trong dialog → mở menu chọn nguồn ảnh (Camera hoặc Thư viện)
        android.view.View.OnClickListener pickPhoto = v -> {
            Toast.makeText(ProfileActivity.this, "Đã chạm nút chọn ảnh!", Toast.LENGTH_SHORT).show();
            showImageSourceOptions(v);
        };
        btnDialogCamera.setOnClickListener(pickPhoto);
        cardDialogAvatar.setOnClickListener(pickPhoto);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Chỉnh sửa hồ sơ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, which) -> {
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
                .create();

        dialog.setOnDismissListener(d -> {
            activeDialogAvatar = null;
            activeDialogAvatarInitial = null;
        });

        dialog.show();
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

        selectedBankInfo = null;

        if (currentUser != null) {
            if (currentUser.bankName != null)      spinnerBank.setText(currentUser.bankName, false);
            if (currentUser.accountNumber != null) etAccountNumber.setText(currentUser.accountNumber);
            if (currentUser.accountHolder != null) etAccountHolder.setText(currentUser.accountHolder);
        }

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
                public void onFailure(@NonNull Call<BanksResponse> call, @NonNull Throwable t) {}
            });
        } else {
            populateBankDropdown(spinnerBank);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thông tin ngân hàng")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String bankCode = "";
                    if (selectedBankInfo != null) {
                        bankCode = selectedBankInfo.getDisplayName();
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
                                            "Đã lưu thông tin ngân hàng!", Toast.LENGTH_SHORT).show());
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
        BankAdapter adapter = new BankAdapter(this, android.R.layout.simple_dropdown_item_1line, bankList);
        spinner.setAdapter(adapter);

        spinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedString = (String) parent.getItemAtPosition(position);
            for (BankInfo b : bankList) {
                if ((b.getDisplayName() + " (" + b.bin + ")").equals(selectedString)) {
                    selectedBankInfo = b;
                    break;
                }
            }
        });

        spinner.setOnClickListener(v -> spinner.showDropDown());
        spinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinner.showDropDown();
            }
        });
    }

    private static class BankAdapter extends ArrayAdapter<String> {
        private final List<BankInfo> banks;
        private final List<String> allItems;
        private final android.widget.Filter customFilter = new android.widget.Filter() {
            @Override
            protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                android.widget.Filter.FilterResults results = new android.widget.Filter.FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = allItems;
                    results.count = allItems.size();
                } else {
                    String query = removeAccent(constraint.toString().toLowerCase().trim());
                    List<String> matches = new ArrayList<>();
                    for (BankInfo b : banks) {
                        String normalizedDisplayName = removeAccent(b.getDisplayName().toLowerCase());
                        String normalizedName = b.name != null ? removeAccent(b.name.toLowerCase()) : "";
                        String normalizedShortName = b.shortName != null ? removeAccent(b.shortName.toLowerCase()) : "";
                        String normalizedCode = b.code != null ? removeAccent(b.code.toLowerCase()) : "";
                        String normalizedBin = b.bin != null ? b.bin.toLowerCase() : "";

                        if (normalizedDisplayName.contains(query)
                                || normalizedName.contains(query)
                                || normalizedShortName.contains(query)
                                || normalizedCode.contains(query)
                                || normalizedBin.contains(query)) {
                            matches.add(b.getDisplayName() + " (" + b.bin + ")");
                        }
                    }
                    results.values = matches;
                    results.count = matches.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults results) {
                clear();
                if (results != null && results.values != null) {
                    addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return (CharSequence) resultValue;
            }
        };

        private static String removeAccent(String s) {
            if (s == null) return "";
            String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("")
                    .replace('đ', 'd')
                    .replace('Đ', 'D');
        }

        public BankAdapter(android.content.Context context, int resource, List<BankInfo> banks) {
            super(context, resource, new ArrayList<>());
            this.banks = banks;
            this.allItems = new ArrayList<>();
            for (BankInfo b : banks) {
                this.allItems.add(b.getDisplayName() + " (" + b.bin + ")");
            }
            addAll(allItems);
        }

        @NonNull
        @Override
        public android.widget.Filter getFilter() {
            return customFilter;
        }
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
        intent.putExtra("amount",         0L);
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
