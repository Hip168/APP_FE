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
    @SerializedName("idempotency_key")
    public String idempotencyKey;

    public SettlementCreate(String fromUserId, String toUserId, long amount, String note, String idempotencyKey) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.note = note;
        this.idempotencyKey = idempotencyKey;
    }
}
