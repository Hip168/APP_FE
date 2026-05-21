package com.example.btck.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.btck.activities.LoginActivity;
import com.example.btck.databinding.FragmentProfileBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.UpdatePasswordRequest;
import com.example.btck.models.UpdateUserMeRequest;
import com.example.btck.models.UserPublic;
import com.example.btck.repository.UserRepository;
import com.example.btck.viewmodel.AuthViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private TokenManager tokenManager;
    private UserRepository userRepository;
    private UserPublic currentUser;
    private java.util.List<com.example.btck.models.BankInfo> bankList = new java.util.ArrayList<>();
    private com.example.btck.models.BankInfo selectedBankInfo = null;

    private android.widget.ImageView activeDialogAvatar = null;
    private android.widget.TextView activeDialogAvatarInitial = null;
    private android.net.Uri cameraImageUri = null;

    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> takePictureLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.TakePicture(), isSuccess -> {
                if (isSuccess && cameraImageUri != null) {
                    uploadAvatar(cameraImageUri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCameraCapture();
                } else {
                    Toast.makeText(requireContext(), "Cần quyền Camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        tokenManager = new TokenManager(requireContext());
        userRepository = new UserRepository(requireContext());

        loadUserInfo();
        setupClickListeners();
    }

    private void loadUserInfo() {
        String name = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();

        binding.tvUserName.setText(name != null ? name : "Người dùng");
        binding.tvUserEmail.setText(email != null ? email : "");

        if (name != null && !name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            String initials = parts.length >= 2
                    ? String.valueOf(parts[0].charAt(0)).toUpperCase() + String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase()
                    : String.valueOf(name.charAt(0)).toUpperCase();
            binding.tvAvatarInitial.setText(initials);
        }

        if (currentUser != null && currentUser.avatarUrl != null && !currentUser.avatarUrl.isEmpty()) {
            binding.ivAvatar.setVisibility(View.VISIBLE);
            binding.tvAvatarInitial.setVisibility(View.GONE);
            com.bumptech.glide.Glide.with(requireContext())
                    .load(com.example.btck.utils.ImageUtils.getFullImageUrl(currentUser.avatarUrl))
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(binding.ivAvatar);
        }

        authViewModel.currentUser.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                binding.tvUserName.setText(user.getDisplayName());
                binding.tvUserEmail.setText(user.email != null ? user.email : "");
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    binding.tvAvatarInitial.setText(
                            String.valueOf(user.getDisplayName().charAt(0)).toUpperCase());
                }
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    binding.ivAvatar.setVisibility(View.VISIBLE);
                    binding.tvAvatarInitial.setVisibility(View.GONE);
                    com.bumptech.glide.Glide.with(requireContext())
                            .load(com.example.btck.utils.ImageUtils.getFullImageUrl(user.avatarUrl))
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(binding.ivAvatar);
                } else {
                    binding.ivAvatar.setVisibility(View.GONE);
                    binding.tvAvatarInitial.setVisibility(View.VISIBLE);
                }
            }
        });
        authViewModel.fetchCurrentUser();
    }

    private void setupClickListeners() {
        binding.layoutEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.layoutBankInfo.setOnClickListener(v -> showBankInfoDialog());
        binding.layoutAbout.setOnClickListener(v -> showAboutDialog());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(com.example.btck.R.layout.dialog_edit_profile, null);
        android.widget.EditText etName = dialogView.findViewById(com.example.btck.R.id.etFullName);
        android.widget.ImageView ivDialogAvatar = dialogView.findViewById(com.example.btck.R.id.ivDialogAvatar);
        android.widget.TextView tvDialogInitial = dialogView.findViewById(com.example.btck.R.id.tvDialogAvatarInitial);
        com.google.android.material.card.MaterialCardView btnDialogCamera =
                dialogView.findViewById(com.example.btck.R.id.btnDialogEditAvatar);
        com.google.android.material.card.MaterialCardView cardDialogAvatar =
                dialogView.findViewById(com.example.btck.R.id.cardDialogAvatar);

        activeDialogAvatar = ivDialogAvatar;
        activeDialogAvatarInitial = tvDialogInitial;

        if (currentUser != null) {
            etName.setText(currentUser.getDisplayName());
            if (currentUser.avatarUrl != null && !currentUser.avatarUrl.isEmpty()) {
                ivDialogAvatar.setVisibility(View.VISIBLE);
                tvDialogInitial.setVisibility(View.GONE);
                com.bumptech.glide.Glide.with(requireContext())
                        .load(com.example.btck.utils.ImageUtils.getFullImageUrl(currentUser.avatarUrl))
                        .circleCrop()
                        .into(ivDialogAvatar);
            } else if (currentUser.getDisplayName() != null) {
                tvDialogInitial.setText(
                        String.valueOf(currentUser.getDisplayName().charAt(0)).toUpperCase());
            }
        }

        android.view.View.OnClickListener pickPhoto = v -> {
            Toast.makeText(requireContext(), "Đã chạm nút chọn ảnh!", Toast.LENGTH_SHORT).show();
            showImageSourceOptions(v);
        };
        btnDialogCamera.setOnClickListener(pickPhoto);
        cardDialogAvatar.setOnClickListener(pickPhoto);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chỉnh sửa hồ sơ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        UpdateUserMeRequest req = new UpdateUserMeRequest();
                        req.fullName = newName;
                        userRepository.updateMe(req, new UserRepository.UserCallback() {
                            @Override
                            public void onSuccess(UserPublic user) {
                                if (!isAdded()) return;
                                requireActivity().runOnUiThread(() -> {
                                    tokenManager.saveUserInfo(tokenManager.getUserId(), tokenManager.getUserEmail(), newName);
                                    loadUserInfo();
                                    Toast.makeText(requireContext(), "Đã cập nhật hồ sơ!", Toast.LENGTH_SHORT).show();
                                });
                            }
                            @Override
                            public void onError(String message) {
                                if (!isAdded()) return;
                                requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showAvatarImage(String url) {
        binding.ivAvatar.setVisibility(View.VISIBLE);
        binding.tvAvatarInitial.setVisibility(View.GONE);
        com.bumptech.glide.Glide.with(requireContext())
                .load(com.example.btck.utils.ImageUtils.getFullImageUrl(url))
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    private void showImageSourceOptions(android.view.View anchorView) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("📷 Chọn nguồn ảnh đại diện")
                .setItems(new String[]{"Dùng Camera", "Chọn từ Thư viện"}, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
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

    private void startCameraCapture() {
        Toast.makeText(requireContext(), "Đang mở máy ảnh...", Toast.LENGTH_SHORT).show();
        cameraImageUri = createCameraImageUri();
        if (cameraImageUri != null) {
            takePictureLauncher.launch(cameraImageUri);
        }
    }

    private android.net.Uri createCameraImageUri() {
        try {
            java.io.File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                storageDir = new java.io.File(requireContext().getFilesDir(), "Pictures");
            }
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            java.io.File imageFile = java.io.File.createTempFile(imageFileName, ".jpg", storageDir);

            return androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    imageFile
            );
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không tạo được file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void uploadAvatar(android.net.Uri imageUri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            String mimeType = requireContext().getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            String ext = mimeType.contains("png") ? ".png" : ".jpg";

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(bytes, okhttp3.MediaType.parse(mimeType));
            okhttp3.MultipartBody.Part part = okhttp3.MultipartBody.Part.createFormData("file", "avatar" + ext, requestBody);

            binding.ivAvatar.setVisibility(android.view.View.VISIBLE);
            binding.tvAvatarInitial.setVisibility(android.view.View.GONE);
            com.bumptech.glide.Glide.with(requireContext()).load(imageUri).circleCrop().into(binding.ivAvatar);

            if (activeDialogAvatar != null && activeDialogAvatarInitial != null) {
                activeDialogAvatar.setVisibility(android.view.View.VISIBLE);
                activeDialogAvatarInitial.setVisibility(android.view.View.GONE);
                com.bumptech.glide.Glide.with(requireContext()).load(imageUri).circleCrop().into(activeDialogAvatar);
            }

            Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            com.example.btck.api.RetrofitClient.getApiService()
                    .uploadAvatar(part)
                    .enqueue(new retrofit2.Callback<UserPublic>() {
                        @Override
                        public void onResponse(@androidx.annotation.NonNull retrofit2.Call<UserPublic> call,
                                               @androidx.annotation.NonNull retrofit2.Response<UserPublic> response) {
                            if (!isAdded() || getActivity() == null) return;
                            if (response.isSuccessful() && response.body() != null) {
                                currentUser = response.body();
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(),
                                            "✅ Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                                    if (currentUser.avatarUrl != null) {
                                        showAvatarImage(currentUser.avatarUrl);
                                        if (activeDialogAvatar != null && activeDialogAvatarInitial != null) {
                                            com.bumptech.glide.Glide.with(requireContext())
                                                    .load(com.example.btck.utils.ImageUtils.getFullImageUrl(currentUser.avatarUrl))
                                                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .circleCrop()
                                                    .into(activeDialogAvatar);
                                        }
                                    }
                                });
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(),
                                                "Tải ảnh thất bại. Thử lại sau.", Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onFailure(@androidx.annotation.NonNull retrofit2.Call<UserPublic> call, @androidx.annotation.NonNull Throwable t) {
                            if (!isAdded() || getActivity() == null) return;
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(),
                                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(com.example.btck.R.layout.dialog_change_password, null);
        android.widget.EditText etCurrent = dialogView.findViewById(com.example.btck.R.id.etCurrentPassword);
        android.widget.EditText etNew = dialogView.findViewById(com.example.btck.R.id.etNewPassword);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Đổi", (dialog, which) -> {
                    String current = etCurrent.getText().toString().trim();
                    String newPass = etNew.getText().toString().trim();
                    if (TextUtils.isEmpty(current) || TextUtils.isEmpty(newPass)) {
                        Toast.makeText(requireContext(), "Vui lòng điền đầy đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPass.length() < 8) {
                        Toast.makeText(requireContext(), "Mật khẩu mới tối thiểu 8 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UpdatePasswordRequest req = new UpdatePasswordRequest(current, newPass);
                    userRepository.updatePassword(req, new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Đã đổi mật khẩu!", Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override
                        public void onError(String message) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showBankInfoDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(com.example.btck.R.layout.dialog_bank_info, null);
        android.widget.AutoCompleteTextView spinnerBank  = dialogView.findViewById(com.example.btck.R.id.spinnerBank);
        com.google.android.material.textfield.TextInputEditText etAccountNumber = dialogView.findViewById(com.example.btck.R.id.etAccountNumber);
        com.google.android.material.textfield.TextInputEditText etAccountHolder = dialogView.findViewById(com.example.btck.R.id.etAccountHolder);

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
            com.example.btck.api.RetrofitClient.getApiService().getBanks().enqueue(new retrofit2.Callback<com.example.btck.models.BanksResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<com.example.btck.models.BanksResponse> call,
                                       @NonNull retrofit2.Response<com.example.btck.models.BanksResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().data != null) {
                        bankList = response.body().data;
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> populateBankDropdown(spinnerBank));
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull retrofit2.Call<com.example.btck.models.BanksResponse> call, @NonNull Throwable t) {
                    // Nếu API ngân hàng lỗi, người dùng vẫn có thể gõ tay
                }
            });
        } else {
            populateBankDropdown(spinnerBank);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("🏦 Thông tin ngân hàng")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
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
                        Toast.makeText(requireContext(), "Vui lòng nhập số tài khoản", Toast.LENGTH_SHORT).show();
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
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "✅ Đã lưu thông tin ngân hàng!", Toast.LENGTH_SHORT).show());
                            }
                        }
                        @Override
                        public void onError(String message) {
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void populateBankDropdown(android.widget.AutoCompleteTextView spinner) {
        BankAdapter adapter = new BankAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bankList);
        spinner.setAdapter(adapter);

        spinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedString = (String) parent.getItemAtPosition(position);
            for (com.example.btck.models.BankInfo b : bankList) {
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

    private static class BankAdapter extends android.widget.ArrayAdapter<String> {
        private final java.util.List<com.example.btck.models.BankInfo> banks;
        private final java.util.List<String> allItems;
        private final android.widget.Filter customFilter = new android.widget.Filter() {
            @Override
            protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                android.widget.Filter.FilterResults results = new android.widget.Filter.FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = allItems;
                    results.count = allItems.size();
                } else {
                    String query = removeAccent(constraint.toString().toLowerCase().trim());
                    java.util.List<String> matches = new java.util.ArrayList<>();
                    for (com.example.btck.models.BankInfo b : banks) {
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
                    addAll((java.util.List<String>) results.values);
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

        public BankAdapter(android.content.Context context, int resource, java.util.List<com.example.btck.models.BankInfo> banks) {
            super(context, resource, new java.util.ArrayList<>());
            this.banks = banks;
            this.allItems = new java.util.ArrayList<>();
            for (com.example.btck.models.BankInfo b : banks) {
                this.allItems.add(b.getDisplayName() + " (" + b.bin + ")");
            }
            addAll(allItems);
        }

        @androidx.annotation.NonNull
        @Override
        public android.widget.Filter getFilter() {
            return customFilter;
        }
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Về SplitMate")
                .setMessage("Phiên bản: 1.0.0\nỨng dụng chia tiền thông minh\n\nPhát triển bởi BTCK Team")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    authViewModel.logout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
