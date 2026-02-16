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
import org.jspecify.annotations.Nullable;

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
    public static Optional<OperatorIdIso> parse(@Nullable String id) {
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
