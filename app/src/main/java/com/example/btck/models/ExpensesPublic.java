package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExpensesPublic {
    @SerializedName("data")
    public List<ExpensePublic> data;
    @SerializedName("count")
    public int count;
}
