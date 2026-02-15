package dev.juherr.mobilityid4j;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * EVSE identifier in ISO format.
 *
 * @param countryCode ISO country code
 * @param operatorId ISO operator identifier
 * @param powerOutletId outlet identifier fragment without leading {@code E}
 */
public record EvseIdIso(CountryCode countryCode, OperatorIdIso operatorId, String powerOutletId) implements EvseId {
    private static final Pattern POWER_OUTLET = Pattern.compile("[A-Za-z0-9*]{1,31}");
    private static final Pattern FULL = Pattern.compile("^([A-Za-z]{2})\\*?([A-Za-z0-9]{3})\\*?E([A-Za-z0-9*]{1,31})$");

    /**
     * Creates and validates an ISO EVSE identifier.
     */
    public EvseIdIso {
        var normalizedPower = powerOutletId.toUpperCase(Locale.ROOT);
        if (!POWER_OUTLET.matcher(normalizedPower).matches()) {
            throw new IllegalArgumentException("Invalid powerOutletId for ISO format");
        }
        powerOutletId = normalizedPower;
    }

    /**
     * Creates an ISO EVSE identifier from parts.
     *
     * @param countryCode ISO country code
     * @param operatorId ISO operator identifier
     * @param powerOutletId outlet identifier fragment
     * @return validated ISO EVSE identifier
     */
    public static EvseIdIso of(String countryCode, String operatorId, String powerOutletId) {
        var error = validate(countryCode, operatorId, powerOutletId);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.orElseThrow().description());
        }
        return new EvseIdIso(CountryCode.of(countryCode), OperatorIdIso.of(operatorId), normalizePower(powerOutletId));
    }

    /**
     * Parses an ISO EVSE identifier.
     *
     * @param evseId raw EVSE ID
     * @return parsed ISO EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdIso> parse(String evseId) {
        var matcher = FULL.matcher(evseId);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(of(matcher.group(1), matcher.group(2), matcher.group(3)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    static Optional<EvseValidationError> validate(String countryCode, String operatorId, String powerOutletId) {
        var cc = countryCode.toUpperCase(Locale.ROOT);
        var op = operatorId.toUpperCase(Locale.ROOT);
        var po = normalizePower(powerOutletId);

        if (CountryCode.parse(cc).isEmpty()) {
            return Optional.of(new EvseValidationError(1, "Invalid countryCode for ISO or DIN format"));
        }
        if (OperatorIdIso.parse(op).isEmpty()) {
            return Optional.of(new EvseValidationError(2, "Invalid operatorId for ISO format"));
        }
        if (!POWER_OUTLET.matcher(po).matches()) {
            return Optional.of(new EvseValidationError(3, "Invalid powerOutletId for ISO format"));
        }
        return Optional.empty();
    }

    /**
     * Returns the compact ISO representation without separators.
     *
     * @return compact ISO EVSE identifier
     */
    public String toCompactString() {
        return toString().replace("*", "");
    }

    /**
     * Returns the operator party identifier derived from this EVSE ID.
     *
     * @return party ID
     */
    public PartyId partyId() {
        return PartyId.of(countryCode, operatorId);
    }

    @Override
    public String toString() {
        return countryCode + "*" + operatorId + "*E" + powerOutletId;
    }

    private static String normalizePower(String powerOutletId) {
        var normalized = powerOutletId.toUpperCase(Locale.ROOT);
        if (normalized.startsWith("E")) {
            return normalized.substring(1);
        }
        return normalized;
    }
}
