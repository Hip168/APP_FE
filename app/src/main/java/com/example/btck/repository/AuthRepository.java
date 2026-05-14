package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService api;

    public AuthRepository() {
        api = RetrofitClient.getApiService();
    }

    public void login(String email, String password,
                      MutableLiveData<TokenResponse> onSuccess,
                      MutableLiveData<String> onError) {
        api.login(email, password).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.postValue(response.body());
                } else {
                    onError.postValue(parseError(response));
                }
            }
            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                onError.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void register(String email, String password, String fullName,
                         MutableLiveData<UserPublic> onSuccess,
                         MutableLiveData<String> onError) {
        UserRegister body = new UserRegister(email, password, fullName);
        api.register(body).enqueue(new Callback<UserPublic>() {
            @Override
            public void onResponse(Call<UserPublic> call, Response<UserPublic> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.postValue(response.body());
                } else {
                    onError.postValue(parseError(response));
                }
            }
            @Override
            public void onFailure(Call<UserPublic> call, Throwable t) {
                onError.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void recoverPassword(String email,
                                MutableLiveData<MessageResponse> onSuccess,
                                MutableLiveData<String> onError) {
        api.recoverPassword(email).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.postValue(response.body());
                } else {
                    onError.postValue(parseError(response));
                }
            }
            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                onError.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getCurrentUser(MutableLiveData<UserPublic> onSuccess, MutableLiveData<String> onError) {
        api.getCurrentUser().enqueue(new Callback<UserPublic>() {
            @Override
            public void onResponse(Call<UserPublic> call, Response<UserPublic> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onSuccess.postValue(response.body());
                } else {
                    onError.postValue(parseError(response));
                }
            }
            @Override
            public void onFailure(Call<UserPublic> call, Throwable t) {
                onError.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private String parseError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
            if (errorBody.contains("detail")) {
                // Try to extract detail message
                int start = errorBody.indexOf("\"detail\":\"") + 10;
                if (start > 9) {
                    int end = errorBody.indexOf("\"", start);
                    if (end > start) return errorBody.substring(start, end);
                }
            }
            if (response.code() == 400) return "Email hoặc mật khẩu không đúng";
            if (response.code() == 401) return "Phiên đăng nhập hết hạn";
            if (response.code() == 404) return "Không tìm thấy";
            if (response.code() == 422) return "Dữ liệu không hợp lệ";
            return "Lỗi: " + response.code();
        } catch (Exception e) {
            return "Lỗi không xác định";
        }
    }
}
