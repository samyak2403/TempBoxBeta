package com.samyak.tempboxbeta.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    private static final String DISPLAY_TIME_FORMAT = "HH:mm";
    private static final String DISPLAY_DATE_TIME_FORMAT = "MMM dd, yyyy HH:mm";
    
    private static final SimpleDateFormat isoFormatter;
    private static final SimpleDateFormat displayDateFormatter;
    private static final SimpleDateFormat displayTimeFormatter;
    private static final SimpleDateFormat displayDateTimeFormatter;
    
    static {
        isoFormatter = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault());
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        displayDateFormatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
        displayTimeFormatter = new SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault());
        displayDateTimeFormatter = new SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT, Locale.getDefault());
    }
    
    public static String formatDisplayDate(String isoDate) {
        try {
            Date date = isoFormatter.parse(isoDate);
            if (date != null) {
                return displayDateFormatter.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoDate; // Return original if parsing fails
    }
    
    public static String formatDisplayTime(String isoDate) {
        try {
            Date date = isoFormatter.parse(isoDate);
            if (date != null) {
                return displayTimeFormatter.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoDate; // Return original if parsing fails
    }
    
    public static String formatDisplayDateTime(String isoDate) {
        try {
            Date date = isoFormatter.parse(isoDate);
            if (date != null) {
                return displayDateTimeFormatter.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoDate; // Return original if parsing fails
    }
    
    public static String getRelativeTimeString(String isoDate) {
        try {
            Date date = isoFormatter.parse(isoDate);
            if (date != null) {
                long now = System.currentTimeMillis();
                long diff = now - date.getTime();
                
                if (diff < 60000) { // Less than 1 minute
                    return "Just now";
                } else if (diff < 3600000) { // Less than 1 hour
                    return (diff / 60000) + "m ago";
                } else if (diff < 86400000) { // Less than 1 day
                    return (diff / 3600000) + "h ago";
                } else if (diff < 604800000) { // Less than 1 week
                    return (diff / 86400000) + "d ago";
                } else {
                    return formatDisplayDate(isoDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoDate; // Return original if parsing fails
    }
    
    private DateUtils() {
        // Private constructor to prevent instantiation
    }
} 