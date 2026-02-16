/*
 * Copyright (c) 2014 The New Motion team, and respective contributors
 * Copyright (c) 2026 Julien Herr, and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.juherr.mobilityid4j;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

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
        return new EvseIdIso(
                CountryCode.of(countryCode), OperatorIdIso.of(operatorId), powerOutletId.toUpperCase(Locale.ROOT));
    }

    /**
     * Parses an ISO EVSE identifier.
     *
     * @param evseId raw EVSE ID
     * @return parsed ISO EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdIso> parse(@Nullable String evseId) {
        if (evseId == null) {
            return Optional.empty();
        }
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
        if (countryCode == null || operatorId == null || powerOutletId == null) {
            return Optional.of(new EvseValidationError(1, "Invalid countryCode for ISO or DIN format"));
        }
        var cc = countryCode.toUpperCase(Locale.ROOT);
        var op = operatorId.toUpperCase(Locale.ROOT);
        var po = powerOutletId.toUpperCase(Locale.ROOT);

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

}
