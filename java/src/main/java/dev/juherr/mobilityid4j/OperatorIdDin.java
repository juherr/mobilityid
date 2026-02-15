package dev.juherr.mobilityid4j;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * DIN operator identifier.
 *
 * @param id normalized operator ID value
 */
public record OperatorIdDin(String id) implements OperatorId {
    private static final Pattern PATTERN = Pattern.compile("[0-9]{3,6}");

    /**
     * Creates and validates a DIN operator identifier.
     */
    public OperatorIdDin {
        if (!PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("OperatorId must have a length of 3-6 chars and be digits");
        }
    }

    /**
     * Creates and validates a DIN operator identifier.
     *
     * @param id candidate operator ID
     * @return validated operator ID
     */
    public static OperatorIdDin of(String id) {
        return new OperatorIdDin(id);
    }

    /**
     * Parses a DIN operator identifier.
     *
     * @param id candidate operator ID
     * @return validated operator ID, or empty when invalid
     */
    public static Optional<OperatorIdDin> parse(String id) {
        try {
            return Optional.of(of(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
