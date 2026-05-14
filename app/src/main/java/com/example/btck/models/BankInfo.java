package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class BankInfo {
    @SerializedName("bin")
    public String bin;

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

    @SerializedName("shortName")
    public String shortName;

    @SerializedName("logo")
    public String logo;

    @SerializedName("transferSupported")
    public int transferSupported;

    /** Tên hiển thị: ưu tiên shortName, fallback về code */
    public String getDisplayName() {
        if (shortName != null && !shortName.isEmpty()) return shortName;
        if (code != null && !code.isEmpty()) return code;
        return name != null ? name : "";
    }
}
