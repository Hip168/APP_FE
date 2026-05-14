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
        Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
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
