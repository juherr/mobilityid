package dev.juherr.mobilityid4j;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ISO 3166-1 alpha-2 country code.
 *
 * @param value normalized uppercase country code value
 */
public record CountryCode(String value) {
    private static final Pattern PATTERN = Pattern.compile("[A-Za-z]{2}");
    private static final Set<String> ISO_COUNTRIES =
            Arrays.stream(Locale.getISOCountries()).collect(Collectors.toUnmodifiableSet());

    /**
     * Creates and validates a country code.
     *
     * @param value candidate country code
     */
    public CountryCode {
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Country Code must be valid according to ISO 3166-1 alpha-2");
        }
        var normalized = value.toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRIES.contains(normalized)) {
            throw new IllegalArgumentException("Country Code must be valid according to ISO 3166-1 alpha-2");
        }
        value = normalized;
    }

    /**
     * Creates and validates a country code.
     *
     * @param value candidate country code
     * @return validated country code
     */
    public static CountryCode of(String value) {
        return new CountryCode(value);
    }

    /**
     * Parses a country code.
     *
     * @param value candidate country code
     * @return validated country code, or empty when invalid
     */
    public static Optional<CountryCode> parse(String value) {
        try {
            return Optional.of(of(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
