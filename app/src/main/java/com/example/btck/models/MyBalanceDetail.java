package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyBalanceDetail {
    @SerializedName("events")
    public List<EventBalances> events;
    @SerializedName("summary")
    public MyBalanceSummary summary;
}
