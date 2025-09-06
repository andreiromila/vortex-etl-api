package com.andreiromila.vetl.utils;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
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
     *
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w.-]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

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

    /**
     * Cleans and sanitizes a filename to make it URL-safe.
     * <p>
     * This process involves:
     * - Replacing whitespace with hyphens.
     * - Removing all characters that are not alphanumeric, hyphens, or dots.
     * - Converting to lowercase.
     *
     * @param filename {@link String} The original filename.
     * @return A URL-safe "slugified" version of the filename.
     */
    public static String sanitizeFilename(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        final String noWhitespace = WHITESPACE.matcher(filename).replaceAll("-");
        final String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        final String slug = NON_LATIN.matcher(normalized).replaceAll("");

        return EDGES_DASHES.matcher(slug)
                    .replaceAll("")
                    .toLowerCase(Locale.ENGLISH);
    }
}
