package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class SettlementPublic {
    @SerializedName("id")
    public String id;
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("from_user_id")
    public String fromUserId;
    @SerializedName("to_user_id")
    public String toUserId;
    @SerializedName("amount")
    public long amount;
    @SerializedName("note")
    public String note;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("from_user_email")
    public String fromUserEmail;
    @SerializedName("from_user_full_name")
    public String fromUserFullName;
    @SerializedName("to_user_email")
    public String toUserEmail;
    @SerializedName("to_user_full_name")
    public String toUserFullName;

    public String getFromDisplayName() {
        if (fromUserFullName != null && !fromUserFullName.isEmpty()) return fromUserFullName;
        if (fromUserEmail != null && fromUserEmail.contains("@")) return fromUserEmail.split("@")[0];
        return fromUserEmail != null ? fromUserEmail : "Unknown";
    }

    public String getToDisplayName() {
        if (toUserFullName != null && !toUserFullName.isEmpty()) return toUserFullName;
        if (toUserEmail != null && toUserEmail.contains("@")) return toUserEmail.split("@")[0];
        return toUserEmail != null ? toUserEmail : "Unknown";
    }

    public String getFormattedAmount() {
        return String.format("%,dđ", amount);
    }
}
