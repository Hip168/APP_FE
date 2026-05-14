package com.example.btck.models;
import com.google.gson.annotations.SerializedName;

public class FCMTokenRequest {
    @SerializedName("fcm_token")
    public String fcmToken;
    @SerializedName("device_type")
    public String deviceType;
    public FCMTokenRequest(String fcmToken, String deviceType) {
        this.fcmToken = fcmToken; this.deviceType = deviceType;
    }
}
