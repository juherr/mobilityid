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

/** Convenience parser entry points for mobility ID domain types. */
public final class MobilityIdParsers {
    private MobilityIdParsers() {}

    /**
     * Parses an ISO contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdIso(String raw) {
        return ContractId.parse(ContractIdStandard.ISO, raw);
    }

    /**
     * Parses a DIN contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdDin(String raw) {
        return ContractId.parse(ContractIdStandard.DIN, raw);
    }

    /**
     * Parses an EMI3 contract ID.
     *
     * @param raw raw contract ID string
     * @return parsed contract ID, or empty when invalid
     */
    public static Optional<ContractId> parseContractIdEmi3(String raw) {
        return ContractId.parse(ContractIdStandard.EMI3, raw);
    }

    /**
     * Parses an EVSE ID in ISO or DIN format.
     *
     * @param raw raw EVSE ID
     * @return parsed EVSE ID, or empty when invalid
     */
    public static Optional<EvseId> parseEvseId(String raw) {
        return EvseId.parse(raw);
    }

    /**
     * Parses an ISO EVSE ID.
     *
     * @param raw raw EVSE ID
     * @return parsed ISO EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdIso> parseEvseIdIso(String raw) {
        return EvseIdIso.parse(raw);
    }

    /**
     * Parses a DIN EVSE ID.
     *
     * @param raw raw EVSE ID
     * @return parsed DIN EVSE ID, or empty when invalid
     */
    public static Optional<EvseIdDin> parseEvseIdDin(String raw) {
        return EvseIdDin.parse(raw);
    }

    /**
     * Parses a provider ID.
     *
     * @param raw raw provider ID
     * @return parsed provider ID, or empty when invalid
     */
    public static Optional<ProviderId> parseProviderId(String raw) {
        return ProviderId.parse(raw);
    }

    /**
     * Parses a country code.
     *
     * @param raw raw country code
     * @return parsed country code, or empty when invalid
     */
    public static Optional<CountryCode> parseCountryCode(String raw) {
        return CountryCode.parse(raw);
    }

    /**
     * Parses a phone country code.
     *
     * @param raw raw phone country code
     * @return parsed phone country code, or empty when invalid
     */
    public static Optional<PhoneCountryCode> parsePhoneCountryCode(String raw) {
        return PhoneCountryCode.parse(raw);
    }

    /**
     * Parses an ISO operator ID.
     *
     * @param raw raw operator ID
     * @return parsed operator ID, or empty when invalid
     */
    public static Optional<OperatorIdIso> parseOperatorIdIso(String raw) {
        return OperatorIdIso.parse(raw);
    }

    /**
     * Parses a DIN operator ID.
     *
     * @param raw raw operator ID
     * @return parsed operator ID, or empty when invalid
     */
    public static Optional<OperatorIdDin> parseOperatorIdDin(String raw) {
        return OperatorIdDin.parse(raw);
    }
}
