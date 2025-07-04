package com.samyak.tempboxbeta;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.samyak.tempboxbeta.fragments.AccountFragment;
import com.samyak.tempboxbeta.fragments.CreateAccountFragment;
import com.samyak.tempboxbeta.utils.AuthManager;
import com.samyak.tempboxbeta.utils.AutoEmailGenerator;
import com.samyak.tempboxbeta.utils.PreloadManager;

public class MainActivity extends AppCompatActivity implements 
        CreateAccountFragment.OnAccountCreatedListener,
        AccountFragment.OnAccountActionListener {

    private BottomNavigationView bottomNavigation;
    private NavController navController;
    private AuthManager authManager;
    private PreloadManager preloadManager;
    private AutoEmailGenerator autoEmailGenerator;
    private boolean navigationSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        authManager = AuthManager.getInstance(this);
        preloadManager = PreloadManager.getInstance(this);
        autoEmailGenerator = new AutoEmailGenerator(this);
        
        // Start background preloading for super fast data loading
        if (authManager.isLoggedIn()) {
            preloadManager.startPreloading();
        }
        
        initViews();
        
        // Check if we need to auto-generate an email
        checkAndGenerateAutoEmail();
        
        // Setup modern back press handling
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navController != null) {
                    // If not on inbox fragment, navigate to inbox
                    if (navController.getCurrentDestination() != null && 
                        navController.getCurrentDestination().getId() != R.id.inboxFragment) {
                        navController.navigate(R.id.inboxFragment);
                    } else {
                        // Exit the app
                        finish();
                    }
                } else {
                    // Fallback: just exit
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Setup navigation after the view is fully created
        if (!navigationSetup) {
            setupNavigation();
        }
    }
    
    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }
    
    private void setupNavigation() {
        try {
            // Get NavHostFragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            
            if (navHostFragment != null) {
                // Get NavController from NavHostFragment
                navController = navHostFragment.getNavController();
                
                // Setup BottomNavigationView with NavController
                NavigationUI.setupWithNavController(bottomNavigation, navController);
                
                // Set up fragments' listeners when they are created
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    // Find and set up fragment listeners when destinations change
                    setupFragmentListeners();
                });
                
                navigationSetup = true;
            }
        } catch (Exception e) {
            // Fallback: try using Navigation.findNavController
            try {
                navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                NavigationUI.setupWithNavController(bottomNavigation, navController);
                
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    setupFragmentListeners();
                });
                
                navigationSetup = true;
            } catch (Exception fallbackException) {
                // If navigation setup fails, we'll handle navigation manually
                setupManualNavigation();
            }
        }
    }
    
    private void setupManualNavigation() {
        // Fallback to manual navigation if NavController setup fails
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.inboxFragment) {
                // Handle inbox navigation manually if needed
                return true;
            } else if (itemId == R.id.createAccountFragment) {
                // Handle create account navigation manually if needed
                return true;
            } else if (itemId == R.id.accountFragment) {
                // Handle account navigation manually if needed
                return true;
            }
            
            return false;
        });
    }
    
    private void setupFragmentListeners() {
        // Use a post to ensure fragments are fully created
        bottomNavigation.post(() -> {
            try {
                // Get current fragment
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                
                if (navHostFragment != null) {
                    androidx.fragment.app.Fragment fragment = 
                        navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                    
                    // Set up listeners based on fragment type
                    if (fragment instanceof CreateAccountFragment) {
                        ((CreateAccountFragment) fragment).setOnAccountCreatedListener(this);
                    } else if (fragment instanceof AccountFragment) {
                        ((AccountFragment) fragment).setOnAccountActionListener(this);
                    }
                }
            } catch (Exception e) {
                // Handle safely - fragments might not be ready yet
            }
        });
    }
    
    @Override
    public void onAccountCreated() {
        // Account was created successfully, switch to inbox
        if (navController != null) {
            try {
                navController.navigate(R.id.inboxFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
    }

    @Override
    public void onAccountDeleted() {
        // Account was deleted, refresh fragments and stay on account page
        refreshAllFragments();
    }

    @Override
    public void onLogout() {
        // User logged out, navigate to create account
        if (navController != null) {
            try {
                navController.navigate(R.id.createAccountFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
        refreshAllFragments();
    }

    @Override
    public void onNavigateToCreate() {
        // Navigate to create account fragment
        if (navController != null) {
            try {
                navController.navigate(R.id.createAccountFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
    }
    
    private void refreshAllFragments() {
        // This method refreshes all fragments by clearing the fragment manager
        // The navigation component will recreate them as needed
        try {
            getSupportFragmentManager().getFragments().clear();
        } catch (Exception e) {
            // Handle safely
        }
    }
    
    // Public methods for programmatic navigation
    public void navigateToInbox() {
        if (navController != null && navController.getCurrentDestination() != null && 
            navController.getCurrentDestination().getId() != R.id.inboxFragment) {
            try {
                navController.navigate(R.id.inboxFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
    }
    
    public void navigateToCreateAccount() {
        if (navController != null && navController.getCurrentDestination() != null && 
            navController.getCurrentDestination().getId() != R.id.createAccountFragment) {
            try {
                navController.navigate(R.id.createAccountFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
    }
    
    public void navigateToAccount() {
        if (navController != null && navController.getCurrentDestination() != null && 
            navController.getCurrentDestination().getId() != R.id.accountFragment) {
            try {
                navController.navigate(R.id.accountFragment);
            } catch (Exception e) {
                // Handle navigation error gracefully
            }
        }
    }
    
    private void checkAndGenerateAutoEmail() {
        // Only generate auto email if:
        // 1. User is not already logged in
        // 2. Auto email hasn't been generated before
        if (!authManager.isLoggedIn() && !authManager.hasAutoEmailGenerated()) {
            android.util.Log.d("MainActivity", "Generating automatic temporary email...");
            
            autoEmailGenerator.generateAutoEmail(new AutoEmailGenerator.AutoEmailCallback() {
                @Override
                public void onSuccess(String emailAddress) {
                    android.util.Log.d("MainActivity", "Auto email generated: " + emailAddress);
                    
                    // Mark auto email as generated
                    authManager.setAutoEmailGenerated(true);
                    
                    // Navigate to inbox to show the new email
                    runOnUiThread(() -> {
                        if (navController != null) {
                            try {
                                navController.navigate(R.id.inboxFragment);
                            } catch (Exception e) {
                                // Handle navigation error gracefully
                            }
                        }
                        
                        // Show a toast to inform the user
                        android.widget.Toast.makeText(MainActivity.this, 
                            "Welcome! Your temporary email is ready: " + emailAddress, 
                            android.widget.Toast.LENGTH_LONG).show();
                    });
                }
                
                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("MainActivity", "Failed to generate auto email: " + errorMessage);
                    
                    runOnUiThread(() -> {
                        // If auto generation fails, show the create account screen
                        if (navController != null) {
                            try {
                                navController.navigate(R.id.createAccountFragment);
                            } catch (Exception e) {
                                // Handle navigation error gracefully
                            }
                        }
                        
                        android.widget.Toast.makeText(MainActivity.this, 
                            "Please create your temporary email manually", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
}