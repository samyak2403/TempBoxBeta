package com.samyak.tempboxbeta.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DateUtils using traditional Java Date/Calendar API for F-Droid compatibility
 * 
 * Supports common date formatting patterns without requiring java.time API
 */
public class DateUtils {
    
    // ISO-8601 format for storage and APIs (best practice)
    private static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ISO_DATE_TIME_FORMAT_ALT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    // Display patterns
    private static final String PATTERN_ISO_DATE = "yyyy-MM-dd";                    // 2023-12-31
    private static final String PATTERN_SLASH_DATE = "dd/MM/yyyy";                  // 31/12/2023
    private static final String PATTERN_READABLE_DATE = "MMM dd, yyyy";             // Dec 31, 2023
    private static final String PATTERN_FULL_DATETIME = "yyyy-MM-dd HH:mm:ss";      // 2023-12-31 23:59:59
    private static final String PATTERN_TIME_12H = "hh:mm a";                       // 11:59 PM
    private static final String PATTERN_SHORT_DATE = "EEE, MMM d, ''yy";            // Sun, Dec 31, '23
    private static final String PATTERN_DISPLAY_DATETIME = "MMM dd, yyyy HH:mm";    // Dec 31, 2023 23:59
    private static final String PATTERN_TIME_24H = "HH:mm";                         // 23:59
    
    // Thread-safe formatters
    private static final SimpleDateFormat ISO_FORMATTER = new SimpleDateFormat(ISO_DATE_TIME_FORMAT, Locale.ENGLISH);
    private static final SimpleDateFormat ISO_FORMATTER_ALT = new SimpleDateFormat(ISO_DATE_TIME_FORMAT_ALT, Locale.ENGLISH);
    
    // Display formatters
    private static final SimpleDateFormat READABLE_DATE_FORMATTER = new SimpleDateFormat(PATTERN_READABLE_DATE, Locale.getDefault());
    private static final SimpleDateFormat TIME_12H_FORMATTER = new SimpleDateFormat(PATTERN_TIME_12H, Locale.getDefault());
    private static final SimpleDateFormat TIME_24H_FORMATTER = new SimpleDateFormat(PATTERN_TIME_24H, Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATETIME_FORMATTER = new SimpleDateFormat(PATTERN_DISPLAY_DATETIME, Locale.getDefault());
    private static final SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat(PATTERN_SHORT_DATE, Locale.getDefault());
    private static final SimpleDateFormat SLASH_DATE_FORMATTER = new SimpleDateFormat(PATTERN_SLASH_DATE, Locale.getDefault());
    private static final SimpleDateFormat ISO_DATE_FORMATTER = new SimpleDateFormat(PATTERN_ISO_DATE, Locale.getDefault());
    private static final SimpleDateFormat FULL_DATETIME_FORMATTER = new SimpleDateFormat(PATTERN_FULL_DATETIME, Locale.getDefault());
    
    static {
        // Set UTC timezone for ISO formatters
        ISO_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_FORMATTER_ALT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * Parse ISO date string to Date object
     */
    private static Date parseIsoDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try primary ISO format first
            synchronized (ISO_FORMATTER) {
                return ISO_FORMATTER.parse(isoDate);
            }
        } catch (ParseException e1) {
            try {
                // Try alternative ISO format
                synchronized (ISO_FORMATTER_ALT) {
                    return ISO_FORMATTER_ALT.parse(isoDate);
                }
            } catch (ParseException e2) {
                // If both fail, return null
                return null;
            }
        }
    }
    
    /**
     * Format date as readable format: "Dec 31, 2023"
     */
    public static String formatDisplayDate(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (READABLE_DATE_FORMATTER) {
                return READABLE_DATE_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format time in 24-hour format: "23:59"
     */
    public static String formatDisplayTime(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (TIME_24H_FORMATTER) {
                return TIME_24H_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format time in 12-hour format: "11:59 PM"
     */
    public static String formatDisplayTime12H(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (TIME_12H_FORMATTER) {
                return TIME_12H_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format full date and time: "Dec 31, 2023 23:59"
     */
    public static String formatDisplayDateTime(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (DISPLAY_DATETIME_FORMATTER) {
                return DISPLAY_DATETIME_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as short date: "Sun, Dec 31, '23"
     */
    public static String formatShortDate(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (SHORT_DATE_FORMATTER) {
                return SHORT_DATE_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as slash-separated date: "31/12/2023"
     */
    public static String formatSlashDate(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (SLASH_DATE_FORMATTER) {
                return SLASH_DATE_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as ISO date: "2023-12-31"
     */
    public static String formatIsoDate(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (ISO_DATE_FORMATTER) {
                return ISO_DATE_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format as full datetime: "2023-12-31 23:59:59"
     */
    public static String formatFullDateTime(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            synchronized (FULL_DATETIME_FORMATTER) {
                return FULL_DATETIME_FORMATTER.format(date);
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Format date as a relative time string (e.g., "2h ago", "Yesterday").
     */
    public static String formatRelative(String isoDate) {
        return getRelativeTimeString(isoDate);
    }

    /**
     * Get relative time string with smart formatting
     */
    public static String getRelativeTimeString(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            Date now = new Date();
            long diffMillis = now.getTime() - date.getTime();
            
            long minutes = diffMillis / (60 * 1000);
            long hours = diffMillis / (60 * 60 * 1000);
            long days = diffMillis / (24 * 60 * 60 * 1000);
            
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
     * Check if the given date is today
     */
    public static boolean isToday(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            
            Calendar today = Calendar.getInstance();
            
            return dateCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                   dateCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        }
        return false;
    }
    
    /**
     * Check if the given date is yesterday
     */
    public static boolean isYesterday(String isoDate) {
        Date date = parseIsoDate(isoDate);
        if (date != null) {
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            
            return dateCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                   dateCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR);
        }
        return false;
    }
    
    /**
     * Smart date formatting: shows relative time for recent dates, formatted date for older ones
     */
    public static String formatSmartDate(String isoDate) {
        if (isToday(isoDate)) {
            return formatDisplayTime(isoDate);
        } else if (isYesterday(isoDate)) {
            return "Yesterday";
        } else {
            Date date = parseIsoDate(isoDate);
            if (date != null) {
                Date now = new Date();
                long diffMillis = now.getTime() - date.getTime();
                long days = diffMillis / (24 * 60 * 60 * 1000);
                
                if (days < 7) {
                    return formatShortDate(isoDate);
                } else {
                    return formatDisplayDate(isoDate);
                }
            }
        }
        return isoDate; // Fallback to original
    }
    
    /**
     * Get current timestamp in ISO format
     */
    public static String getCurrentIsoTimestamp() {
        synchronized (ISO_FORMATTER) {
            return ISO_FORMATTER.format(new Date());
        }
    }
    
    private DateUtils() {
        // Utility class
    }
} 