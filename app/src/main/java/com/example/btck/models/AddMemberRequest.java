package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class AddMemberRequest {
    @SerializedName("email")
    public String email;
    public AddMemberRequest(String email) { this.email = email; }
}
