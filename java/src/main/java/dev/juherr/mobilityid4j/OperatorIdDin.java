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
        if (id == null) {
            throw new IllegalArgumentException("OperatorId must have a length of 3-6 chars and be digits");
        }
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
    public static Optional<OperatorIdDin> parse(@Nullable String id) {
        if (id == null) {
            return Optional.empty();
        }
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
