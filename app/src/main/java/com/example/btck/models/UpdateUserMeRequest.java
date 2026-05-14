package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class UpdateUserMeRequest {
    @SerializedName("full_name")
    public String fullName;
    @SerializedName("email")
    public String email;
    @SerializedName("bank_name")
    public String bankName;
    @SerializedName("account_number")
    public String accountNumber;
    @SerializedName("account_holder")
    public String accountHolder;
}
