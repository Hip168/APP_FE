package com.example.btck.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.MessageResponse;
import com.example.btck.models.NotificationPublic;
import com.example.btck.models.NotificationsPublic;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationViewModel extends AndroidViewModel {

    public MutableLiveData<NotificationsPublic> notifications = new MutableLiveData<>();
    public MutableLiveData<NotificationPublic>  markedRead    = new MutableLiveData<>();
    public MutableLiveData<Boolean>             markedAllRead = new MutableLiveData<>();
    public MutableLiveData<String>              errorMessage  = new MutableLiveData<>();

    public NotificationViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadNotifications() {
        RetrofitClient.getApiService()
                .getNotifications(0, 50)
                .enqueue(new Callback<NotificationsPublic>() {
                    @Override
                    public void onResponse(@NonNull Call<NotificationsPublic> call,
                                           @NonNull Response<NotificationsPublic> response) {
                        if (response.isSuccessful()) notifications.postValue(response.body());
                        else errorMessage.postValue("Lỗi tải thông báo: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<NotificationsPublic> call, @NonNull Throwable t) {
                        errorMessage.postValue("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    public void markRead(String notificationId) {
        RetrofitClient.getApiService()
                .markNotificationRead(notificationId)
                .enqueue(new Callback<NotificationPublic>() {
                    @Override
                    public void onResponse(@NonNull Call<NotificationPublic> call,
                                           @NonNull Response<NotificationPublic> response) {
                        if (response.isSuccessful()) markedRead.postValue(response.body());
                    }
                    @Override
                    public void onFailure(@NonNull Call<NotificationPublic> call, @NonNull Throwable t) {
                        errorMessage.postValue("Lỗi: " + t.getMessage());
                    }
                });
    }

    public void markAllRead() {
        RetrofitClient.getApiService()
                .markAllRead()
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        markedAllRead.postValue(response.isSuccessful());
                    }
                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        errorMessage.postValue("Lỗi: " + t.getMessage());
                    }
                });
    }

    public void resetMarkedAllRead() {
        markedAllRead.setValue(false);
    }
}
