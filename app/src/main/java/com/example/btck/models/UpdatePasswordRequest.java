package com.example.btck.models;
import com.google.gson.annotations.SerializedName;

public class UpdatePasswordRequest {
    @SerializedName("current_password")
    public String currentPassword;
    @SerializedName("new_password")
    public String newPassword;
    public UpdatePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword; this.newPassword = newPassword;
    }
}
