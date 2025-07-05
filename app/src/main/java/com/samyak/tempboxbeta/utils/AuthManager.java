package com.samyak.tempboxbeta.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.models.AuthToken;

public class AuthManager {
    private static final String PREF_NAME = "temp_mail_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_EMAIL_ADDRESS = "email_address";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTO_EMAIL_GENERATED = "auto_email_generated";
    
    private final SharedPreferences preferences;
    private static AuthManager instance;
    
    private AuthManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }
    
    public void saveAuthToken(AuthToken authToken) {
        preferences.edit()
                .putString(KEY_TOKEN, authToken.getToken())
                .putString(KEY_ACCOUNT_ID, authToken.getId())
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }
    
    public void saveAccount(Account account) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_EMAIL_ADDRESS, account.getAddress());
        if (account.getPassword() != null) {
            editor.putString(KEY_PASSWORD, account.getPassword());
        }
        editor.apply();
    }

    public void savePassword(String password) {
        preferences.edit().putString(KEY_PASSWORD, password).apply();
    }
    
    public String getAuthToken() {
        return preferences.getString(KEY_TOKEN, null);
    }
    
    public String getFormattedAuthToken() {
        String token = getAuthToken();
        return token != null ? "Bearer " + token : null;
    }
    
    public String getAccountId() {
        return preferences.getString(KEY_ACCOUNT_ID, null);
    }
    
    public String getEmailAddress() {
        return preferences.getString(KEY_EMAIL_ADDRESS, null);
    }
    
    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, null);
    }
    
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAuthToken() != null && 
               getAccountId() != null && 
               getEmailAddress() != null;
    }
    
    public void logout() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ACCOUNT_ID)
                .remove(KEY_EMAIL_ADDRESS)
                .remove(KEY_PASSWORD)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }
    
    public void clear() {
        preferences.edit().clear().apply();
    }
    
    public boolean hasAutoEmailGenerated() {
        return preferences.getBoolean(KEY_AUTO_EMAIL_GENERATED, false);
    }
    
    public void setAutoEmailGenerated(boolean generated) {
        preferences.edit()
                .putBoolean(KEY_AUTO_EMAIL_GENERATED, generated)
                .apply();
    }
} 