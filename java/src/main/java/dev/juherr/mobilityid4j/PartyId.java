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

import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Party identifier (country + 3-char party code).
 *
 * @param countryCode ISO country code
 * @param partyCode normalized party code
 */
public record PartyId(CountryCode countryCode, String partyCode) {
    private static final Pattern PATTERN = Pattern.compile("([A-Za-z]{2})[-*]?([A-Za-z0-9]{3})");

    /**
     * Creates and validates a party identifier.
     */
    public PartyId {
        partyCode = new PartyCode(partyCode).value();
    }

    /**
     * Creates a party ID from country and provider IDs.
     *
     * @param countryCode country code
     * @param providerId provider ID
     * @return party ID
     */
    public static PartyId of(CountryCode countryCode, ProviderId providerId) {
        return new PartyId(countryCode, providerId.id());
    }

    /**
     * Creates a party ID from country and ISO operator IDs.
     *
     * @param countryCode country code
     * @param operatorIdIso operator ID
     * @return party ID
     */
    public static PartyId of(CountryCode countryCode, OperatorIdIso operatorIdIso) {
        return new PartyId(countryCode, operatorIdIso.id());
    }

    /**
     * Parses a party identifier from compact or separated representation.
     *
     * @param raw candidate party ID
     * @return parsed party ID, or empty when invalid
     */
    public static Optional<PartyId> parse(@Nullable String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        var matcher = PATTERN.matcher(raw);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new PartyId(CountryCode.of(matcher.group(1)), matcher.group(2)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns compact representation without separators.
     *
     * @return compact party ID
     */
    public String toCompactString() {
        return countryCode + partyCode;
    }

    @Override
    public String toString() {
        return countryCode + "-" + partyCode;
    }
}
