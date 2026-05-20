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

        authViewModel.currentUser.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                binding.tvUserName.setText(user.getDisplayName());
                binding.tvUserEmail.setText(user.email != null ? user.email : "");
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    binding.tvAvatarInitial.setText(
                            String.valueOf(user.getDisplayName().charAt(0)).toUpperCase());
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
        etName.setText(tokenManager.getUserName());

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
        java.util.List<String> names = new java.util.ArrayList<>();
        for (com.example.btck.models.BankInfo b : bankList) {
            names.add(b.getDisplayName() + " (" + b.bin + ")");
        }
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, names);
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
