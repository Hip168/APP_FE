package com.example.btck.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.models.*;
import com.example.btck.repository.SettlementRepository;
import java.util.UUID;

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
    }

    public void createSettlement(String eventId, String fromUserId, String toUserId, long amount, String note) {
        if (amount <= 0 || amount > Integer.MAX_VALUE) {
            errorMessage.setValue("Số tiền thanh toán không hợp lệ. Vui lòng tải lại hoặc thử nhóm khác.");
            isLoading.setValue(false);
            return;
        }
        isLoading.setValue(true);
        String idempotencyKey = UUID.randomUUID().toString();
        repository.createSettlement(eventId, new SettlementCreate(fromUserId, toUserId, amount, note, idempotencyKey), createdSettlement, errorMessage);
    }
}
