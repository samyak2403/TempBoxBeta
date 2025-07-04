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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonSyntaxException;
import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.adapters.AttachmentAdapter;
import com.samyak.tempboxbeta.models.Message;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.DateUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageDetailActivity extends AppCompatActivity {

    private static final String TAG = "MessageDetailActivity";
    private static final String EXTRA_MESSAGE_ID = "message_id";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private TextView senderName;
    private TextView senderEmail;
    private TextView subjectText;
    private TextView dateText;
    private WebView contentWebView;
    private RecyclerView attachmentsRecyclerView;
    private ProgressBar progressBar;
    private ProgressBar webviewProgressBar;
    private View errorState;
    private TextView errorTitle;
    private TextView errorMessage;
    private View retryButton;
    private View headerContent;
    private View webviewContainer;
    private View attachmentsSection;

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
        senderName = findViewById(R.id.sender_name);
        senderEmail = findViewById(R.id.sender_email);
        subjectText = findViewById(R.id.subject_text);
        dateText = findViewById(R.id.date_text);
        contentWebView = findViewById(R.id.content_webview);
        attachmentsRecyclerView = findViewById(R.id.attachments_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        webviewProgressBar = findViewById(R.id.webview_progress_bar);
        errorState = findViewById(R.id.error_state);
        errorTitle = findViewById(R.id.error_title);
        errorMessage = findViewById(R.id.error_message);
        retryButton = findViewById(R.id.retry_button);
        headerContent = findViewById(R.id.header_content);
        webviewContainer = findViewById(R.id.webview_container);
        attachmentsSection = findViewById(R.id.attachments_section);
    }

    private void setupViews() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Message");
        }

        contentWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webviewProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webviewProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "WebView Error: " + description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webviewProgressBar.setVisibility(View.GONE);
            }
        });

        swipeRefresh.setOnRefreshListener(this::loadMessage);
        retryButton.setOnClickListener(v -> {
            hideErrorState();
            loadMessage();
        });

        // Setup attachments adapter
        attachmentAdapter = new AttachmentAdapter(this, new ArrayList<>());
        attachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attachmentsRecyclerView.setAdapter(attachmentAdapter);
    }

    private void loadMessage() {
        if (!isNetworkAvailable()) {
            showErrorState("No Connection", "Please check your internet connection and try again.");
            swipeRefresh.setRefreshing(false);
            return;
        }

        hideErrorState();
        progressBar.setVisibility(View.VISIBLE);
        swipeRefresh.setVisibility(View.GONE);

                String token = authManager.getFormattedAuthToken();
        if (token == null) {
            handleAuthError("Authentication required to view message");
            return;
        }

        Log.d(TAG, "Loading message: " + messageId);
        ApiClient.getApiService().getMessage(token, messageId)
                .enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            retryAttempt = 0; // Reset on success
                            currentMessage = response.body();
                            displayMessage();
                        } else if (response.code() == 401) {
                            handleAuthError("Session expired. Please login again.");
                        } else {
                            handleErrorWithRetry("Failed to load message (Error " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error loading message", t);
                        if (t instanceof JsonSyntaxException) {
                            handleError("Failed to parse message data.");
                        } else {
                            handleErrorWithRetry("Network error: Cannot load message.");
                        }
                    }
                });
    }

    private void displayMessage() {
        if (currentMessage == null) return;

        progressBar.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentMessage.getSubject());
        }

        if (currentMessage.getFrom() != null) {
            senderName.setText(currentMessage.getFrom().getName());
            senderEmail.setText(currentMessage.getFrom().getAddress());
        }
        subjectText.setText(currentMessage.getSubject());
        dateText.setText(DateUtils.formatRelative(currentMessage.getCreatedAt()));

        headerContent.setVisibility(View.VISIBLE);

        displayContent();
        displayAttachments();
        markAsReadIfNeeded();
    }

    private void displayContent() {
        webviewContainer.setVisibility(View.VISIBLE);
        String htmlContent = "";
        if (currentMessage.getHtml() != null && !currentMessage.getHtml().isEmpty()) {
            htmlContent = currentMessage.getHtml().get(0);
        } else if (currentMessage.getText() != null) {
            htmlContent = currentMessage.getText();
        }

        String sanitizedHtml = sanitizeHtmlContent(htmlContent);

        contentWebView.loadDataWithBaseURL(null, sanitizedHtml, "text/html", "UTF-8", null);
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
            attachmentsSection.setVisibility(View.VISIBLE);
            attachmentAdapter.setAttachments(currentMessage.getAttachments());
        } else {
            attachmentsSection.setVisibility(View.GONE);
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