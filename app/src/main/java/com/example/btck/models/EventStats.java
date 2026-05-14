package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class EventStats {
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("total_spent")
    public long totalSpent;
    @SerializedName("expense_count")
    public int expenseCount;
    @SerializedName("member_count")
    public int memberCount;
    @SerializedName("your_total_paid")
    public long yourTotalPaid;
    @SerializedName("your_total_owed")
    public long yourTotalOwed;
    @SerializedName("your_net_balance")
    public long yourNetBalance;
}
