package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class InviteCodeCreate {
    @SerializedName("expires_in_hours")
    public Integer expiresInHours;
    @SerializedName("max_uses")
    public Integer maxUses;

    public InviteCodeCreate(Integer expiresInHours, Integer maxUses) {
        this.expiresInHours = expiresInHours;
        this.maxUses = maxUses;
    }
}
