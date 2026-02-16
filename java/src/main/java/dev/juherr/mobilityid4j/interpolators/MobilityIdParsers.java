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
package dev.juherr.mobilityid4j.interpolators;

import dev.juherr.mobilityid4j.ContractId;
import dev.juherr.mobilityid4j.ContractIdStandard;
import dev.juherr.mobilityid4j.CountryCode;
import dev.juherr.mobilityid4j.EvseId;
import dev.juherr.mobilityid4j.EvseIdDin;
import dev.juherr.mobilityid4j.EvseIdIso;
import dev.juherr.mobilityid4j.OperatorIdDin;
import dev.juherr.mobilityid4j.OperatorIdIso;
import dev.juherr.mobilityid4j.PhoneCountryCode;
import dev.juherr.mobilityid4j.ProviderId;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Convenience parser entry points for mobility ID domain types. */
public final class MobilityIdParsers {
    private MobilityIdParsers() {}

    /**
     * Parses an ISO contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdIso(@Nullable String raw) {
        return ContractId.parse(ContractIdStandard.ISO, raw);
    }

    /**
     * Parses a DIN contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdDin(@Nullable String raw) {
        return ContractId.parse(ContractIdStandard.DIN, raw);
    }

    /**
     * Parses an EMI3 contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdEmi3(@Nullable String raw) {
        return ContractId.parse(ContractIdStandard.EMI3, raw);
    }

    /**
     * Parses an EVSE ID in ISO or DIN format.
     *
     * @param raw raw EVSE ID
     * @return parsed EVSE ID, or empty when invalid
     */
    public static Optional<EvseId> parseEvseId(@Nullable String raw) {
        return EvseId.parse(raw);
    }

    /**
     * Parses an ISO EVSE ID.
     *
     * @param raw raw EVSE ID
     * @return parsed ISO EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdIso> parseEvseIdIso(@Nullable String raw) {
        return EvseIdIso.parse(raw);
    }

    /**
     * Parses a DIN EVSE ID.
     *
     * @param raw raw EVSE ID
     * @return parsed DIN EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdDin> parseEvseIdDin(@Nullable String raw) {
        return EvseIdDin.parse(raw);
    }

    /**
     * Parses a provider ID.
     *
     * @param raw raw provider ID
     * @return parsed provider ID, or empty when invalid
     */
    public static Optional<ProviderId> parseProviderId(@Nullable String raw) {
        return ProviderId.parse(raw);
    }

    /**
     * Parses a country code.
     *
     * @param raw raw country code
     * @return parsed country code, or empty when invalid
     */
    public static Optional<CountryCode> parseCountryCode(@Nullable String raw) {
        return CountryCode.parse(raw);
    }

    /**
     * Parses a phone country code.
     *
     * @param raw raw phone country code
     * @return parsed phone country code, or empty when invalid
     */
    public static Optional<PhoneCountryCode> parsePhoneCountryCode(@Nullable String raw) {
        return PhoneCountryCode.parse(raw);
    }

    /**
     * Parses an ISO operator ID.
     *
     * @param raw raw operator ID
     * @return parsed operator ID, or empty when invalid
     */
    public static Optional<OperatorIdIso> parseOperatorIdIso(@Nullable String raw) {
        return OperatorIdIso.parse(raw);
    }

    /**
     * Parses a DIN operator ID.
     *
     * @param raw raw operator ID
     * @return parsed operator ID, or empty when invalid
     */
    public static Optional<OperatorIdDin> parseOperatorIdDin(@Nullable String raw) {
        return OperatorIdDin.parse(raw);
    }
}
