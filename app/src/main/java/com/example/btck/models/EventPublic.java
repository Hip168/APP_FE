package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventPublic {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("created_by_id")
    public String createdById;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("member_count")
    public int memberCount;
    @SerializedName("expense_count")
    public int expenseCount;
}
