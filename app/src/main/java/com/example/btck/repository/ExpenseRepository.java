package com.example.btck.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseRepository {
    private final ApiService api;
    public ExpenseRepository() { api = RetrofitClient.getApiService(); }

    public void getExpenses(String eventId, int skip, int limit,
                            MutableLiveData<ExpensesPublic> onSuccess, MutableLiveData<String> onError) {
        api.getExpenses(eventId, skip, limit).enqueue(new Callback<ExpensesPublic>() {
            @Override public void onResponse(Call<ExpensesPublic> call, Response<ExpensesPublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<ExpensesPublic> call, Throwable t) { onError.postValue("Lỗi mạng"); }
        });
    }

    public void createExpense(String eventId, ExpenseCreate body,
                              MutableLiveData<ExpensePublic> onSuccess, MutableLiveData<String> onError) {
        api.createExpense(eventId, body).enqueue(new Callback<ExpensePublic>() {
            @Override public void onResponse(Call<ExpensePublic> call, Response<ExpensePublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<ExpensePublic> call, Throwable t) { onError.postValue("Lỗi mạng"); }
        });
    }

    public void getExpense(String eventId, String expenseId,
                           MutableLiveData<ExpensePublic> onSuccess, MutableLiveData<String> onError) {
        api.getExpense(eventId, expenseId).enqueue(new Callback<ExpensePublic>() {
            @Override public void onResponse(Call<ExpensePublic> call, Response<ExpensePublic> response) {
                if (response.isSuccessful() && response.body() != null) onSuccess.postValue(response.body());
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<ExpensePublic> call, Throwable t) { onError.postValue("Lỗi mạng"); }
        });
    }

    public void deleteExpense(String eventId, String expenseId,
                              MutableLiveData<Boolean> onSuccess, MutableLiveData<String> onError) {
        api.deleteExpense(eventId, expenseId).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) onSuccess.postValue(true);
                else onError.postValue(parseError(response));
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) { onError.postValue("Lỗi mạng"); }
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
