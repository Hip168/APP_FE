package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class SimplifiedDebt {
    @SerializedName("from_user_id")
    public String fromUserId;
    @SerializedName("from_user_email")
    public String fromUserEmail;
    @SerializedName("from_user_full_name")
    public String fromUserFullName;
    @SerializedName("to_user_id")
    public String toUserId;
    @SerializedName("to_user_email")
    public String toUserEmail;
    @SerializedName("to_user_full")
    public String toUserFull;
    @SerializedName("amount")
    public long amount;

    public String getFromDisplayName() {
        if (fromUserFullName != null && !fromUserFullName.isEmpty()) return fromUserFullName;
        if (fromUserEmail != null) return fromUserEmail.split("@")[0];
        return "Unknown";
    }

    public String getToDisplayName() {
        if (toUserFull != null && !toUserFull.isEmpty()) return toUserFull;
        if (toUserEmail != null) return toUserEmail.split("@")[0];
        return "Unknown";
    }
}
