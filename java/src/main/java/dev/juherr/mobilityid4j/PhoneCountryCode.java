package dev.juherr.mobilityid4j;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Phone country code used by DIN EVSE identifiers.
 *
 * @param value normalized phone country code with leading {@code +}
 */
public record PhoneCountryCode(String value) {
    private static final Pattern PATTERN = Pattern.compile("\\+?([0-9]{1,3})");

    /**
     * Creates and validates a phone country code.
     */
    public PhoneCountryCode {
        var matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "phone Country Code must start with a '+' sign and be followed by 1-3 digits. (Was: "
                            + value
                            + ")");
        }
        value = "+" + matcher.group(1);
    }

    /**
     * Creates and validates a phone country code.
     *
     * @param value candidate phone country code
     * @return validated phone country code
     */
    public static PhoneCountryCode of(String value) {
        return new PhoneCountryCode(value);
    }

    /**
     * Parses a phone country code.
     *
     * @param value candidate phone country code
     * @return validated phone country code, or empty when invalid
     */
    public static Optional<PhoneCountryCode> parse(String value) {
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
