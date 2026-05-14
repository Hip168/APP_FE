package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class PaymentQrResponse {
    @SerializedName("qr_url")
    public String qrUrl;

    @SerializedName("amount")
    public long amount;

    @SerializedName("bank_name")
    public String bankName;

    @SerializedName("account_number")
    public String accountNumber;

    @SerializedName("account_holder")
    public String accountHolder;
}
