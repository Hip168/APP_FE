package com.example.btck.models;
import com.google.gson.annotations.SerializedName;

public class NewPasswordRequest {
    @SerializedName("token")
    public String token;
    @SerializedName("new_password")
    public String newPassword;
    public NewPasswordRequest(String token, String newPassword) {
        this.token = token; this.newPassword = newPassword;
    }
}
