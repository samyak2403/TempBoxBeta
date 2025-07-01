package com.samyak.tempboxbeta.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samyak.tempboxbeta.models.ApiResponse;
import com.samyak.tempboxbeta.models.Message;
import com.samyak.tempboxbeta.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PreloadManager - Handles background data preloading and caching for super fast loading
 */
public class PreloadManager {
    
    private static final String TAG = "PreloadManager";
    private static final long PRELOAD_INTERVAL = 15000; // 15 seconds
    private static final int MAX_CACHE_SIZE = 100;
    
    private static PreloadManager instance;
    private Context context;
    private AuthManager authManager;
    
    private List<Message> preloadedMessages = new ArrayList<>();
    private Handler preloadHandler;
    private Runnable preloadRunnable;
    private boolean isPreloadingEnabled = true;
    private long lastPreloadTime = 0;
    
    // Callback interface for preload notifications
    public interface PreloadCallback {
        void onPreloadComplete(List<Message> messages);
        void onPreloadFailed(String error);
    }
    
    private List<PreloadCallback> callbacks = new ArrayList<>();
    
    private PreloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.authManager = AuthManager.getInstance(context);
        setupPreloader();
    }
    
    public static synchronized PreloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreloadManager(context);
        }
        return instance;
    }
    
    private void setupPreloader() {
        preloadHandler = new Handler(Looper.getMainLooper());
        preloadRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPreloadingEnabled && authManager.isLoggedIn()) {
                    performBackgroundPreload();
                }
                scheduleNextPreload();
            }
        };
    }
    
    public void startPreloading() {
        if (isPreloadingEnabled && authManager.isLoggedIn()) {
            Log.d(TAG, "Starting background preloading");
            scheduleNextPreload();
        }
    }
    
    public void stopPreloading() {
        if (preloadHandler != null && preloadRunnable != null) {
            preloadHandler.removeCallbacks(preloadRunnable);
            Log.d(TAG, "Stopped background preloading");
        }
    }
    
    private void scheduleNextPreload() {
        if (preloadHandler != null && preloadRunnable != null && isPreloadingEnabled) {
            preloadHandler.postDelayed(preloadRunnable, PRELOAD_INTERVAL);
        }
    }
    
    private void performBackgroundPreload() {
        String token = authManager.getFormattedAuthToken();
        if (token == null) return;
        
        // Skip if recently preloaded
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastPreloadTime) < 10000) {
            return;
        }
        
        lastPreloadTime = currentTime;
        Log.d(TAG, "Performing background preload");
        
        ApiClient.getApiService().getMessages(token, 1)
                .enqueue(new Callback<ApiResponse<Message>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Message>> call, 
                                         @NonNull Response<ApiResponse<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Message> messages = response.body().getMembers();
                            if (messages != null) {
                                updateCache(messages);
                                notifyCallbacks(messages);
                                Log.d(TAG, "Preloaded " + messages.size() + " messages");
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Message>> call, @NonNull Throwable t) {
                        Log.w(TAG, "Background preload failed: " + t.getMessage());
                        notifyCallbacksError(t.getMessage());
                    }
                });
    }
    
    private void updateCache(List<Message> messages) {
        synchronized (preloadedMessages) {
            // Clear old cache if too large
            if (preloadedMessages.size() > MAX_CACHE_SIZE) {
                preloadedMessages.clear();
            }
            
            // Update cache with smart merging
            for (Message newMessage : messages) {
                boolean found = false;
                for (int i = 0; i < preloadedMessages.size(); i++) {
                    if (preloadedMessages.get(i).getId().equals(newMessage.getId())) {
                        preloadedMessages.set(i, newMessage);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    preloadedMessages.add(0, newMessage);
                }
            }
            
            // Keep cache size manageable
            while (preloadedMessages.size() > MAX_CACHE_SIZE) {
                preloadedMessages.remove(preloadedMessages.size() - 1);
            }
        }
    }
    
    public List<Message> getCachedMessages() {
        synchronized (preloadedMessages) {
            return new ArrayList<>(preloadedMessages);
        }
    }
    
    public boolean hasCachedData() {
        return !preloadedMessages.isEmpty();
    }
    
    public void addPreloadCallback(PreloadCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }
    
    public void removePreloadCallback(PreloadCallback callback) {
        callbacks.remove(callback);
    }
    
    private void notifyCallbacks(List<Message> messages) {
        for (PreloadCallback callback : callbacks) {
            try {
                callback.onPreloadComplete(messages);
            } catch (Exception e) {
                Log.e(TAG, "Error in preload callback", e);
            }
        }
    }
    
    private void notifyCallbacksError(String error) {
        for (PreloadCallback callback : callbacks) {
            try {
                callback.onPreloadFailed(error);
            } catch (Exception e) {
                Log.e(TAG, "Error in preload error callback", e);
            }
        }
    }
    
    public void setPreloadingEnabled(boolean enabled) {
        isPreloadingEnabled = enabled;
        if (enabled) {
            startPreloading();
        } else {
            stopPreloading();
        }
    }
    
    public void clearCache() {
        synchronized (preloadedMessages) {
            preloadedMessages.clear();
        }
        Log.d(TAG, "Cache cleared");
    }
    
    // Method to force immediate preload
    public void forcePreload() {
        if (authManager.isLoggedIn()) {
            performBackgroundPreload();
        }
    }
} 