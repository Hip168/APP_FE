package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// ============ AUTH ============
public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("token_type")
    public String tokenType;
}
