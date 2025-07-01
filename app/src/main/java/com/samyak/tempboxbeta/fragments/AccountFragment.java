package com.samyak.tempboxbeta.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.models.Account;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.DateUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    
    private MaterialCardView accountInfoCard;
    private TextView accountEmailText;
    private TextView accountUsageText;
    private TextView accountCreatedText;
    private MaterialButton refreshAccountButton;
    private MaterialButton copyEmailButton;
    private MaterialButton logoutButton;
    private MaterialButton deleteAccountButton;
    private MaterialButton goToCreateButton;
    private View noAccountState;
    private ProgressBar progressBar;
    private ProgressBar usageProgressBar;
    
    private AuthManager authManager;
    private Account currentAccount;
    
    private OnAccountActionListener accountActionListener;
    
    public interface OnAccountActionListener {
        void onAccountDeleted();
        void onLogout();
        void onNavigateToCreate();
    }
    
    public static AccountFragment newInstance() {
        return new AccountFragment();
    }
    
    public void setOnAccountActionListener(OnAccountActionListener listener) {
        this.accountActionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        
        authManager = AuthManager.getInstance(requireContext());
        
        updateUI();
    }
    
    private void initViews(View view) {
        accountInfoCard = view.findViewById(R.id.account_info_card);
        accountEmailText = view.findViewById(R.id.account_email);
        accountUsageText = view.findViewById(R.id.account_usage);
        accountCreatedText = view.findViewById(R.id.account_created);
        refreshAccountButton = view.findViewById(R.id.refresh_account_button);
        copyEmailButton = view.findViewById(R.id.copy_email_button);
        logoutButton = view.findViewById(R.id.logout_button);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
        goToCreateButton = view.findViewById(R.id.go_to_create_button);
        noAccountState = view.findViewById(R.id.no_account_state);
        progressBar = view.findViewById(R.id.progress_bar);
        usageProgressBar = view.findViewById(R.id.usage_progress_bar);
    }
    
    private void setupClickListeners() {
        refreshAccountButton.setOnClickListener(v -> refreshAccountInfo());
        copyEmailButton.setOnClickListener(v -> copyEmailToClipboard());
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        deleteAccountButton.setOnClickListener(v -> showDeleteConfirmation());
        goToCreateButton.setOnClickListener(v -> {
            if (accountActionListener != null) {
                accountActionListener.onNavigateToCreate();
            }
        });
    }
    
    private void updateUI() {
        if (authManager.isLoggedIn()) {
            showAccountInfo();
            refreshAccountInfo();
        } else {
            showNoAccountState();
        }
    }
    
    private void showAccountInfo() {
        noAccountState.setVisibility(View.GONE);
        accountInfoCard.setVisibility(View.VISIBLE);
        refreshAccountButton.setVisibility(View.VISIBLE);
        copyEmailButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
        deleteAccountButton.setVisibility(View.VISIBLE);
        
        // Show basic info from stored data
        String email = authManager.getEmailAddress();
        if (email != null) {
            accountEmailText.setText(email);
        }
    }
    
    private void showNoAccountState() {
        accountInfoCard.setVisibility(View.GONE);
        refreshAccountButton.setVisibility(View.GONE);
        copyEmailButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        deleteAccountButton.setVisibility(View.GONE);
        noAccountState.setVisibility(View.VISIBLE);
    }
    
    private void refreshAccountInfo() {
        String token = authManager.getFormattedAuthToken();
        if (token == null) {
            showNoAccountState();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        refreshAccountButton.setEnabled(false);
        
        ApiClient.getApiService().getCurrentAccount(token)
                .enqueue(new Callback<Account>() {
                    @Override
                    public void onResponse(@NonNull Call<Account> call, @NonNull Response<Account> response) {
                        progressBar.setVisibility(View.GONE);
                        refreshAccountButton.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            currentAccount = response.body();
                            updateAccountDisplay();
                        } else {
                            handleError("Failed to refresh account info");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Account> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        refreshAccountButton.setEnabled(true);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void updateAccountDisplay() {
        if (currentAccount == null) return;
        
        accountEmailText.setText(currentAccount.getAddress());
        
        // Smart usage display with infinity support and colors
        setUsageTextWithFormatting(currentAccount.getUsed(), currentAccount.getQuota());
        
        // Update visual progress bar
        updateUsageProgressBar(currentAccount.getUsed(), currentAccount.getQuota());
        
        // Created date
        if (currentAccount.getCreatedAt() != null) {
            String createdText = DateUtils.formatDisplayDateTime(currentAccount.getCreatedAt());
            accountCreatedText.setText(createdText);
        }
    }
    
    private void setUsageTextWithFormatting(int used, int quota) {
        if (getContext() == null) return;
        
        // Check for unlimited/infinity account
        if (isUnlimitedAccount(quota)) {
            String fullText = used + " / ∞ messages (Unlimited)";
            SpannableString spannableString = new SpannableString(fullText);
            
            // Make infinity symbol gold/orange
            int infinityStart = fullText.indexOf("∞");
            if (infinityStart != -1) {
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF9800")), 
                    infinityStart, infinityStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Make "Unlimited" green
            int unlimitedStart = fullText.indexOf("(Unlimited)");
            if (unlimitedStart != -1) {
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), 
                    unlimitedStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            accountUsageText.setText(spannableString);
            return;
        }
        
        // Regular account with quota
        if (quota <= 0) {
            accountUsageText.setText(used + " messages");
            return;
        }
        
        // Calculate percentage for visual indication
        double percentage = quota > 0 ? (double) used / quota * 100 : 0;
        String percentageStr = String.format("%.1f%%", percentage);
        
        // Color-coded usage indication
        String statusText;
        int statusColor;
        if (percentage >= 90) {
            statusText = " (Almost Full)";
            statusColor = Color.parseColor("#F44336"); // Red
        } else if (percentage >= 75) {
            statusText = " (High Usage)";
            statusColor = Color.parseColor("#FF9800"); // Orange
        } else if (percentage >= 50) {
            statusText = " (Half Used)";
            statusColor = Color.parseColor("#FFC107"); // Amber
        } else {
            statusText = " (" + percentageStr + " used)";
            statusColor = Color.parseColor("#4CAF50"); // Green
        }
        
        String fullText = used + " / " + quota + " messages" + statusText;
        SpannableString spannableString = new SpannableString(fullText);
        
        // Color the status part
        int statusStart = fullText.lastIndexOf(statusText);
        if (statusStart != -1) {
            spannableString.setSpan(new ForegroundColorSpan(statusColor), 
                statusStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        accountUsageText.setText(spannableString);
    }
    
    private boolean isUnlimitedAccount(int quota) {
        // Check for common unlimited indicators from Mail.tm API
        return quota <= 0 ||                    // Zero or negative quota (most common)
               quota >= 999999 ||               // Very large quota (999,999+)
               quota == Integer.MAX_VALUE ||    // Maximum integer value
               quota == 999999999;              // Specific large number used by some APIs
    }
    
    private String formatQuotaDisplay(int quota) {
        if (isUnlimitedAccount(quota)) {
            return "∞";
        }
        
        // Format large numbers with commas
        if (quota >= 1000000) {
            return String.format("%.1fM", quota / 1000000.0);
        } else if (quota >= 1000) {
            return String.format("%.1fK", quota / 1000.0);
        } else {
            return String.valueOf(quota);
        }
    }
    
    private void updateUsageProgressBar(int used, int quota) {
        if (usageProgressBar == null) return;
        
        // Hide progress bar for unlimited accounts
        if (isUnlimitedAccount(quota)) {
            usageProgressBar.setVisibility(View.GONE);
            return;
        }
        
        // Show and update progress for limited accounts
        usageProgressBar.setVisibility(View.VISIBLE);
        
        if (quota > 0) {
            int percentage = (int) ((double) used / quota * 100);
            usageProgressBar.setProgress(Math.min(percentage, 100));
            
            // Change progress bar color based on usage
            if (getContext() != null) {
                int progressColor;
                if (percentage >= 90) {
                    progressColor = Color.parseColor("#F44336"); // Red
                } else if (percentage >= 75) {
                    progressColor = Color.parseColor("#FF9800"); // Orange
                } else if (percentage >= 50) {
                    progressColor = Color.parseColor("#FFC107"); // Amber
                } else {
                    progressColor = Color.parseColor("#4CAF50"); // Green
                }
                usageProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(progressColor));
            }
        } else {
            usageProgressBar.setProgress(0);
        }
    }
    
    private void copyEmailToClipboard() {
        String email = authManager.getEmailAddress();
        if (email != null && getContext() != null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Email Address", email);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(getContext(), "Email copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLogoutConfirmation() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout? You will need your email and password to login again.")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        authManager.logout();
        updateUI();
        
        if (accountActionListener != null) {
            accountActionListener.onLogout();
        }
        
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void showDeleteConfirmation() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone and all messages will be permanently lost.")
                .setPositiveButton("Delete", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performDeleteAccount() {
        String token = authManager.getFormattedAuthToken();
        String accountId = authManager.getAccountId();
        
        if (token == null || accountId == null) {
            handleError("Unable to delete account");
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        deleteAccountButton.setEnabled(false);
        
        ApiClient.getApiService().deleteAccount(token, accountId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        progressBar.setVisibility(View.GONE);
                        deleteAccountButton.setEnabled(true);
                        
                        if (response.isSuccessful()) {
                            authManager.clear();
                            updateUI();
                            
                            if (accountActionListener != null) {
                                accountActionListener.onAccountDeleted();
                            }
                            
                            Toast.makeText(getContext(), Constants.SUCCESS_ACCOUNT_DELETED, 
                                    Toast.LENGTH_LONG).show();
                        } else {
                            handleError("Failed to delete account");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        deleteAccountButton.setEnabled(true);
                        handleError(Constants.ERROR_NETWORK);
                    }
                });
    }
    
    private void handleError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
} 