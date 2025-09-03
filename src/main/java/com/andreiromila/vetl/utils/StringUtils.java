package com.andreiromila.vetl.utils;

import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * Simple string utility class
 */
public class StringUtils {

    /**
     * Secure random instance
     */
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Allowed characters for the random string generator
     */
    private static final String[] chars = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    /**
     * Generates a secure random string
     *
     * @param size {@link Integer} The size of the string
     *
     * @return The random generated string
     */
    public static String generateRandomString(int size) {
        return secureRandom.ints(0, chars.length)
                .limit(size)
                .mapToObj(index -> chars[index])
                .collect(Collectors.joining());
    }

}
