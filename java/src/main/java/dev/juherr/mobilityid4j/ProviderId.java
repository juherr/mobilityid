package dev.juherr.mobilityid4j;

import java.util.Optional;

/**
 * Provider identifier.
 *
 * @param id normalized provider ID value
 */
public record ProviderId(String id) {
    /**
     * Creates and validates a provider identifier.
     */
    public ProviderId {
        id = new PartyCode(id).value();
    }

    /**
     * Creates and validates a provider identifier.
     *
     * @param id candidate provider ID
     * @return validated provider ID
     */
    public static ProviderId of(String id) {
        return new ProviderId(id);
    }

    /**
     * Parses a provider identifier.
     *
     * @param id candidate provider ID
     * @return validated provider ID, or empty when invalid
     */
    public static Optional<ProviderId> parse(String id) {
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
