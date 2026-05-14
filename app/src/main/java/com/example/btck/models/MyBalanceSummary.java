package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyBalanceSummary {
    @SerializedName("total_you_owe")
    public long totalYouOwe;
    @SerializedName("total_owed_to_you")
    public long totalOwedToYou;
    @SerializedName("net_balance")
    public long netBalance;
}
