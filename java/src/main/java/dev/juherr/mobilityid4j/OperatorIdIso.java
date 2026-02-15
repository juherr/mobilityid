package dev.juherr.mobilityid4j;

import java.util.Optional;

/**
 * ISO operator identifier.
 *
 * @param id normalized operator ID value
 */
public record OperatorIdIso(String id) implements OperatorId {
    /**
     * Creates and validates an ISO operator identifier.
     */
    public OperatorIdIso {
        id = new PartyCode(id).value();
    }

    /**
     * Creates and validates an ISO operator identifier.
     *
     * @param id candidate operator ID
     * @return validated operator ID
     */
    public static OperatorIdIso of(String id) {
        return new OperatorIdIso(id);
    }

    /**
     * Parses an ISO operator identifier.
     *
     * @param id candidate operator ID
     * @return validated operator ID, or empty when invalid
     */
    public static Optional<OperatorIdIso> parse(String id) {
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
