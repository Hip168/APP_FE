package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserBalance {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_email")
    public String userEmail;
    @SerializedName("user_full_name")
    public String userFullName;
    @SerializedName("bank_name")
    public String bankName;
    @SerializedName("account_number")
    public String accountNumber;
    @SerializedName("account_holder")
    public String accountHolder;
    @SerializedName("total_paid")
    public long totalPaid;
    @SerializedName("total_owed")
    public long totalOwed;
    @SerializedName("net_balance")
    public long netBalance;

    public String getDisplayName() {
        if (userFullName != null && !userFullName.isEmpty()) return userFullName;
        if (userEmail != null && userEmail.contains("@")) return userEmail.split("@")[0];
        return userEmail != null ? userEmail : "Unknown";
    }
}
