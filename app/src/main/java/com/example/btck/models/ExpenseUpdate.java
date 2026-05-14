package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class ExpenseUpdate {
    @SerializedName("description")
    public String description;
    @SerializedName("amount")
    public Long amount;
    @SerializedName("category")
    public String category;
    @SerializedName("expense_date")
    public String expenseDate;
}
