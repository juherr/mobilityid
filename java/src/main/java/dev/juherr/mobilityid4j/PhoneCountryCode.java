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
        if (value == null) {
            throw new IllegalArgumentException(
                    "phone Country Code must start with a '+' sign and be followed by 1-3 digits. (Was: null)");
        }
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
    public static Optional<PhoneCountryCode> parse(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }
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
