package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BanksResponse {
    @SerializedName("data")
    public List<BankInfo> data;
}
