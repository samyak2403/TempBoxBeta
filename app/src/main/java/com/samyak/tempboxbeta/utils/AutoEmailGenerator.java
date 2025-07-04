package com.samyak.tempboxbeta.utils;

import android.content.Context;
import android.util.Log;

import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.models.ApiResponse;
import com.samyak.tempboxbeta.models.AuthToken;
import com.samyak.tempboxbeta.models.Domain;
import com.samyak.tempboxbeta.network.ApiClient;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoEmailGenerator {
    private static final String TAG = "AutoEmailGenerator";
    private static final String[] RANDOM_PREFIXES = {
        "temp", "test", "demo", "user", "mail", "box", "quick", "fast", "auto", "gen"
    };
    private static final String DEFAULT_PASSWORD = "TempBox2024!";
    
    public interface AutoEmailCallback {
        void onSuccess(String emailAddress);
        void onError(String errorMessage);
    }
    
    private final Context context;
    private final AuthManager authManager;
    
    public AutoEmailGenerator(Context context) {
        this.context = context;
        this.authManager = AuthManager.getInstance(context);
    }
    
    public void generateAutoEmail(AutoEmailCallback callback) {
        Log.d(TAG, "Starting automatic email generation");
        
        // First get available domains
        ApiClient.getApiService().getDomains(1)
                .enqueue(new Callback<ApiResponse<Domain>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Domain>> call, Response<ApiResponse<Domain>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Domain> domains = response.body().getData();
                            if (domains != null && !domains.isEmpty()) {
                                // Find first active, non-private domain
                                Domain selectedDomain = null;
                                for (Domain domain : domains) {
                                    if (domain.isActive() && !domain.isPrivate()) {
                                        selectedDomain = domain;
                                        break;
                                    }
                                }
                                
                                if (selectedDomain != null) {
                                    createAutoAccount(selectedDomain, callback);
                                } else {
                                    callback.onError("No available domains found");
                                }
                            } else {
                                callback.onError("No domains available");
                            }
                        } else {
                            callback.onError("Failed to load domains");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<Domain>> call, Throwable t) {
                        Log.e(TAG, "Failed to load domains", t);
                        callback.onError("Network error while loading domains");
                    }
                });
    }
    
    private void createAutoAccount(Domain domain, AutoEmailCallback callback) {
        String username = generateRandomUsername();
        String emailAddress = username + "@" + domain.getDomain();
        String password = DEFAULT_PASSWORD;
        
        Log.d(TAG, "Creating auto account: " + emailAddress);
        
        Account account = new Account(emailAddress, password);
        
        ApiClient.getApiService().createAccount(account)
                .enqueue(new Callback<Account>() {
                    @Override
                    public void onResponse(Call<Account> call, Response<Account> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Account createdAccount = response.body();
                            // Get authentication token
                            getAutoAuthToken(emailAddress, password, createdAccount, callback);
                        } else {
                            // If username exists, try with a different one
                            if (response.code() == 422) {
                                Log.d(TAG, "Username exists, trying different one");
                                createAutoAccount(domain, callback); // Retry with new username
                            } else {
                                callback.onError("Failed to create account");
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Account> call, Throwable t) {
                        Log.e(TAG, "Failed to create auto account", t);
                        callback.onError("Network error while creating account");
                    }
                });
    }
    
    private void getAutoAuthToken(String emailAddress, String password, Account account, AutoEmailCallback callback) {
        Account credentials = new Account(emailAddress, password);
        
        ApiClient.getApiService().getToken(credentials)
                .enqueue(new Callback<AuthToken>() {
                    @Override
                    public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthToken authToken = response.body();
                            
                            // Save authentication data
                            authManager.saveAuthToken(authToken);
                            authManager.saveAccount(account);
                            
                            Log.d(TAG, "Auto email created successfully: " + emailAddress);
                            callback.onSuccess(emailAddress);
                        } else {
                            callback.onError("Failed to authenticate auto account");
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<AuthToken> call, Throwable t) {
                        Log.e(TAG, "Failed to authenticate auto account", t);
                        callback.onError("Network error during authentication");
                    }
                });
    }
    
    private String generateRandomUsername() {
        SecureRandom random = new SecureRandom();
        
        // Use a prefix + random numbers + short UUID
        String prefix = RANDOM_PREFIXES[random.nextInt(RANDOM_PREFIXES.length)];
        int randomNum = 1000 + random.nextInt(9000); // 4-digit number
        String shortUuid = UUID.randomUUID().toString().substring(0, 6);
        
        return prefix + randomNum + shortUuid;
    }
} 