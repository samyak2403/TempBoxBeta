package com.samyak.tempboxbeta.utils;

import java.security.SecureRandom;

/**
 * Utility class for generating random data such as usernames and passwords.
 */
public class GeneratorUtils {

    private static final String EMAIL_CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String EMAIL_CHAR_NUMBERS = "0123456789";
    private static final String EMAIL_CHAR_SET = EMAIL_CHAR_LOWER + EMAIL_CHAR_NUMBERS;
    private static final int USERNAME_LENGTH = 10;

    private static final String PASSWORD_CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String PASSWORD_CHAR_UPPER = PASSWORD_CHAR_LOWER.toUpperCase();
    private static final String PASSWORD_CHAR_NUMBERS = "0123456789";
    private static final String PASSWORD_CHAR_SYMBOLS = "!@#$%^&*()_+-=[]{}|;':,./<>?";
    private static final String PASSWORD_CHAR_SET = PASSWORD_CHAR_LOWER + PASSWORD_CHAR_UPPER + PASSWORD_CHAR_NUMBERS + PASSWORD_CHAR_SYMBOLS;
    private static final int DEFAULT_PASSWORD_LENGTH = 12;

    private static final SecureRandom random = new SecureRandom();

    private GeneratorUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a random username with a mix of lowercase letters and numbers.
     *
     * @param length The length of the username to generate.
     * @return A random username string.
     */
    public static String generateRandomUsername(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(EMAIL_CHAR_SET.length());
            sb.append(EMAIL_CHAR_SET.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * Generates a random username with default length.
     *
     * @return A random username string.
     */
    public static String generateRandomUsername() {
        return generateRandomUsername(USERNAME_LENGTH);
    }

    /**
     * Generates a random email address with a default username length and a given domain.
     *
     * @param domain The domain for the email address (e.g., "tempbox.com").
     * @return A randomly generated email address.
     */
    public static String generateRandomEmailAddress(String domain) {
        String username = generateRandomUsername(USERNAME_LENGTH);
        return username + "@" + domain;
    }

    /**
     * Generates a secure random password with a mix of uppercase letters, lowercase letters, numbers, and symbols.
     *
     * @param length The desired length of the password.
     * @return A randomly generated password.
     */
    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHAR_SET.length());
            password.append(PASSWORD_CHAR_SET.charAt(randomIndex));
        }
        return password.toString();
    }

    /**
     * Generates a secure random password with the default length.
     *
     * @return A randomly generated password.
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Generates a secure password with enhanced security features.
     * Ensures at least one character from each character set.
     *
     * @return A secure randomly generated password.
     */
    public static String generateSecurePassword() {
        return generateSecurePassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Generates a secure password with enhanced security features.
     * Ensures at least one character from each character set.
     *
     * @param length The desired length of the password.
     * @return A secure randomly generated password.
     */
    public static String generateSecurePassword(int length) {
        if (length < 4) {
            // Minimum length to ensure at least one character from each set
            length = 4;
        }
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one character from each set
        password.append(PASSWORD_CHAR_LOWER.charAt(random.nextInt(PASSWORD_CHAR_LOWER.length())));
        password.append(PASSWORD_CHAR_UPPER.charAt(random.nextInt(PASSWORD_CHAR_UPPER.length())));
        password.append(PASSWORD_CHAR_NUMBERS.charAt(random.nextInt(PASSWORD_CHAR_NUMBERS.length())));
        password.append(PASSWORD_CHAR_SYMBOLS.charAt(random.nextInt(PASSWORD_CHAR_SYMBOLS.length())));
        
        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHAR_SET.length());
            password.append(PASSWORD_CHAR_SET.charAt(randomIndex));
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }

    /**
     * Shuffles the characters in a string.
     *
     * @param string The string to shuffle.
     * @return The shuffled string.
     */
    private static String shuffleString(String string) {
        char[] characters = string.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }
}
