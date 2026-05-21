package com.example.btck.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.btck.R;
import com.example.btck.databinding.ActivityMainBinding;
import com.example.btck.fragments.HomeFragment;
import com.example.btck.fragments.GroupsFragment;
import com.example.btck.fragments.NotificationsFragment;
import com.example.btck.fragments.ProfileFragment;
import com.example.btck.api.RetrofitClient;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.FCMTokenRequest;
import com.example.btck.models.MessageResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        setupFcm();

        // Show home by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupFcm() {
        // Request notifications permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Get FCM Token and register on server
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("MainActivity", "Lấy FCM Token thất bại", task.getException());
                return;
            }

            String token = task.getResult();
            Log.d("MainActivity", "FCM Token hiện tại: " + token);

            TokenManager tokenManager = new TokenManager(this);
            tokenManager.saveFcmToken(token);

            if (tokenManager.isLoggedIn()) {
                sendTokenToServer(token);
            }
        });
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
                            Log.d("MainActivity", "FCM Token đã gửi lên server thành công");
                        } else {
                            Log.e("MainActivity", "Gửi FCM Token thất bại: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        Log.e("MainActivity", "Lỗi mạng");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    public void updateNotificationBadge() {
        TokenManager tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            return;
        }

        RetrofitClient.getApiService().getUnreadCount().enqueue(new Callback<com.example.btck.models.UnreadCountResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.btck.models.UnreadCountResponse> call,
                                   @NonNull Response<com.example.btck.models.UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().count;
                    com.google.android.material.badge.BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_notifications);
                    if (count > 0) {
                        badge.setVisible(true);
                        badge.setMaxNumber(10);
                        badge.setNumber(count);
                    } else {
                        badge.setVisible(false);
                        binding.bottomNav.removeBadge(R.id.nav_notifications);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.btck.models.UnreadCountResponse> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Lỗi mạng");
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_groups) {
                loadFragment(new GroupsFragment());
                return true;
            } else if (id == R.id.nav_notifications) {
                loadFragment(new NotificationsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateToGroupDetail(String eventId, String eventName) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_name", eventName);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
