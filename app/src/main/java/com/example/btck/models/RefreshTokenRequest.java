package com.example.btck.models;
import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {
    @SerializedName("refresh_token")
    public String refreshToken;
    public RefreshTokenRequest(String refreshToken) { this.refreshToken = refreshToken; }
}
