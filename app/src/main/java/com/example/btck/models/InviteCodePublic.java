package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class InviteCodePublic {
    @SerializedName("code")
    public String code;
    @SerializedName("expires_at")
    public String expiresAt;
    @SerializedName("invite_url")
    public String inviteUrl;
    @SerializedName("created_at")
    public String createdAt;
}
