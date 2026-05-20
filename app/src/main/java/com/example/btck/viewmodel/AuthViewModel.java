package com.example.btck.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.btck.api.RetrofitClient;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.*;
import com.example.btck.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final TokenManager tokenManager;

    public final MutableLiveData<TokenResponse> loginResult = new MutableLiveData<>();
    public final MutableLiveData<UserPublic> registerResult = new MutableLiveData<>();
    public final MutableLiveData<UserPublic> currentUser = new MutableLiveData<>();
    public final MutableLiveData<MessageResponse> passwordRecoveryResult = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(Application application) {
        super(application);
        tokenManager = new TokenManager(application);
        RetrofitClient.init(application);
        repository = new AuthRepository();
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        repository.login(email, password, loginResult, errorMessage);
    }

    public void register(String email, String password, String fullName) {
        isLoading.setValue(true);
        repository.register(email, password, fullName, registerResult, errorMessage);
    }

    public void recoverPassword(String email) {
        isLoading.setValue(true);
        repository.recoverPassword(email, passwordRecoveryResult, errorMessage);
    }

    public void fetchCurrentUser() {
        repository.getCurrentUser(currentUser, errorMessage);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        tokenManager.saveTokens(accessToken, refreshToken);
        RetrofitClient.reset();
        RetrofitClient.init(getApplication());
    }

    public void saveUserInfo(String userId, String email, String fullName) {
        tokenManager.saveUserInfo(userId, email, fullName);
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public void logout() {
        tokenManager.clearAll();
        RetrofitClient.reset();
    }
}
