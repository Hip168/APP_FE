package com.example.btck.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.models.*;
import com.example.btck.repository.ExpenseRepository;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    public final MutableLiveData<ExpensesPublic> expenses = new MutableLiveData<>();
    public final MutableLiveData<ExpensePublic> selectedExpense = new MutableLiveData<>();
    public final MutableLiveData<ExpensePublic> createdExpense = new MutableLiveData<>();
    public final MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ExpenseViewModel(Application application) {
        super(application);
        repository = new ExpenseRepository();
    }

    public void loadExpenses(String eventId) {
        isLoading.setValue(true);
        repository.getExpenses(eventId, 0, 100, expenses, errorMessage);
        expenses.observeForever(e -> isLoading.setValue(false));
    }

    public void createExpense(String eventId, ExpenseCreate body) {
        isLoading.setValue(true);
        repository.createExpense(eventId, body, createdExpense, errorMessage);
    }

    public void loadExpense(String eventId, String expenseId) {
        repository.getExpense(eventId, expenseId, selectedExpense, errorMessage);
    }

    public void deleteExpense(String eventId, String expenseId) {
        repository.deleteExpense(eventId, expenseId, deleteResult, errorMessage);
    }
}
