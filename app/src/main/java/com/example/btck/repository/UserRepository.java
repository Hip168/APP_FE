package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final ApiService api;

    /** Constructor dùng cho ViewModel (không cần Context khi RetrofitClient đã init) */
    public UserRepository() { api = RetrofitClient.getApiService(); }

    /** Constructor dùng cho Activity (cần Context để lấy token) */
    public UserRepository(android.content.Context context) {
        api = RetrofitClient.getApiService();
    }

    // ============ Callback interfaces ============
    public interface UserCallback {
        void onSuccess(UserPublic user);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    // ============ Callback-based methods (dùng trong Activity) ============
    public void updateMe(UpdateUserMeRequest body, UserCallback callback) {
        api.updateMe(body).enqueue(new Callback<UserPublic>() {
            @Override public void onResponse(Call<UserPublic> call, Response<UserPublic> response) {
                if (response.isSuccessful() && response.body() != null)
                    callback.onSuccess(response.body());
                else callback.onError(parseError(response));
            }
            @Override public void onFailure(Call<UserPublic> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void updatePassword(UpdatePasswordRequest body, SimpleCallback callback) {
        api.updatePassword(body).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) callback.onSuccess();
                else callback.onError(parseError(response));
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ============ LiveData-based methods (dùng trong ViewModel) ============
    public void updateMe(UpdateUserMeRequest body,
                         MutableLiveData<UserPublic> onSuccess, MutableLiveData<String> onError) {
        api.updateMe(body).enqueue(new Callback<UserPublic>() {
            @Override public void onResponse(Call<UserPublic> call, Response<UserPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<UserPublic> call, Throwable t) { onError.postValue("Lỗi kết nối: " + t.getMessage()); }
        });
    }

    public void updatePassword(UpdatePasswordRequest body,
                               MutableLiveData<Boolean> onSuccess, MutableLiveData<String> onError) {
        api.updatePassword(body).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) onSuccess.postValue(true);
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) { onError.postValue("Lỗi kết nối: " + t.getMessage()); }
        });
    }

    public void searchUsers(String email,
                            MutableLiveData<List<UserPublic>> onSuccess, MutableLiveData<String> onError) {
        api.searchUsers(email).enqueue(new Callback<List<UserPublic>>() {
            @Override public void onResponse(Call<List<UserPublic>> call, Response<List<UserPublic>> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<List<UserPublic>> call, Throwable t) { onError.postValue("Lỗi kết nối: " + t.getMessage()); }
        });
    }

    public void registerFcmToken(String fcmToken, MutableLiveData<Boolean> onSuccess, MutableLiveData<String> onError) {
        api.registerFcmToken(new FCMTokenRequest(fcmToken, "android")).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) onSuccess.postValue(true);
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) { onError.postValue("Lỗi kết nối: " + t.getMessage()); }
        });
    }

    private String parseError(Response<?> response) {
        try {
            String body = response.errorBody() != null ? response.errorBody().string() : "";
            if (body.contains("\"detail\":\"")) {
                int s = body.indexOf("\"detail\":\"") + 10;
                int e = body.indexOf("\"", s);
                if (e > s) return body.substring(s, e);
            }
            return "Lỗi: " + response.code();
        } catch (Exception e) { return "Lỗi không xác định"; }
    }
}
