package com.samyak.tempboxbeta.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.samyak.tempboxbeta.R;
import com.samyak.tempboxbeta.activities.MessageDetailActivity;
import com.samyak.tempboxbeta.adapters.MessageAdapter;
import com.samyak.tempboxbeta.models.ApiResponse;
import com.samyak.tempboxbeta.models.Message;
import com.samyak.tempboxbeta.network.ApiClient;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.DateUtils;
import com.samyak.tempboxbeta.utils.PreloadManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxFragment extends Fragment implements MessageAdapter.OnMessageClickListener, PreloadManager.PreloadCallback {
    
    private static final String TAG = "InboxFragment";
    private static final long AUTO_REFRESH_INTERVAL = 30000; // 30 seconds
    private static final long FAST_REFRESH_INTERVAL = 10000; // 10 seconds when active
    
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private View emptyState;
    private ProgressBar progressBar;
    private TextView emailAddressText;
    private View emailInfoLayout;
    
    private MessageAdapter messageAdapter;
    private AuthManager authManager;
    private PreloadManager preloadManager;
    private boolean isLoading = false;
    private boolean isRealTimeEnabled = true;
    private boolean isVisible = false;
    
    // Real-time update handlers
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private List<Message> cachedMessages = new ArrayList<>();
    private long lastRefreshTime = 0;

    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupRealTimeUpdates();
        
        authManager = AuthManager.getInstance(requireContext());
        preloadManager = PreloadManager.getInstance(requireContext());
        preloadManager.addPreloadCallback(this);
        
        if (authManager.isLoggedIn()) {
            showEmailInfo();
            
            // Try to load from cache first for instant display
            if (preloadManager.hasCachedData()) {
                List<Message> cachedData = preloadManager.getCachedMessages();
                if (!cachedData.isEmpty()) {
                    updateMessagesSmartly(cachedData, false);
                    showMessages();
                    Log.d(TAG, "Loaded " + cachedData.size() + " messages from cache (instant)");
                }
            }
            
            // Then load fresh data
            loadMessages();
            startRealTimeUpdates();
            preloadManager.startPreloading();
        } else {
            showEmptyState();
        }
    }
    
    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.messages_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        emailAddressText = view.findViewById(R.id.email_address_text);
        emailInfoLayout = view.findViewById(R.id.email_info_layout);
    }
    
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(getContext());
        messageAdapter.setOnMessageClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setHasFixedSize(true);
        
        // Performance optimizations for fast loading
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        // Smooth scrolling optimizations
        recyclerView.setNestedScrollingEnabled(false);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            if (authManager.isLoggedIn()) {
                loadMessages(true); // Force refresh
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
        
        // Set refresh colors for better UX
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }
    
    private void setupRealTimeUpdates() {
        autoRefreshHandler = new Handler(Looper.getMainLooper());
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRealTimeEnabled && authManager.isLoggedIn() && isVisible) {
                    Log.d(TAG, "Auto-refreshing messages");
                    loadMessages(false); // Silent refresh
                }
                scheduleNextRefresh();
            }
        };
    }
    
    private void showEmailInfo() {
        String email = authManager.getEmailAddress();
        if (email != null) {
            emailAddressText.setText(email);
            emailInfoLayout.setVisibility(View.VISIBLE);
        }
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emailInfoLayout.setVisibility(View.GONE);
    }
    
    private void showMessages() {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
    
    private void loadMessages() {
        loadMessages(false);
    }
    
    private void loadMessages(boolean forceRefresh) {
        if (isLoading) return;
        
        String token = authManager.getFormattedAuthToken();
        if (token == null) {
            showEmptyState();
            return;
        }
        
        // Skip if recently refreshed and not forced
        long currentTime = System.currentTimeMillis();
        if (!forceRefresh && (currentTime - lastRefreshTime) < 5000) {
            Log.d(TAG, "Skipping refresh - too recent");
            return;
        }
        
        isLoading = true;
        lastRefreshTime = currentTime;
        
        // Show loading only for manual refresh or first load
        if (forceRefresh || cachedMessages.isEmpty()) {
            if (!swipeRefresh.isRefreshing()) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        
        Log.d(TAG, "Loading messages - Force: " + forceRefresh);
        
        ApiClient.getApiService().getMessages(token, 1)
                .enqueue(new Callback<ApiResponse<Message>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Message>> call, 
                                         @NonNull Response<ApiResponse<Message>> response) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Message> messages = response.body().getMembers();
                            
                            if (messages != null && !messages.isEmpty()) {
                                updateMessagesSmartly(messages, forceRefresh);
                                showMessages();
                                Log.d(TAG, "Loaded " + messages.size() + " messages");
                            } else {
                                if (forceRefresh || cachedMessages.isEmpty()) {
                                    messageAdapter.clearMessages();
                                    cachedMessages.clear();
                                    showEmptyState();
                                }
                            }
                        } else {
                            if (forceRefresh) {
                                handleError("Failed to load messages");
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Message>> call, @NonNull Throwable t) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        
                        if (forceRefresh) {
                            handleError(Constants.ERROR_NETWORK);
                        } else {
                            Log.w(TAG, "Silent refresh failed: " + t.getMessage());
                        }
                    }
                });
    }
    
    private void handleError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onMessageClick(Message message) {
        // Show brief loading feedback
        if (getContext() != null) {
            Toast.makeText(getContext(), "Opening message...", Toast.LENGTH_SHORT).show();
        }
        
        // Navigate to message detail activity with slight delay for visual feedback
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = MessageDetailActivity.createIntent(requireContext(), message.getId());
            startActivity(intent);
            
            // Add smooth transition animation
            if (getActivity() != null) {
                getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left, 
                    android.R.anim.slide_out_right
                );
            }
        }, 100); // Small delay for better UX
        
        // Mark message as read if not already read
        if (!message.isSeen()) {
            markMessageAsRead(message);
        }
    }
    
    private void markMessageAsRead(Message message) {
        String token = authManager.getFormattedAuthToken();
        if (token == null) return;
        
        ApiClient.getApiService().markMessageAsRead(token, message.getId())
                .enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                        if (response.isSuccessful()) {
                            // Update the message in adapter
                            message.setSeen(true);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                        // Ignore failure to mark as read
                    }
                });
    }
    
    public void refreshMessages() {
        if (authManager.isLoggedIn()) {
            loadMessages();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        
        // Check if user logged in or out
        if (authManager.isLoggedIn()) {
            showEmailInfo();
            if (messageAdapter.getItemCount() == 0) {
                loadMessages(true);
            } else {
                // Quick refresh when returning to screen
                loadMessages(false);
            }
            startRealTimeUpdates();
        } else {
            showEmptyState();
            messageAdapter.clearMessages();
            cachedMessages.clear();
            stopRealTimeUpdates();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
        stopRealTimeUpdates();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRealTimeUpdates();
        if (preloadManager != null) {
            preloadManager.removePreloadCallback(this);
            preloadManager.stopPreloading();
        }
    }
    
    // PreloadManager.PreloadCallback implementation
    @Override
    public void onPreloadComplete(List<Message> messages) {
        if (isVisible && !isLoading) {
            Log.d(TAG, "Background preload complete: " + messages.size() + " messages");
            updateMessagesSmartly(messages, false);
        }
    }
    
    @Override
    public void onPreloadFailed(String error) {
        Log.w(TAG, "Background preload failed: " + error);
        // Silent failure - don't show error to user for background preloads
    }
    
    // Real-time update methods
    private void startRealTimeUpdates() {
        if (isRealTimeEnabled && autoRefreshHandler != null) {
            stopRealTimeUpdates(); // Stop any existing updates
            scheduleNextRefresh();
            Log.d(TAG, "Real-time updates started");
        }
    }
    
    private void stopRealTimeUpdates() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
            Log.d(TAG, "Real-time updates stopped");
        }
    }
    
    private void scheduleNextRefresh() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null && isRealTimeEnabled) {
            // Use faster refresh when user is actively using the app
            long interval = isVisible ? FAST_REFRESH_INTERVAL : AUTO_REFRESH_INTERVAL;
            autoRefreshHandler.postDelayed(autoRefreshRunnable, interval);
        }
    }
    
    // Smart message updating with diff detection
    private void updateMessagesSmartly(List<Message> newMessages, boolean forceRefresh) {
        if (forceRefresh || cachedMessages.isEmpty()) {
            // Full update
            cachedMessages.clear();
            cachedMessages.addAll(newMessages);
            messageAdapter.updateMessages(newMessages);
            return;
        }
        
        // Smart diff update
        List<Message> messagesToAdd = new ArrayList<>();
        boolean hasChanges = false;
        
        // Check for new messages
        for (Message newMessage : newMessages) {
            boolean found = false;
            for (Message cachedMessage : cachedMessages) {
                if (newMessage.getId().equals(cachedMessage.getId())) {
                    found = true;
                    // Check if message status changed (read/unread)
                    if (newMessage.isSeen() != cachedMessage.isSeen()) {
                        cachedMessage.setSeen(newMessage.isSeen());
                        hasChanges = true;
                    }
                    break;
                }
            }
            if (!found) {
                messagesToAdd.add(newMessage);
                hasChanges = true;
            }
        }
        
        // Add new messages to cache and adapter
        if (!messagesToAdd.isEmpty()) {
            cachedMessages.addAll(0, messagesToAdd); // Add to beginning
            Log.d(TAG, "Added " + messagesToAdd.size() + " new messages");
            
            // Show brief notification for new messages
            if (isVisible && messagesToAdd.size() > 0) {
                String message = messagesToAdd.size() == 1 ? 
                    "New message received" : messagesToAdd.size() + " new messages";
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        // Update adapter if there are changes
        if (hasChanges) {
            messageAdapter.updateMessages(cachedMessages);
        }
    }
    
    // Public method to enable/disable real-time updates
    public void setRealTimeUpdatesEnabled(boolean enabled) {
        isRealTimeEnabled = enabled;
        if (enabled) {
            startRealTimeUpdates();
        } else {
            stopRealTimeUpdates();
        }
    }
} 