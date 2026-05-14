package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SettlementsPublic {
    @SerializedName("data")
    public List<SettlementPublic> data;
    @SerializedName("count")
    public int count;
}
