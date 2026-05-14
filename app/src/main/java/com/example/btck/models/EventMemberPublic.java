package com.example.btck.models;

import com.google.gson.annotations.SerializedName;

public class EventMemberPublic {
    @SerializedName("event_id")
    public String eventId;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("joined_at")
    public String joinedAt;
    @SerializedName("user_email")
    public String userEmail;
    @SerializedName("user_full_name")
    public String userFullName;
    @SerializedName("role")
    public String role;

    public String getDisplayName() {
        if (userFullName != null && !userFullName.isEmpty()) return userFullName;
        if (userEmail != null && userEmail.contains("@")) return userEmail.split("@")[0];
        return userEmail != null ? userEmail : "Unknown";
    }

    public String getInitials() {
        String name = getDisplayName();
        if (name.length() >= 2) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0)).toUpperCase() +
                       String.valueOf(parts[parts.length-1].charAt(0)).toUpperCase();
            }
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
        return "?";
    }
}
