package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class SettlementCreate {
    @SerializedName("from_user_id")
    public String fromUserId;
    @SerializedName("to_user_id")
    public String toUserId;
    @SerializedName("amount")
    public long amount;
    @SerializedName("note")
    public String note;

    public SettlementCreate(String fromUserId, String toUserId, long amount, String note) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.note = note;
    }
}
