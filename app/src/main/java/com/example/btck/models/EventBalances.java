package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventBalances {
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("event_name")
    public String eventName;
    @SerializedName("balances")
    public List<UserBalance> balances;
}
