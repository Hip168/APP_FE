package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class UserRegister {
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("full_name")
    public String fullName;

    public UserRegister(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
}
