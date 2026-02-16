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

/** Common EVSE identifier contract across supported formats. */
public sealed interface EvseId permits EvseIdIso, EvseIdDin {
    /**
     * Returns the outlet-specific part of the EVSE identifier.
     *
     * @return power outlet identifier fragment
     */
    String powerOutletId();

    /**
     * Creates an EVSE ID from individual parts, selecting ISO or DIN based on validation.
     *
     * @param countryCode country code part
     * @param operatorId operator code part
     * @param powerOutletId outlet part
     * @return validated EVSE ID
     */
    static EvseId of(String countryCode, String operatorId, String powerOutletId) {
        var isoError = EvseIdIso.validate(countryCode, operatorId, powerOutletId);
        var dinError = EvseIdDin.validate(countryCode, operatorId, powerOutletId);

        if (isoError.isEmpty()) {
            return EvseIdIso.of(countryCode, operatorId, powerOutletId);
        }
        if (dinError.isEmpty()) {
            return EvseIdDin.of(countryCode, operatorId, powerOutletId);
        }

        var e1 = isoError.orElseThrow();
        var e2 = dinError.orElseThrow();
        throw new IllegalArgumentException(e1.priority() >= e2.priority() ? e1.description() : e2.description());
    }

    /**
     * Parses an EVSE ID string in either ISO or DIN form.
     *
     * @param evseId raw EVSE ID
     * @return parsed EVSE ID, or empty when invalid
     */
    static Optional<EvseId> parse(@Nullable String evseId) {
        if (evseId == null) {
            return Optional.empty();
        }
        var iso = EvseIdIso.parse(evseId);
        if (iso.isPresent()) {
            return Optional.of(iso.orElseThrow());
        }
        var din = EvseIdDin.parse(evseId);
        return din.map(it -> it);
    }
}
