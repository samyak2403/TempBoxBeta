package com.samyak.tempboxbeta.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samyak.tempboxbeta.R;
import com.ncorti.slidetoact.SlideToActView;
import com.samyak.tempboxbeta.adapters.DomainAdapter;
import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.models.ApiResponse;
import com.samyak.tempboxbeta.models.AuthToken;
import com.samyak.tempboxbeta.models.Domain;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.GeneratorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountFragment extends Fragment {
    
    private TextInputLayout domainInputLayout;
    private AutoCompleteTextView domainDropdown;
    private TextInputLayout usernameInputLayout;
    private TextInputEditText usernameEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private MaterialButton createButton;
    private SlideToActView quickGenerateButton;
    private MaterialButton generateUsernameButton;
    private MaterialButton generatePasswordButton;
    private ProgressBar progressBar;
    
    private List<Domain> availableDomains;
    private DomainAdapter domainAdapter;
    private AuthManager authManager;
    
    private OnAccountCreatedListener accountCreatedListener;
    
    public interface OnAccountCreatedListener {
        void onAccountCreated();
    }
    
    public static CreateAccountFragment newInstance() {
        return new CreateAccountFragment();
    }
    
    public void setOnAccountCreatedListener(OnAccountCreatedListener listener) {
        this.accountCreatedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupViews();
        
        authManager = AuthManager.getInstance(requireContext());
        availableDomains = new ArrayList<>();
        
        loadDomains();
    }
    
    private void initViews(View view) {
        domainInputLayout = view.findViewById(R.id.domain_input_layout);
        domainDropdown = view.findViewById(R.id.domain_dropdown);
        usernameInputLayout = view.findViewById(R.id.username_input_layout);
        usernameEditText = view.findViewById(R.id.username_edit_text);
        passwordInputLayout = view.findViewById(R.id.password_input_layout);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        createButton = view.findViewById(R.id.create_button);
        quickGenerateButton = view.findViewById(R.id.quick_generate_button);
        generateUsernameButton = view.findViewById(R.id.generate_username_button);
        generatePasswordButton = view.findViewById(R.id.generate_password_button);
        progressBar = view.findViewById(R.id.progress_bar);
    }
    
    private void setupViews() {
        // Setup text watchers for validation
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };
        
        usernameEditText.addTextChangedListener(validationWatcher);
        passwordEditText.addTextChangedListener(validationWatcher);
        
        domainDropdown.setOnItemClickListener((parent, view, position, id) -> validateForm());
        
        // Setup click listeners
        createButton.setOnClickListener(v -> createAccount());
        quickGenerateButton.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                quickGenerateEmail();
            }
        });
        generateUsernameButton.setOnClickListener(v -> generateRandomUsername());
        generatePasswordButton.setOnClickListener(v -> generateSecurePassword());
        
        // Setup end icon clicks for text fields
        usernameInputLayout.setEndIconOnClickListener(v -> generateRandomUsername());
    }
    
    /**
     * Mail.tm style instant email generation
     */
    private void quickGenerateEmail() {
        if (availableDomains.isEmpty()) {
            Toast.makeText(getContext(), "Loading domains, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading state and disable button
        quickGenerateButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        // Auto-generate username and password
        String randomUsername = GeneratorUtils.generateRandomUsername();
        String randomPassword = GeneratorUtils.generateSecurePassword();
        
        // Select random domain
        Domain randomDomain = availableDomains.get((int) (Math.random() * availableDomains.size()));
        
        // Create account instantly
        createAccountWithCredentials(randomUsername, randomPassword, randomDomain);
    }
    
    /**
     * Generate random username
     */
    private void generateRandomUsername() {
        String randomUsername = GeneratorUtils.generateRandomUsername();
        usernameEditText.setText(randomUsername);
        
        // Show toast with generation info
        Toast.makeText(getContext(), "Random username generated: " + randomUsername, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Generate secure password
     */
    private void generateSecurePassword() {
        String securePassword = GeneratorUtils.generateSecurePassword();
        passwordEditText.setText(securePassword);
        
        // Show toast with generation info
        Toast.makeText(getContext(), "Secure password generated! Length: " + securePassword.length(), Toast.LENGTH_SHORT).show();
    }
    
    private void loadDomains() {
        progressBar.setVisibility(View.VISIBLE);
        createButton.setEnabled(false);
        quickGenerateButton.setEnabled(false);
        
        ApiClient.getApiService().getDomains(1)
                .enqueue(new Callback<ApiResponse<Domain>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Domain>> call, 
                                         @NonNull Response<ApiResponse<Domain>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Domain> domains = response.body().getMembers();
                            if (domains != null && !domains.isEmpty()) {
                                setupDomainDropdown(domains);
                            } else {
                                handleError("No domains available");
                            }
                        } else {
                            handleError("Failed to load domains");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Domain>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void setupDomainDropdown(List<Domain> domains) {
        // Filter active domains only
        availableDomains.clear();
        for (Domain domain : domains) {
            if (domain.isActive() && !domain.isPrivate()) {
                availableDomains.add(domain);
            }
        }
        
        if (!availableDomains.isEmpty()) {
            domainAdapter = new DomainAdapter(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, availableDomains);
            domainDropdown.setAdapter(domainAdapter);
            
            // Set first domain as default
            domainDropdown.setText(availableDomains.get(0).getDomain(), false);
            validateForm();
            
            // Enable quick generate button
            quickGenerateButton.setEnabled(true);
        } else {
            handleError("No active domains available");
        }
    }
    
    private void validateForm() {
        boolean isValid = true;
        
        // Validate username
        String username = usernameEditText.getText() != null ? 
                usernameEditText.getText().toString().trim() : "";
        if (username.length() < 3) {
            usernameInputLayout.setError("Username must be at least 3 characters");
            isValid = false;
        } else if (!isValidUsername(username)) {
            usernameInputLayout.setError("Username can only contain letters, numbers, dots, and underscores");
            isValid = false;
        } else {
            usernameInputLayout.setError(null);
        }
        
        // Validate password
        String password = passwordEditText.getText() != null ? 
                passwordEditText.getText().toString() : "";
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            passwordInputLayout.setError("Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }
        
        // Validate domain selection
        String selectedDomain = domainDropdown.getText().toString();
        if (selectedDomain.isEmpty()) {
            domainInputLayout.setError("Please select a domain");
            isValid = false;
        } else {
            domainInputLayout.setError(null);
        }
        
        createButton.setEnabled(isValid && !availableDomains.isEmpty());
    }
    
    private boolean isValidUsername(String username) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._]+$");
        return pattern.matcher(username).matches();
    }
    
    private void createAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String selectedDomainName = domainDropdown.getText().toString();
        
        Domain selectedDomain = null;
        for (Domain domain : availableDomains) {
            if (domain.getDomain().equals(selectedDomainName)) {
                selectedDomain = domain;
                break;
            }
        }
        
        if (selectedDomain == null) {
            handleError("Please select a valid domain");
            return;
        }
        
        createAccountWithCredentials(username, password, selectedDomain);
    }
    
    private void createAccountWithCredentials(String username, String password, Domain domain) {
        String emailAddress = username + "@" + domain.getDomain();
        
        progressBar.setVisibility(View.VISIBLE);
        createButton.setEnabled(false);
        quickGenerateButton.setEnabled(false);
        
        Account account = new Account(emailAddress, password);
        ApiClient.getApiService().createAccount(account)
                .enqueue(new Callback<Account>() {
                    @Override
                    public void onResponse(@NonNull Call<Account> call, @NonNull Response<Account> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Account createdAccount = response.body();
                            getAuthToken(emailAddress, password, createdAccount);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            createButton.setEnabled(true);
                            quickGenerateButton.setEnabled(true);
                            handleError("Failed to create account. Email might already exist.");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Account> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        createButton.setEnabled(true);
                        quickGenerateButton.setEnabled(true);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void getAuthToken(String emailAddress, String password, Account account) {
        Account credentials = new Account(emailAddress, password);
        ApiClient.getApiService().getToken(credentials)
                .enqueue(new Callback<AuthToken>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthToken> call, @NonNull Response<AuthToken> response) {
                        progressBar.setVisibility(View.GONE);
                        createButton.setEnabled(true);
                        quickGenerateButton.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            AuthToken authToken = response.body();
                            
                            // Save authentication data
                            authManager.saveAuthToken(authToken);
                            authManager.saveAccount(account);
                            authManager.savePassword(password);
                            
                            // Clear form
                            clearForm();
                            
                            // Show success message
                            showSuccess("🎉 Email created successfully: " + emailAddress);
                            
                            // Notify listener
                            if (accountCreatedListener != null) {
                                accountCreatedListener.onAccountCreated();
                            }
                        } else {
                            handleError("Failed to authenticate with created account");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<AuthToken> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        createButton.setEnabled(true);
                        quickGenerateButton.setEnabled(true);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void clearForm() {
        usernameEditText.setText("");
        passwordEditText.setText("");
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);
        domainInputLayout.setError(null);
    }
    
    private void handleError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
} 