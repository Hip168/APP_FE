package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class UserPublic {
    @SerializedName("id")
    public String id;
    @SerializedName("email")
    public String email;
    @SerializedName("full_name")
    public String fullName;
    @SerializedName("is_active")
    public boolean isActive;
    @SerializedName("is_superuser")
    public boolean isSuperuser;
    @SerializedName("bank_name")
    public String bankName;
    @SerializedName("account_number")
    public String accountNumber;
    @SerializedName("account_holder")
    public String accountHolder;
    @SerializedName("avatar_url")
    public String avatarUrl;
    @SerializedName("created_at")
    public String createdAt;

    public String getDisplayName() {
        if (fullName != null && !fullName.isEmpty()) return fullName;
        if (email != null && email.contains("@")) return email.split("@")[0];
        return email != null ? email : "Unknown";
    }

    public String getInitials() {
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0)).toUpperCase() +
                       String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
            }
            return String.valueOf(fullName.charAt(0)).toUpperCase();
        }
        if (email != null && !email.isEmpty()) {
            return String.valueOf(email.charAt(0)).toUpperCase();
        }
        return "?";
    }
}
