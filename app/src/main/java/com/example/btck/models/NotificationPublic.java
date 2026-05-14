package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class NotificationPublic {
    @SerializedName("id")
    public String id;
    @SerializedName("title")
    public String title;
    @SerializedName("content")
    public String content;
    @SerializedName("type")
    public String type;
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("reference_id")
    public String referenceId;
    @SerializedName("sender_id")
    public String senderId;
    @SerializedName("is_read")
    public boolean isRead;
    @SerializedName("created_at")
    public String createdAt;
}
