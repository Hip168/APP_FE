package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import com.example.btck.utils.ErrorUtils;
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
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override public void onFailure(Call<SettlementsPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void createSettlement(String eventId, SettlementCreate body,
                                 MutableLiveData<SettlementPublic> onSuccess, MutableLiveData<String> onError) {
        api.createSettlement(eventId, body).enqueue(new Callback<SettlementPublic>() {
            @Override public void onResponse(Call<SettlementPublic> call, Response<SettlementPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override public void onFailure(Call<SettlementPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }
}
