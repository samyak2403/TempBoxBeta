package com.samyak.tempboxbeta.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.models.AuthToken;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private MaterialButton backButton;
    private MaterialButton switchAccountButton;
    private ProgressBar progressBar;
    private View loginFormContainer;
    private View switchAccountContainer;
    private TextView currentAccountText;
    
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        authManager = AuthManager.getInstance(this);
        
        initViews();
        setupClickListeners();
        
        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            showSwitchAccountScreen();
        } else {
            showLoginForm();
        }
    }
    
    private void initViews() {
        emailInputLayout = findViewById(R.id.email_input_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        backButton = findViewById(R.id.back_button);
        switchAccountButton = findViewById(R.id.switch_account_button);
        progressBar = findViewById(R.id.progress_bar);
        loginFormContainer = findViewById(R.id.login_form_container);
        switchAccountContainer = findViewById(R.id.switch_account_container);
        currentAccountText = findViewById(R.id.current_account_text);
    }
    
    private void setupClickListeners() {
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> attemptLogin());
        }
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (switchAccountButton != null) {
            switchAccountButton.setOnClickListener(v -> performSwitchAccount());
        }
    }
    
    private void showSwitchAccountScreen() {
        if (loginFormContainer != null) {
            loginFormContainer.setVisibility(View.GONE);
        }
        if (switchAccountContainer != null) {
            switchAccountContainer.setVisibility(View.VISIBLE);
        }
        
        // Display current account information
        String currentEmail = authManager.getEmailAddress();
        if (currentEmail != null && currentAccountText != null) {
            currentAccountText.setText(getString(R.string.currently_logged_in) + "\n" + currentEmail);
        }
    }
    
    private void showLoginForm() {
        if (switchAccountContainer != null) {
            switchAccountContainer.setVisibility(View.GONE);
        }
        if (loginFormContainer != null) {
            loginFormContainer.setVisibility(View.VISIBLE);
        }
    }
    
    private void performSwitchAccount() {
        // Clear current authentication
        authManager.logout();
        
        // Reset auto email generation flag so new email can be generated
        authManager.setAutoEmailGenerated(false);
        
        // Show login form
        showLoginForm();
        
        // Clear form fields
        if (emailEditText != null) {
            emailEditText.setText("");
        }
        if (passwordEditText != null) {
            passwordEditText.setText("");
        }
        
        Toast.makeText(this, "Logged out. Please login with different credentials.", Toast.LENGTH_SHORT).show();
    }
    
    private void attemptLogin() {
        String email = emailEditText.getText() != null ? 
                emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? 
                passwordEditText.getText().toString() : "";
        
        // Validate inputs
        if (!validateForm(email, password)) {
            return;
        }
        
        // Show loading state
        setLoadingState(true);
        
        // Create account credentials
        Account credentials = new Account(email, password);
        
        // Call API to get auth token
        ApiClient.getApiService().getToken(credentials)
                .enqueue(new Callback<AuthToken>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthToken> call, @NonNull Response<AuthToken> response) {
                        setLoadingState(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            AuthToken authToken = response.body();
                            
                            // Save auth token
                            authManager.saveAuthToken(authToken);
                            
                            // Get account details and save them
                            getAccountDetails(authToken.getToken(), email, password);
                        } else {
                            handleLoginError(response.code());
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<AuthToken> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void getAccountDetails(String token, String email, String password) {
        setLoadingState(true);
        
        ApiClient.getApiService().getCurrentAccount("Bearer " + token)
                .enqueue(new Callback<Account>() {
                    @Override
                    public void onResponse(@NonNull Call<Account> call, @NonNull Response<Account> response) {
                        setLoadingState(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Account account = response.body();
                            
                            // Save account details
                            authManager.saveAccount(account);
                            authManager.savePassword(password);
                            
                            // Show success message
                            Toast.makeText(LoginActivity.this, 
                                    getString(R.string.login_successful), 
                                    Toast.LENGTH_LONG).show();
                            
                            // Close login activity
                            finish();
                        } else {
                            handleError("Failed to get account details");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Account> call, @NonNull Throwable t) {
                        setLoadingState(false);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private boolean validateForm(String email, String password) {
        boolean isValid = true;
        
        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }
        
        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            passwordInputLayout.setError("Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }
        
        return isValid;
    }
    
    private void handleLoginError(int responseCode) {
        String errorMessage;
        switch (responseCode) {
            case 401:
                errorMessage = Constants.ERROR_INVALID_CREDENTIALS;
                break;
            case 422:
                errorMessage = "Invalid email or password format";
                break;
            case 429:
                errorMessage = "Too many login attempts. Please try again later.";
                break;
            default:
                errorMessage = Constants.ERROR_SERVER;
                break;
        }
        handleError(errorMessage);
    }
    
    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            loginButton.setText(getString(R.string.logging_in));
            emailEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            loginButton.setText(getString(R.string.login));
            emailEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
        }
    }
} 