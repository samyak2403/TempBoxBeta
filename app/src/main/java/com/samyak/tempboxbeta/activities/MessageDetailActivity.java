package com.samyak.tempboxbeta.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.adapters.AttachmentAdapter;
import com.samyak.tempboxbeta.models.Message;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.DateUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "MessageDetailActivity";
    private static final String EXTRA_MESSAGE_ID = "message_id";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private TextView senderText;
    private TextView subjectText;
    private TextView dateText;
    private WebView contentWebView;
    private RecyclerView attachmentsRecyclerView;
    private TextView attachmentsLabel;
    private ProgressBar progressBar;
    private View errorState;
    private TextView errorTitle;
    private TextView errorMessage;
    private View retryButton;
    
    private String messageId;
    private Message currentMessage;
    private AuthManager authManager;
    private AttachmentAdapter attachmentAdapter;
    private int retryAttempt = 0;
    
    public static Intent createIntent(Context context, String messageId) {
        Intent intent = new Intent(context, MessageDetailActivity.class);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        
        messageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        if (messageId == null) {
            finish();
            return;
        }
        
        authManager = AuthManager.getInstance(this);
        
        initViews();
        setupViews();
        loadMessage();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        senderText = findViewById(R.id.sender_text);
        subjectText = findViewById(R.id.subject_text);
        dateText = findViewById(R.id.date_text);
        contentWebView = findViewById(R.id.content_webview);
        attachmentsRecyclerView = findViewById(R.id.attachments_recycler_view);
        attachmentsLabel = findViewById(R.id.attachments_label);
        progressBar = findViewById(R.id.progress_bar);
        errorState = findViewById(R.id.error_state);
        errorTitle = findViewById(R.id.error_title);
        errorMessage = findViewById(R.id.error_message);
        retryButton = findViewById(R.id.retry_button);
    }
    
    private void setupViews() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Message");
        }
        
        // Setup WebView with enhanced security
        contentWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Hide progress when page loads
                progressBar.setVisibility(View.GONE);
            }
        });
        
        contentWebView.getSettings().setJavaScriptEnabled(false);
        contentWebView.getSettings().setDomStorageEnabled(false);
        contentWebView.getSettings().setAllowFileAccess(false);
        contentWebView.getSettings().setAllowContentAccess(false);
        contentWebView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        
        // Setup pull-to-refresh
        swipeRefresh.setOnRefreshListener(() -> {
            retryAttempt = 0; // Reset retry attempts
            hideErrorState();
            loadMessage();
        });
        
        // Setup retry button
        retryButton.setOnClickListener(v -> {
            retryAttempt = 0; // Reset retry attempts
            hideErrorState();
            loadMessage();
        });
        
        // Setup attachments RecyclerView
        attachmentAdapter = new AttachmentAdapter();
        attachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attachmentsRecyclerView.setAdapter(attachmentAdapter);
    }
    
    private void loadMessage() {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            showErrorState("No Internet Connection", "Please check your connection and try again");
            return;
        }
        
        String token = authManager.getFormattedAuthToken();
        if (token == null || token.trim().isEmpty()) {
            handleAuthError("Authentication required. Please login again.");
            return;
        }
        
        Log.d(TAG, "Loading message with ID: " + messageId);
        
        // Show loading state
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        retryAttempt++;
        
        ApiClient.getApiService().getMessage(token, messageId)
                .enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Message loaded successfully");
                            currentMessage = response.body();
                            retryAttempt = 0; // Reset retry count
                            hideErrorState();
                            displayMessage();
                            markAsReadIfNeeded();
                        } else {
                            if (response.code() == 401) {
                                handleAuthError("Session expired. Please login again.");
                            } else if (response.code() == 404) {
                                showErrorState("Message Not Found", "This message may have been deleted");
                            } else {
                                String errorMsg = "Failed to load message (Error " + response.code() + ")";
                                handleErrorWithRetry(errorMsg);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Network error loading message", t);
                        
                        String errorMessage;
                        if (t instanceof JsonSyntaxException) {
                            errorMessage = "Message format error - API response changed";
                            Log.e(TAG, "JSON parsing error: " + t.getMessage());
                            // For JSON errors, don't retry as it won't help
                            showErrorState("Data Format Error", "The message format has changed.\nPlease update the app or try again later.");
                        } else if (t instanceof java.net.UnknownHostException) {
                            errorMessage = "Cannot reach server - check your internet connection";
                            handleErrorWithRetry(errorMessage);
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage = "Connection timeout - server is slow";
                            handleErrorWithRetry(errorMessage);
                        } else if (t instanceof java.net.ConnectException) {
                            errorMessage = "Cannot connect to server";
                            handleErrorWithRetry(errorMessage);
                        } else {
                            errorMessage = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                            handleErrorWithRetry(errorMessage);
                        }
                    }
                });
    }
    
    private void displayMessage() {
        if (currentMessage == null) {
            Log.w(TAG, "Cannot display message - currentMessage is null");
            showErrorState("Message Error", "Unable to load message data");
            return;
        }
        
        try {
            // Set sender info
            if (currentMessage.getFrom() != null) {
                String senderInfo = currentMessage.getFrom().toString();
                senderText.setText(senderInfo != null ? senderInfo : "Unknown Sender");
            } else {
                senderText.setText("Unknown Sender");
            }
            
            // Set subject
            String subject = currentMessage.getSubject();
            if (subject != null && !subject.trim().isEmpty()) {
                subjectText.setText(subject);
            } else {
                subjectText.setText("(No Subject)");
            }
            
            // Set date
            String createdAt = currentMessage.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                try {
                    dateText.setText(DateUtils.formatDisplayDateTime(createdAt));
                } catch (Exception e) {
                    Log.w(TAG, "Error formatting date: " + createdAt, e);
                    dateText.setText(createdAt); // Fallback to raw date
                }
            } else {
                dateText.setText("Date not available");
            }
            
            // Display content
            displayContent();
            
            // Display attachments
            displayAttachments();
            
            Log.d(TAG, "Message displayed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying message", e);
            showErrorState("Display Error", "Error displaying message content");
        }
    }
    
    private void displayContent() {
        try {
            String content = "";
            
            // Prefer HTML content if available
            if (currentMessage.getHtml() != null && !currentMessage.getHtml().isEmpty()) {
                content = currentMessage.getHtml().get(0);
                if (content != null && !content.trim().isEmpty()) {
                    // Sanitize HTML content
                    content = sanitizeHtmlContent(content);
                    contentWebView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
                    return;
                }
            }
            
            // Fallback to plain text
            if (currentMessage.getText() != null && !currentMessage.getText().trim().isEmpty()) {
                content = currentMessage.getText().replace("\n", "<br>");
                // Escape HTML characters for safety
                content = content.replace("<", "&lt;").replace(">", "&gt;");
                String htmlContent = "<html><head><meta charset='UTF-8'></head><body style='margin:16px;font-family:sans-serif;line-height:1.6;'>" + content + "</body></html>";
                contentWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            } else {
                // No content available
                contentWebView.loadDataWithBaseURL(null, 
                        "<html><head><meta charset='UTF-8'></head><body style='margin:16px;color:#666;text-align:center;padding-top:50px;'>No content available</body></html>", 
                        "text/html", "UTF-8", null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying message content", e);
            contentWebView.loadDataWithBaseURL(null, 
                    "<html><head><meta charset='UTF-8'></head><body style='margin:16px;color:#666;text-align:center;padding-top:50px;'>Error loading content</body></html>", 
                    "text/html", "UTF-8", null);
        }
    }
    
    private String sanitizeHtmlContent(String html) {
        if (html == null) return "";
        
        // Basic HTML sanitization - remove potentially dangerous elements
        html = html.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        html = html.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        html = html.replaceAll("(?i)<object[^>]*>.*?</object>", "");
        html = html.replaceAll("(?i)<embed[^>]*>.*?</embed>", "");
        html = html.replaceAll("(?i)<form[^>]*>.*?</form>", "");
        html = html.replaceAll("(?i)javascript:", "");
        html = html.replaceAll("(?i)on\\w+\\s*=", "");
        
        return html;
    }
    
    private void displayAttachments() {
        if (currentMessage.getAttachments() != null && !currentMessage.getAttachments().isEmpty()) {
            attachmentsLabel.setVisibility(View.VISIBLE);
            attachmentsRecyclerView.setVisibility(View.VISIBLE);
            attachmentAdapter.updateAttachments(currentMessage.getAttachments());
        } else {
            attachmentsLabel.setVisibility(View.GONE);
            attachmentsRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void markAsReadIfNeeded() {
        if (currentMessage != null && !currentMessage.isSeen()) {
            String token = authManager.getFormattedAuthToken();
            if (token != null && isNetworkAvailable()) {
                Log.d(TAG, "Marking message as read");
                ApiClient.getApiService().markMessageAsRead(token, messageId)
                        .enqueue(new Callback<Message>() {
                            @Override
                            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                                if (response.isSuccessful()) {
                                    Log.d(TAG, "Message marked as read successfully");
                                    currentMessage.setSeen(true);
                                } else {
                                    Log.w(TAG, "Failed to mark message as read: " + response.code());
                                }
                            }
                            
                            @Override
                            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                                Log.w(TAG, "Network error marking message as read", t);
                                // Don't show error to user for this operation
                            }
                        });
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_detail_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteMessage();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void deleteMessage() {
        if (!isNetworkAvailable()) {
            handleNetworkError("Cannot delete message - no internet connection", false);
            return;
        }
        
        String token = authManager.getFormattedAuthToken();
        if (token == null) {
            handleAuthError("Authentication required to delete message");
            return;
        }
        
        Log.d(TAG, "Deleting message: " + messageId);
        progressBar.setVisibility(View.VISIBLE);
        
        ApiClient.getApiService().deleteMessage(token, messageId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Message deleted successfully");
                            Toast.makeText(MessageDetailActivity.this, 
                                    Constants.SUCCESS_MESSAGE_DELETED, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Notify calling activity
                            finish();
                        } else if (response.code() == 401) {
                            handleAuthError("Session expired. Please login again.");
                        } else if (response.code() == 404) {
                            Toast.makeText(MessageDetailActivity.this, 
                                    "Message already deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            handleError("Failed to delete message (Error " + response.code() + ")");
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Network error deleting message", t);
                        handleError("Network error: Cannot delete message. Check your connection.");
                    }
                });
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
    
    private void handleError(String message) {
        Log.e(TAG, "Error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void handleErrorWithRetry(String message) {
        if (retryAttempt < MAX_RETRY_ATTEMPTS) {
            Toast.makeText(this, message + " - Retrying... (" + retryAttempt + "/" + MAX_RETRY_ATTEMPTS + ")", Toast.LENGTH_SHORT).show();
            
            // Retry after 2 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadMessage();
            }, 2000);
        } else {
            showErrorState("Connection Failed", message + "\nPlease check your internet connection");
        }
    }
    
    private void showErrorState(String title, String message) {
        errorTitle.setText(title);
        errorMessage.setText(message);
        errorState.setVisibility(View.VISIBLE);
        swipeRefresh.setVisibility(View.GONE);
    }
    
    private void hideErrorState() {
        errorState.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
    }
    
    private void handleNetworkError(String message, boolean showRetryOption) {
        handleError(message);
        if (showRetryOption) {
            Toast.makeText(this, "Pull down to refresh or check your internet connection", Toast.LENGTH_LONG).show();
        }
    }
    
    private void handleAuthError(String message) {
        handleError(message);
        // Clear invalid auth data
        authManager.logout();
        finish();
    }
} 