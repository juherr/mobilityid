package dev.juherr.mobilityid4j;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * EVSE identifier in DIN format.
 *
 * @param countryCode phone country code with leading {@code +}
 * @param operatorId DIN operator identifier
 * @param powerOutletId outlet identifier fragment
 */
public record EvseIdDin(PhoneCountryCode countryCode, OperatorIdDin operatorId, String powerOutletId)
        implements EvseId {
    private static final Pattern POWER_OUTLET = Pattern.compile("[0-9*]{1,32}");
    private static final Pattern FULL = Pattern.compile("^(\\+?[0-9]{1,3})\\*([0-9]{3,6})\\*([0-9*]{1,32})$");

    /**
     * Creates and validates a DIN EVSE identifier.
     */
    public EvseIdDin {
        var normalizedPower = powerOutletId.toUpperCase(Locale.ROOT);
        if (!POWER_OUTLET.matcher(normalizedPower).matches()) {
            throw new IllegalArgumentException("Invalid powerOutletId for DIN format");
        }
        powerOutletId = normalizedPower;
    }

    /**
     * Creates a DIN EVSE identifier from parts.
     *
     * @param countryCode phone country code
     * @param operatorId DIN operator identifier
     * @param powerOutletId outlet identifier fragment
     * @return validated DIN EVSE identifier
     */
    public static EvseIdDin of(String countryCode, String operatorId, String powerOutletId) {
        var error = validate(countryCode, operatorId, powerOutletId);
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.orElseThrow().description());
        }
        return new EvseIdDin(
                PhoneCountryCode.of(countryCode), OperatorIdDin.of(operatorId), powerOutletId.toUpperCase(Locale.ROOT));
    }

    /**
     * Parses a DIN EVSE identifier.
     *
     * @param evseId raw EVSE ID
     * @return parsed DIN EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdDin> parse(String evseId) {
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
        var po = powerOutletId.toUpperCase(Locale.ROOT);

        if (PhoneCountryCode.parse(cc).isEmpty()) {
            return Optional.of(new EvseValidationError(1, "Invalid countryCode for ISO or DIN format"));
        }
        if (OperatorIdDin.parse(op).isEmpty()) {
            return Optional.of(new EvseValidationError(2, "Invalid operatorId for DIN format"));
        }
        if (!POWER_OUTLET.matcher(po).matches()) {
            return Optional.of(new EvseValidationError(3, "Invalid powerOutletId for DIN format"));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return countryCode + "*" + operatorId + "*" + powerOutletId;
    }
}
