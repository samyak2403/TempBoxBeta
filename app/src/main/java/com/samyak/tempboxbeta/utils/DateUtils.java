package com.samyak.tempboxbeta.utils;

import android.os.Build;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Modern DateUtils using java.time API with recommended patterns and best practices
 * 
 * Recommended Patterns Supported:
 * - yyyy-MM-dd (2023-12-31)
 * - dd/MM/yyyy (31/12/2023) 
 * - MMM dd, yyyy (Dec 31, 2023)
 * - yyyy-MM-dd HH:mm:ss (2023-12-31 23:59:59)
 * - hh:mm a (11:59 PM)
 * - EEE, MMM d, ''yy (Sun, Dec 31, '23)
 */
public class DateUtils {
    
    // ISO-8601 format for storage and APIs (best practice)
    private static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ISO_DATE_TIME_FORMAT_ALT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    // Recommended display patterns
    private static final String PATTERN_ISO_DATE = "yyyy-MM-dd";                    // 2023-12-31
    private static final String PATTERN_SLASH_DATE = "dd/MM/yyyy";                  // 31/12/2023
    private static final String PATTERN_READABLE_DATE = "MMM dd, yyyy";             // Dec 31, 2023
    private static final String PATTERN_FULL_DATETIME = "yyyy-MM-dd HH:mm:ss";      // 2023-12-31 23:59:59
    private static final String PATTERN_TIME_12H = "hh:mm a";                       // 11:59 PM
    private static final String PATTERN_SHORT_DATE = "EEE, MMM d, ''yy";            // Sun, Dec 31, '23
    private static final String PATTERN_DISPLAY_DATETIME = "MMM dd, yyyy HH:mm";    // Dec 31, 2023 23:59
    private static final String PATTERN_TIME_24H = "HH:mm";                         // 23:59
    
    // Modern formatters using java.time (thread-safe)
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT, Locale.ENGLISH);
    private static final DateTimeFormatter ISO_FORMATTER_ALT = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT_ALT, Locale.ENGLISH);
    
    // Display formatters with explicit locale (best practice)
    private static final DateTimeFormatter READABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_READABLE_DATE, Locale.getDefault());
    private static final DateTimeFormatter TIME_12H_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_TIME_12H, Locale.getDefault());
    private static final DateTimeFormatter TIME_24H_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_TIME_24H, Locale.getDefault());
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DISPLAY_DATETIME, Locale.getDefault());
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_SHORT_DATE, Locale.getDefault());
    private static final DateTimeFormatter SLASH_DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_SLASH_DATE, Locale.getDefault());
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_ISO_DATE, Locale.getDefault());
    private static final DateTimeFormatter FULL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_FULL_DATETIME, Locale.getDefault());
    
    /**
     * Parse ISO date string to ZonedDateTime with timezone consideration
     */
    private static ZonedDateTime parseIsoDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try primary ISO format first
            Instant instant = Instant.from(ISO_FORMATTER.parse(isoDate));
            return instant.atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e1) {
            try {
                // Try alternative ISO format
                Instant instant = Instant.from(ISO_FORMATTER_ALT.parse(isoDate));
                return instant.atZone(ZoneId.systemDefault());
            } catch (DateTimeParseException e2) {
                // If both fail, return null
                return null;
            }
        }
    }
    
    /**
     * Format date as readable format: "Dec 31, 2023"
     */
    public static String formatDisplayDate(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(READABLE_DATE_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format time in 24-hour format: "23:59"
     */
    public static String formatDisplayTime(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(TIME_24H_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format time in 12-hour format: "11:59 PM"
     */
    public static String formatDisplayTime12H(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(TIME_12H_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format full date and time: "Dec 31, 2023 23:59"
     */
    public static String formatDisplayDateTime(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(DISPLAY_DATETIME_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as short date: "Sun, Dec 31, '23"
     */
    public static String formatShortDate(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(SHORT_DATE_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as slash-separated date: "31/12/2023"
     */
    public static String formatSlashDate(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(SLASH_DATE_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as ISO date: "2023-12-31"
     */
    public static String formatIsoDate(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(ISO_DATE_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as full datetime: "2023-12-31 23:59:59"
     */
    public static String formatFullDateTime(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            return dateTime.format(FULL_DATETIME_FORMATTER);
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Get relative time string with smart formatting
     * Uses modern java.time for precise calculations
     */
        /**
     * Format date as a relative time string (e.g., "2h ago", "Yesterday").
     * This is a convenience wrapper around getRelativeTimeString.
     */
    public static String formatRelative(String isoDate) {
        return getRelativeTimeString(isoDate);
    }

    public static String getRelativeTimeString(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            ZonedDateTime now = ZonedDateTime.now(dateTime.getZone());
            
            long minutes = ChronoUnit.MINUTES.between(dateTime, now);
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            long days = ChronoUnit.DAYS.between(dateTime, now);
            
            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else {
                // For older dates, use readable format
                return formatDisplayDate(isoDate);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Check if date is today
     */
    public static boolean isToday(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            ZonedDateTime now = ZonedDateTime.now(dateTime.getZone());
            return dateTime.toLocalDate().equals(now.toLocalDate());
        }
        return false;
    }
    
    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(String isoDate) {
        ZonedDateTime dateTime = parseIsoDate(isoDate);
        if (dateTime != null) {
            ZonedDateTime now = ZonedDateTime.now(dateTime.getZone());
            ZonedDateTime yesterday = now.minusDays(1);
            return dateTime.toLocalDate().equals(yesterday.toLocalDate());
        }
        return false;
    }
    
    /**
     * Smart date formatting for message lists
     * - Today: "HH:mm"
     * - Yesterday: "Yesterday"
     * - This week: "EEE" (Mon, Tue, etc.)
     * - Older: "MMM dd" (Dec 31)
     */
    public static String formatSmartDate(String isoDate) {
        if (isToday(isoDate)) {
            return formatDisplayTime(isoDate);
        } else if (isYesterday(isoDate)) {
            return "Yesterday";
        } else {
            ZonedDateTime dateTime = parseIsoDate(isoDate);
            if (dateTime != null) {
                ZonedDateTime now = ZonedDateTime.now(dateTime.getZone());
                long days = ChronoUnit.DAYS.between(dateTime, now);
                
                if (days < 7) {
                    // This week - show day name
                    return dateTime.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()));
                } else {
                    // Older - show month and day
                    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault()));
                }
            }
        }
        return isoDate; // Fallback
    }
    
    /**
     * Get current timestamp in ISO format (for API calls)
     */
    public static String getCurrentIsoTimestamp() {
        return Instant.now().toString();
    }
    
    private DateUtils() {
        // Private constructor to prevent instantiation
    }
} 