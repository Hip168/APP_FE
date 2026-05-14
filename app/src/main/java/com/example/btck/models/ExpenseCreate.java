package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExpenseCreate {
    @SerializedName("description")
    public String description;
    @SerializedName("amount")
    public long amount;
    @SerializedName("category")
    public String category;
    @SerializedName("expense_date")
    public String expenseDate;
    @SerializedName("payer_id")
    public String payerId;
    @SerializedName("splits")
    public List<ExpenseSplitCreate> splits;
}
