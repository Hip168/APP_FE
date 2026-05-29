package com.example.btck.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.btck.R;
import com.example.btck.activities.MainActivity;
import com.example.btck.activities.GroupDetailActivity;
import com.example.btck.api.RetrofitClient;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.FCMTokenRequest;
import com.example.btck.models.MessageResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "btck_notifications";
    private static final String CHANNEL_NAME = "BTCK Thông báo";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token mới: " + token);

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        tokenManager.saveFcmToken(token);

        // Nếu đã đăng nhập, gửi token lên server
        if (tokenManager.isLoggedIn()) {
            sendTokenToServer(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Nhận tin nhắn từ: " + remoteMessage.getFrom());

        String title = "SplitMate";
        String body = "Bạn có thông báo mới";
        String type = null;
        String eventId = null;

        // Lấy notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        }

        // Lấy data payload
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            type    = data.get("type");
            eventId = data.get("event_id");
            if (data.containsKey("title"))   title = data.get("title");
            if (data.containsKey("message")) body  = data.get("message");
        }

        showNotification(title, body, type, eventId);
    }

    private void showNotification(String title, String body, String type, String eventId) {
        Intent intent;

        // Điều hướng thông minh dựa vào type
        if (eventId != null && (type == null || type.contains("expense") || type.contains("settlement"))) {
            intent = new Intent(this, GroupDetailActivity.class);
            intent.putExtra("event_id", eventId);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo channel cho Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo chi tiêu và nhóm");
            manager.createNotificationChannel(channel);
        }

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void sendTokenToServer(String fcmToken) {
        FCMTokenRequest request = new FCMTokenRequest(fcmToken, "android");
        RetrofitClient.getApiService()
                .registerFcmToken(request)
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "FCM token đã gửi lên server thành công");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Không kết nối được máy chủ");
                    }
                });
    }
}
