package com.example.btck.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationsPublic {
    @SerializedName("data")
    public List<NotificationPublic> data;
    @SerializedName("count")
    public int count;
}
