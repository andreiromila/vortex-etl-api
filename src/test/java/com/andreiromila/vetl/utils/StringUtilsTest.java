package com.andreiromila.vetl.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    /**
     * Parameterized test to verify the filename sanitization logic.
     * It covers various edge cases like spaces, special characters, accents, and casing.
     *
     * @param input    The original filename to be sanitized.
     * @param expected The expected URL-safe output.
     */
    @ParameterizedTest(name = "Sanitizing \"{0}\" should result in \"{1}\"")
    @CsvSource({
            "'uifaces-popular-avatar (6).jpg', 'uifaces-popular-avatar-6.jpg'",
            "'My Document With Spaces.pdf',   'my-document-with-spaces.pdf'",
            "'Cañón_español.jpeg',            'canon_espanol.jpeg'",
            "'file@#$!%^&*.txt',               'file.txt'",
            "' leading-and-trailing-.zip ',   'leading-and-trailing-.zip'",
            "'image_with_underscores_1.png',  'image_with_underscores_1.png'",
            "'UpperCaseFile.SVG',             'uppercasefile.svg'"
    })
    void sanitizeFilename(String input, String expected) {
        // When
        String result = StringUtils.sanitizeFilename(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
