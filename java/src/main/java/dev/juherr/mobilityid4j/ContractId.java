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
 * Immutable contract identifier with support for ISO, EMI3, and DIN formats.
 */
public final class ContractId {
    private static final Pattern ISO_FULL =
            Pattern.compile("^([A-Za-z]{2})(?:-?)([A-Za-z0-9]{3})(?:-?)([A-Za-z0-9]{9})(?:(?:-?)([A-Za-z0-9]))?$");
    private static final Pattern EMI3_FULL =
            Pattern.compile("^([A-Za-z]{2})(?:-?)([A-Za-z0-9]{3})(?:-?)([Cc][A-Za-z0-9]{8})(?:(?:-?)([A-Za-z0-9]))?$");
    private static final Pattern DIN_FULL = Pattern.compile(
            "^([A-Za-z]{2})(?:[*-]?)([A-Za-z0-9]{3})(?:[*-]?)([A-Za-z0-9]{6})(?:(?:[*-]?)([A-Za-z0-9]))?$");

    private static final Pattern ISO_INSTANCE = Pattern.compile("[A-Za-z0-9]{9}");
    private static final Pattern EMI3_INSTANCE = Pattern.compile("[Cc][A-Za-z0-9]{8}");
    private static final Pattern DIN_INSTANCE = Pattern.compile("[A-Za-z0-9]{6}");

    private final ContractIdStandard standard;
    private final CountryCode countryCode;
    private final ProviderId providerId;
    private final String instanceValue;
    private final char checkDigit;

    private ContractId(
            ContractIdStandard standard,
            CountryCode countryCode,
            ProviderId providerId,
            String instanceValue,
            char checkDigit) {
        this.standard = standard;
        this.countryCode = countryCode;
        this.providerId = providerId;
        this.instanceValue = instanceValue;
        this.checkDigit = checkDigit;
    }

    /**
     * Creates a contract ID and computes its check digit.
     *
     * @param standard identifier standard
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @param providerId party/provider code
     * @param instanceValue standard-specific instance value
     * @return validated contract ID
     */
    public static ContractId of(
            ContractIdStandard standard, String countryCode, String providerId, String instanceValue) {
        return of(standard, CountryCode.of(countryCode), ProviderId.of(providerId), instanceValue, Optional.empty());
    }

    /**
     * Creates a contract ID and validates a provided check digit.
     *
     * @param standard identifier standard
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @param providerId party/provider code
     * @param instanceValue standard-specific instance value
     * @param checkDigit expected check digit
     * @return validated contract ID
     */
    public static ContractId of(
            ContractIdStandard standard, String countryCode, String providerId, String instanceValue, char checkDigit) {
        return of(
                standard,
                CountryCode.of(countryCode),
                ProviderId.of(providerId),
                instanceValue,
                Optional.of(checkDigit));
    }

    /**
     * Parses and validates an identifier for a specific standard.
     *
     * @param standard identifier standard
     * @param raw contract ID string
     * @return parsed contract ID
     * @throws IllegalArgumentException if parsing or validation fails
     */
    public static ContractId parseStrict(ContractIdStandard standard, String raw) {
        var fullPattern = fullPattern(standard);
        var matcher = fullPattern.matcher(raw);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(raw + " is not a valid Contract Id for " + standard.displayName());
        }

        var country = matcher.group(1);
        var provider = matcher.group(2);
        var instance = matcher.group(3);
        var check = matcher.group(4);

        if (check != null) {
            return of(standard, country, provider, instance, check.charAt(0));
        }
        return of(standard, country, provider, instance);
    }

    /**
     * Parses and validates an identifier for a specific standard.
     *
     * @param standard identifier standard
     * @param raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parse(@Nullable ContractIdStandard standard, @Nullable String raw) {
        if (standard == null || raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(parseStrict(standard, raw));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static ContractId of(
            ContractIdStandard standard,
            CountryCode countryCode,
            ProviderId providerId,
            String instanceValue,
            Optional<Character> checkDigit) {
        validateInstanceValue(standard, instanceValue);
        var normalizedInstance = instanceValue.toUpperCase(Locale.ROOT);
        var computedCheck = computeCheckDigit(standard, countryCode + providerId.id() + normalizedInstance);

        if (checkDigit.isPresent()) {
            var provided = Character.toUpperCase(checkDigit.orElseThrow());
            if (provided != computedCheck) {
                throw new IllegalArgumentException(
                        "Given check digit '" + provided + "' is not equal to computed '" + computedCheck + "'");
            }
        }

        return new ContractId(standard, countryCode, providerId, normalizedInstance, computedCheck);
    }

    /**
     * Converts this identifier to another standard when conversion rules allow it.
     *
     * @param target target standard
     * @return converted contract ID
     */
    public ContractId convertTo(ContractIdStandard target) {
        if (target == standard) {
            return this;
        }

        return switch (standard) {
            case DIN -> convertFromDin(target);
            case EMI3 -> convertFromEmi3(target);
            case ISO -> convertFromIso(target);
        };
    }

    private ContractId convertFromDin(ContractIdStandard target) {
        return switch (target) {
            case EMI3 ->
                of(
                        ContractIdStandard.EMI3,
                        countryCode.toString(),
                        providerId.toString(),
                        "C0" + instanceValue + checkDigit);
            case ISO ->
                of(
                        ContractIdStandard.ISO,
                        countryCode.toString(),
                        providerId.toString(),
                        "00" + instanceValue + checkDigit);
            case DIN -> this;
        };
    }

    private ContractId convertFromEmi3(ContractIdStandard target) {
        return switch (target) {
            case DIN -> {
                if (!instanceValue.startsWith("C0")) {
                    throw new IllegalStateException(
                            this + " cannot be converted to " + ContractIdStandard.DIN.displayName() + " format");
                }
                var dinInstance = instanceValue.substring(2, 8);
                var dinCheck = instanceValue.substring(8, 9).charAt(0);
                yield of(ContractIdStandard.DIN, countryCode.toString(), providerId.toString(), dinInstance, dinCheck);
            }
            case ISO ->
                of(ContractIdStandard.ISO, countryCode.toString(), providerId.toString(), instanceValue, checkDigit);
            case EMI3 -> this;
        };
    }

    private ContractId convertFromIso(ContractIdStandard target) {
        return switch (target) {
            case DIN -> {
                if (!instanceValue.startsWith("00")) {
                    throw new IllegalStateException(
                            this + " cannot be converted to " + ContractIdStandard.DIN.displayName() + " format");
                }
                var dinInstance = instanceValue.substring(2, 8);
                var dinCheck = instanceValue.substring(8, 9).charAt(0);
                yield of(ContractIdStandard.DIN, countryCode.toString(), providerId.toString(), dinInstance, dinCheck);
            }
            case ISO -> this;
            case EMI3 -> throw new IllegalStateException("Direct conversion from ISO to EMI3 is not supported");
        };
    }

    /**
     * Returns the contract ID standard.
     *
     * @return contract ID standard
     */
    public ContractIdStandard standard() {
        return standard;
    }

    /**
     * Returns the country code part.
     *
     * @return country code part
     */
    public CountryCode countryCode() {
        return countryCode;
    }

    /**
     * Returns the provider/party identifier part.
     *
     * @return provider/party identifier part
     */
    public ProviderId providerId() {
        return providerId;
    }

    /**
     * Returns the standard-specific instance value.
     *
     * @return instance value
     */
    public String instanceValue() {
        return instanceValue;
    }

    /**
     * Returns the computed check digit.
     *
     * @return check digit
     */
    public char checkDigit() {
        return checkDigit;
    }

    /**
     * Returns the compact representation without separators and without check digit.
     *
     * @return compact representation without check digit
     */
    public String toCompactStringWithoutCheckDigit() {
        return countryCode + providerId.toString() + instanceValue;
    }

    /**
     * Returns the compact representation without separators.
     *
     * @return compact representation
     */
    public String toCompactString() {
        return toCompactStringWithoutCheckDigit() + checkDigit;
    }

    /**
     * Returns the party ID derived from country code and provider ID.
     *
     * @return party ID
     */
    public PartyId partyId() {
        return PartyId.of(countryCode, providerId);
    }

    @Override
    public String toString() {
        return countryCode + "-" + providerId + "-" + instanceValue + "-" + checkDigit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ContractId other)) {
            return false;
        }
        return standard == other.standard
                && countryCode.equals(other.countryCode)
                && providerId.equals(other.providerId)
                && instanceValue.equals(other.instanceValue)
                && checkDigit == other.checkDigit;
    }

    @Override
    public int hashCode() {
        var result = standard.hashCode();
        result = 31 * result + countryCode.hashCode();
        result = 31 * result + providerId.hashCode();
        result = 31 * result + instanceValue.hashCode();
        result = 31 * result + Character.hashCode(checkDigit);
        return result;
    }

    private static void validateInstanceValue(ContractIdStandard standard, String instanceValue) {
        var p = instancePattern(standard);
        if (!p.matcher(instanceValue).matches()) {
            throw new IllegalArgumentException(
                    instanceValue + " is not a valid instance value for " + standard.displayName() + " format");
        }
    }

    private static char computeCheckDigit(ContractIdStandard standard, String s) {
        return switch (standard) {
            case ISO, EMI3 -> CheckDigitIso.compute(s);
            case DIN -> CheckDigitDin.compute(s);
        };
    }

    private static Pattern instancePattern(ContractIdStandard standard) {
        return switch (standard) {
            case ISO -> ISO_INSTANCE;
            case EMI3 -> EMI3_INSTANCE;
            case DIN -> DIN_INSTANCE;
        };
    }

    private static Pattern fullPattern(ContractIdStandard standard) {
        return switch (standard) {
            case ISO -> ISO_FULL;
            case EMI3 -> EMI3_FULL;
            case DIN -> DIN_FULL;
        };
    }
}
