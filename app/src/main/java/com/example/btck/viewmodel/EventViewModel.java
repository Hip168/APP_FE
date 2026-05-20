package com.example.btck.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.models.*;
import com.example.btck.repository.EventRepository;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository repository;

    public final MutableLiveData<EventsPublic> events = new MutableLiveData<>();
    public final MutableLiveData<EventPublic> selectedEvent = new MutableLiveData<>();
    public final MutableLiveData<EventPublic> createdEvent = new MutableLiveData<>();
    public final MutableLiveData<EventMemberPublic> addedMember = new MutableLiveData<>();
    public final MutableLiveData<EventBalances> eventBalances = new MutableLiveData<>();
    public final MutableLiveData<SimplifiedDebtsResponse> simplifiedDebts = new MutableLiveData<>();
    public final MutableLiveData<EventStats> eventStats = new MutableLiveData<>();
    public final MutableLiveData<MyBalanceDetail> myBalance = new MutableLiveData<>();
    public final MutableLiveData<InviteCodePublic> inviteCode = new MutableLiveData<>();
    public final MutableLiveData<EventMemberPublic> joinResult = new MutableLiveData<>();
    public final MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public EventViewModel(Application application) {
        super(application);
        repository = new EventRepository();
    }

    public void loadEvents() {
        isLoading.setValue(true);
        repository.getEvents(0, 100, events, errorMessage);
        // isLoading sẽ được reset ở observer trong Activity/Fragment
    }

    public void createEvent(String name, String description) {
        isLoading.setValue(true);
        repository.createEvent(name, description, createdEvent, errorMessage);
    }

    public void loadEvent(String eventId) {
        repository.getEvent(eventId, selectedEvent, errorMessage);
    }

    public void deleteEvent(String eventId) {
        repository.deleteEvent(eventId, deleteResult, errorMessage);
    }

    public void addMember(String eventId, String email) {
        repository.addMember(eventId, email, addedMember, errorMessage);
    }

    public void loadEventBalances(String eventId) {
        repository.getEventBalances(eventId, eventBalances, errorMessage);
    }

    public void loadSimplifiedDebts(String eventId) {
        repository.getSimplifiedDebts(eventId, simplifiedDebts, errorMessage);
    }

    public void loadEventStats(String eventId) {
        repository.getEventStats(eventId, eventStats, errorMessage);
    }

    public void loadMyBalance() {
        repository.getMyBalance(myBalance, errorMessage);
    }

    public void createInviteCode(String eventId) {
        repository.createInviteCode(eventId, inviteCode, errorMessage);
    }

    public void joinEvent(String code) {
        repository.joinEvent(code, joinResult, errorMessage);
    }
}
