package com.samyak.tempboxbeta.utils;

public class Constants {
    
    // API Constants
    public static final String BASE_URL = "https://api.mail.tm/";
    public static final int REQUEST_TIMEOUT = 30; // seconds
    public static final int ITEMS_PER_PAGE = 30;
    
    // Refresh intervals
    public static final int AUTO_REFRESH_INTERVAL = 30000; // 30 seconds
    public static final int MANUAL_REFRESH_COOLDOWN = 2000; // 2 seconds
    
    // Fragment tags
    public static final String FRAGMENT_INBOX = "inbox_fragment";
    public static final String FRAGMENT_CREATE_ACCOUNT = "create_account_fragment";
    public static final String FRAGMENT_ACCOUNT = "account_fragment";
    public static final String FRAGMENT_MESSAGE_DETAIL = "message_detail_fragment";
    
    // Intent extras
    public static final String EXTRA_MESSAGE_ID = "message_id";
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    
    // Error messages
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_SERVER = "Server error. Please try again later.";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String ERROR_ACCOUNT_EXISTS = "Account already exists.";
    public static final String ERROR_UNKNOWN = "An unknown error occurred.";
    
    // Success messages
    public static final String SUCCESS_ACCOUNT_CREATED = "Account created successfully!";
    public static final String SUCCESS_MESSAGE_DELETED = "Message deleted successfully.";
    public static final String SUCCESS_ACCOUNT_DELETED = "Account deleted successfully.";
    
    // Email validation
    public static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    // Password requirements
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
} 