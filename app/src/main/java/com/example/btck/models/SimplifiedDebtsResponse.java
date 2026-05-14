package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SimplifiedDebtsResponse {
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("debts")
    public List<SimplifiedDebt> debts;
}
