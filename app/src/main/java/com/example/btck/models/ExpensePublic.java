package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExpensePublic {
    @SerializedName("id")
    public String id;
    @SerializedName("description")
    public String description;
    @SerializedName("amount")
    public long amount;
    @SerializedName("category")
    public String category;
    @SerializedName("image_url")
    public String imageUrl;
    @SerializedName("expense_date")
    public String expenseDate;
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("created_by_id")
    public String createdById;
    @SerializedName("payer_id")
    public String payerId;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("payer_email")
    public String payerEmail;
    @SerializedName("payer_full_name")
    public String payerFullName;
    @SerializedName("splits")
    public List<ExpenseSplitPublic> splits;

    public String getPayerDisplayName() {
        if (payerFullName != null && !payerFullName.isEmpty()) return payerFullName;
        if (payerEmail != null && payerEmail.contains("@")) return payerEmail.split("@")[0];
        return payerEmail != null ? payerEmail : "Unknown";
    }

    public String getFormattedAmount() {
        return String.format("%,dđ", amount);
    }
}
