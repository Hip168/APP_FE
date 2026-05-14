package com.example.btck.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.models.*;
import com.example.btck.repository.SettlementRepository;

public class SettlementViewModel extends AndroidViewModel {

    private final SettlementRepository repository;

    public final MutableLiveData<SettlementsPublic> settlements = new MutableLiveData<>();
    public final MutableLiveData<SettlementPublic> createdSettlement = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public SettlementViewModel(Application application) {
        super(application);
        repository = new SettlementRepository();
    }

    public void loadSettlements(String eventId) {
        isLoading.setValue(true);
        repository.getSettlements(eventId, 0, 100, settlements, errorMessage);
        settlements.observeForever(s -> isLoading.setValue(false));
    }

    public void createSettlement(String eventId, String fromUserId, String toUserId, long amount, String note) {
        isLoading.setValue(true);
        repository.createSettlement(eventId, new SettlementCreate(fromUserId, toUserId, amount, note), createdSettlement, errorMessage);
    }
}
