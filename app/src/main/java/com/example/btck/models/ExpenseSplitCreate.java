package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class ExpenseSplitCreate {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("amount_owed")
    public long amountOwed;

    public ExpenseSplitCreate(String userId, long amountOwed) {
        this.userId = userId;
        this.amountOwed = amountOwed;
    }
}
