package com.samyak.tempboxbeta;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.samyak.tempboxbeta.fragments.AccountFragment;
import com.samyak.tempboxbeta.fragments.CreateAccountFragment;
import com.samyak.tempboxbeta.fragments.InboxFragment;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.Constants;
import com.samyak.tempboxbeta.utils.PreloadManager;

public class MainActivity extends AppCompatActivity implements 
        CreateAccountFragment.OnAccountCreatedListener,
        AccountFragment.OnAccountActionListener {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private AuthManager authManager;
    private PreloadManager preloadManager;
    
    // Fragment instances
    private InboxFragment inboxFragment;
    private CreateAccountFragment createAccountFragment;
    private AccountFragment accountFragment;
    
    // Flag to prevent infinite recursion
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        authManager = AuthManager.getInstance(this);
        preloadManager = PreloadManager.getInstance(this);
        fragmentManager = getSupportFragmentManager();
        
        // Start background preloading for super fast data loading
        if (authManager.isLoggedIn()) {
            preloadManager.startPreloading();
        }
        
        initViews();
        setupBottomNavigation();
        
        // Set initial fragment
        if (savedInstanceState == null) {
            showInboxFragment();
        }
        
        // Setup modern back press handling
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If not on inbox fragment, navigate to inbox
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
                if (currentFragment != inboxFragment) {
                    showInboxFragment();
                } else {
                    // Exit the app
                    finish();
                }
            }
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            // Prevent infinite recursion
            if (isNavigating) {
                return true;
            }
            
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_inbox) {
                showInboxFragmentInternal();
                return true;
            } else if (itemId == R.id.nav_create) {
                showCreateAccountFragmentInternal();
                return true;
            } else if (itemId == R.id.nav_account) {
                showAccountFragmentInternal();
                return true;
            }
            
            return false;
        });
    }
    
    private void showInboxFragment() {
        isNavigating = true;
        if (inboxFragment == null) {
            inboxFragment = InboxFragment.newInstance();
        }
        
        showFragment(inboxFragment, Constants.FRAGMENT_INBOX);
        bottomNavigation.setSelectedItemId(R.id.nav_inbox);
        isNavigating = false;
    }
    
    private void showInboxFragmentInternal() {
        if (inboxFragment == null) {
            inboxFragment = InboxFragment.newInstance();
        }
        
        showFragment(inboxFragment, Constants.FRAGMENT_INBOX);
    }
    
    private void showCreateAccountFragment() {
        isNavigating = true;
        if (createAccountFragment == null) {
            createAccountFragment = CreateAccountFragment.newInstance();
            createAccountFragment.setOnAccountCreatedListener(this);
        }
        
        showFragment(createAccountFragment, Constants.FRAGMENT_CREATE_ACCOUNT);
        bottomNavigation.setSelectedItemId(R.id.nav_create);
        isNavigating = false;
    }
    
    private void showCreateAccountFragmentInternal() {
        if (createAccountFragment == null) {
            createAccountFragment = CreateAccountFragment.newInstance();
            createAccountFragment.setOnAccountCreatedListener(this);
        }
        
        showFragment(createAccountFragment, Constants.FRAGMENT_CREATE_ACCOUNT);
    }
    
    private void showAccountFragment() {
        isNavigating = true;
        if (accountFragment == null) {
            accountFragment = AccountFragment.newInstance();
            accountFragment.setOnAccountActionListener(this);
        }
        
        showFragment(accountFragment, Constants.FRAGMENT_ACCOUNT);
        bottomNavigation.setSelectedItemId(R.id.nav_account);
        isNavigating = false;
    }
    
    private void showAccountFragmentInternal() {
        if (accountFragment == null) {
            accountFragment = AccountFragment.newInstance();
            accountFragment.setOnAccountActionListener(this);
        }
        
        showFragment(accountFragment, Constants.FRAGMENT_ACCOUNT);
    }
    
    private void showFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide all fragments
        if (inboxFragment != null) {
            transaction.hide(inboxFragment);
        }
        if (createAccountFragment != null) {
            transaction.hide(createAccountFragment);
        }
        if (accountFragment != null) {
            transaction.hide(accountFragment);
        }
        
        // Show or add the target fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        
        transaction.commit();
    }
    
    @Override
    public void onAccountCreated() {
        // Account was created successfully, switch to inbox
        showInboxFragment();
        
        // Start background preloading for the new account
        preloadManager.startPreloading();
        preloadManager.forcePreload(); // Immediate preload
        
        // Refresh inbox to show the new account
        if (inboxFragment != null) {
            inboxFragment.refreshMessages();
        }
    }
    
    @Override
    public void onAccountDeleted() {
        // Account was deleted, refresh all fragments
        refreshAllFragments();
    }
    
    @Override
    public void onLogout() {
        // User logged out, stop preloading and clear cache
        preloadManager.stopPreloading();
        preloadManager.clearCache();
        
        // Refresh all fragments
        refreshAllFragments();
    }
    
    @Override
    public void onNavigateToCreate() {
        showCreateAccountFragment();
    }
    
    private void refreshAllFragments() {
        // Refresh inbox fragment
        if (inboxFragment != null) {
            inboxFragment.refreshMessages();
        }
    }
    
    // Back press handling is now done in onCreate with OnBackPressedDispatcher
}