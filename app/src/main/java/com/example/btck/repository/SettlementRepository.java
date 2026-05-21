package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettlementRepository {
    private final ApiService api;
    public SettlementRepository() { api = RetrofitClient.getApiService(); }

    public void getSettlements(String eventId, int skip, int limit,
                               MutableLiveData<SettlementsPublic> onSuccess, MutableLiveData<String> onError) {
        api.getSettlements(eventId, skip, limit).enqueue(new Callback<SettlementsPublic>() {
            @Override public void onResponse(Call<SettlementsPublic> call, Response<SettlementsPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<SettlementsPublic> call, Throwable t) { onError.postValue("Lỗi mạng"); }
        });
    }

    public void createSettlement(String eventId, SettlementCreate body,
                                 MutableLiveData<SettlementPublic> onSuccess, MutableLiveData<String> onError) {
        api.createSettlement(eventId, body).enqueue(new Callback<SettlementPublic>() {
            @Override public void onResponse(Call<SettlementPublic> call, Response<SettlementPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<SettlementPublic> call, Throwable t) { onError.postValue("Lỗi mạng"); }
        });
    }

    private String parseError(Response<?> response) {
        try {
            String body = response.errorBody() != null ? response.errorBody().string() : "";
            if (body.contains("\"detail\":\"")) { int s = body.indexOf("\"detail\":\"") + 10; int e = body.indexOf("\"", s); if (e > s) return body.substring(s, e); }
            return "Lỗi: " + response.code();
        } catch (Exception e) { return "Lỗi không xác định"; }
    }
}
