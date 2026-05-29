package com.example.btck;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.example.btck.api.RetrofitClient;

public class BTCKApplication extends Application {

    private static BTCKApplication instance;
    private static final String CHANNEL_ID = "btck_notifications";
    private static final String CHANNEL_NAME = "BTCK Thông báo";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Khởi tạo RetrofitClient khi app start
        RetrofitClient.init(this);
        createNotificationChannel();
    }

    public static BTCKApplication getInstance() {
        return instance;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // 1. Kênh chính
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Thông báo chi tiêu và nhóm");
                manager.createNotificationChannel(channel);

                // 2. Kênh dự phòng mặc định của Firebase (Workaround cho Backend đã hosted)
                NotificationChannel fallbackChannel = new NotificationChannel(
                        "fcm_fallback_notification_channel",
                        "Thông báo ứng dụng",
                        NotificationManager.IMPORTANCE_HIGH
                );
                fallbackChannel.setDescription("Kênh thông báo mặc định của hệ thống");
                manager.createNotificationChannel(fallbackChannel);
            }
        }
    }
}
