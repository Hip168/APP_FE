package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import com.example.btck.utils.ErrorUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private final ApiService api;

    public EventRepository() {
        api = RetrofitClient.getApiService();
    }

    public void getEvents(int skip, int limit, String query,
                          MutableLiveData<EventsPublic> onSuccess,
                          MutableLiveData<String> onError) {
        api.getEvents(skip, limit, query).enqueue(new Callback<EventsPublic>() {
            @Override
            public void onResponse(Call<EventsPublic> call, Response<EventsPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventsPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void createEvent(String name, String description,
                            MutableLiveData<EventPublic> onSuccess,
                            MutableLiveData<String> onError) {
        api.createEvent(new EventCreate(name, description)).enqueue(new Callback<EventPublic>() {
            @Override
            public void onResponse(Call<EventPublic> call, Response<EventPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void getEvent(String eventId,
                         MutableLiveData<EventPublic> onSuccess,
                         MutableLiveData<String> onError) {
        api.getEvent(eventId).enqueue(new Callback<EventPublic>() {
            @Override
            public void onResponse(Call<EventPublic> call, Response<EventPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void deleteEvent(String eventId,
                            MutableLiveData<Boolean> onSuccess,
                            MutableLiveData<String> onError) {
        api.deleteEvent(eventId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) onSuccess.postValue(true);
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void addMember(String eventId, String email,
                          MutableLiveData<EventMemberPublic> onSuccess,
                          MutableLiveData<String> onError) {
        api.addMember(eventId, new AddMemberRequest(email)).enqueue(new Callback<EventMemberPublic>() {
            @Override
            public void onResponse(Call<EventMemberPublic> call, Response<EventMemberPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventMemberPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void getEventBalances(String eventId,
                                 MutableLiveData<EventBalances> onSuccess,
                                 MutableLiveData<String> onError) {
        api.getEventBalances(eventId).enqueue(new Callback<EventBalances>() {
            @Override
            public void onResponse(Call<EventBalances> call, Response<EventBalances> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventBalances> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void getSimplifiedDebts(String eventId,
                                   MutableLiveData<SimplifiedDebtsResponse> onSuccess,
                                   MutableLiveData<String> onError) {
        api.getSimplifiedDebts(eventId).enqueue(new Callback<SimplifiedDebtsResponse>() {
            @Override
            public void onResponse(Call<SimplifiedDebtsResponse> call, Response<SimplifiedDebtsResponse> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<SimplifiedDebtsResponse> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void getEventStats(String eventId,
                              MutableLiveData<EventStats> onSuccess,
                              MutableLiveData<String> onError) {
        api.getEventStats(eventId).enqueue(new Callback<EventStats>() {
            @Override
            public void onResponse(Call<EventStats> call, Response<EventStats> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventStats> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void getMyBalance(MutableLiveData<MyBalanceDetail> onSuccess, MutableLiveData<String> onError) {
        api.getMyBalance().enqueue(new Callback<MyBalanceDetail>() {
            @Override
            public void onResponse(Call<MyBalanceDetail> call, Response<MyBalanceDetail> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<MyBalanceDetail> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void createInviteCode(String eventId,
                                 MutableLiveData<InviteCodePublic> onSuccess,
                                 MutableLiveData<String> onError) {
        api.createInviteCode(eventId, new InviteCodeCreate(24, null)).enqueue(new Callback<InviteCodePublic>() {
            @Override
            public void onResponse(Call<InviteCodePublic> call, Response<InviteCodePublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<InviteCodePublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void joinEvent(String code,
                          MutableLiveData<EventMemberPublic> onSuccess,
                          MutableLiveData<String> onError) {
        api.joinEventByCode(code).enqueue(new Callback<EventMemberPublic>() {
            @Override
            public void onResponse(Call<EventMemberPublic> call, Response<EventMemberPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<EventMemberPublic> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }

    public void removeMember(String eventId, String userId,
                             MutableLiveData<Boolean> onSuccess,
                             MutableLiveData<String> onError) {
        api.removeMember(eventId, userId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) onSuccess.postValue(true);
                else onError.postValue(ErrorUtils.parseError(response));
            }
            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) { onError.postValue("Không kết nối được máy chủ"); }
        });
    }
}
