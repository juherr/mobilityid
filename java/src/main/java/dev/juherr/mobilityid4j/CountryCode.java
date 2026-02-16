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

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

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
        if (value == null) {
            throw new IllegalArgumentException("Country Code must be valid according to ISO 3166-1 alpha-2");
        }
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
    public static Optional<CountryCode> parse(@Nullable String value) {
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
