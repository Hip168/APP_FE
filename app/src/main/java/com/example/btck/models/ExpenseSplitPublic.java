package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class ExpenseSplitPublic {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("amount_owed")
    public long amountOwed;
    @SerializedName("user_email")
    public String userEmail;
    @SerializedName("user_full_name")
    public String userFullName;

    public String getDisplayName() {
        if (userFullName != null && !userFullName.isEmpty()) return userFullName;
        if (userEmail != null && userEmail.contains("@")) return userEmail.split("@")[0];
        return userEmail != null ? userEmail : "Unknown";
    }
}
