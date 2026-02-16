<?php

declare(strict_types=1);

/*
 * This file is part of the Mobility ID library.
 *
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

namespace Juherr\MobilityId;

use InvalidArgumentException;

final class EvseId
{
    private function __construct()
    {
    } // Prevent instantiation

    /**
     * Attempts to parse an EVSE ID string, trying ISO format first, then DIN format.
     *
     * @param string $evseIdString The EVSE ID string to parse.
     * @return AbstractEvseId The parsed EVSE ID.
     * @throws InvalidArgumentException If the string is not a valid ISO or DIN EVSE ID.
     */
    public static function of(string $evseIdString): AbstractEvseId
    {
        // Try ISO first
        try {
            return EvseIdIso::of($evseIdString);
        } catch (InvalidArgumentException $eIso) {
            // If ISO fails, try DIN
            try {
                return EvseIdDin::of($evseIdString);
            } catch (InvalidArgumentException $eDin) {
                // If both fail, determine which error has higher priority (as in Scala)
                // This would require Error objects with priority, but for now we just throw the last one or a combined message.
                // For simplicity, we'll re-throw the original errors as a combined exception message.
                throw new InvalidArgumentException(
                    "'$evseIdString' is not a valid ISO EVSE ID ({$eIso->getMessage()}) " .
                    "and not a valid DIN EVSE ID ({$eDin->getMessage()})"
                );
            }
        }
    }

    /**
     * Attempts to parse an EVSE ID string, trying ISO format first, then DIN format.
     * Returns null if parsing fails.
     *
     * @param string $evseIdString The EVSE ID string to parse.
     * @return AbstractEvseId|null The parsed EVSE ID, or null if invalid.
     */
    public static function opt(string $evseIdString): ?AbstractEvseId
    {
        try {
            return self::of($evseIdString);
        } catch (InvalidArgumentException $e) {
            return null;
        }
    }

    /**
     * Creates an EVSE ID from its components, trying to determine the correct format (ISO or DIN).
     *
     * @param string $countryCodeString Country Code string (e.g., "NL" or "+31").
     * @param string $operatorIdString Operator ID string (e.g., "TNM" or "810").
     * @param string $powerOutletIdString Power Outlet ID string.
     * @return AbstractEvseId The created EVSE ID.
     * @throws InvalidArgumentException If the combination of parts is not valid for any known EVSE ID format.
     */
    public static function ofParts(
        string $countryCodeString,
        string $operatorIdString,
        string $powerOutletIdString
    ): AbstractEvseId {
        // Try to create ISO EVSE ID
        try {
            $countryCode = CountryCode::of($countryCodeString);
            $operatorId = OperatorIdIso::of($operatorIdString);

            return EvseIdIso::ofParts($countryCode, $operatorId, $powerOutletIdString);
        } catch (InvalidArgumentException $eIso) {
            // If ISO fails, try DIN
            try {
                $countryCode = PhoneCountryCode::of($countryCodeString);
                $operatorId = OperatorIdDin::of($operatorIdString);

                return EvseIdDin::ofParts($countryCode, $operatorId, $powerOutletIdString);
            } catch (InvalidArgumentException $eDin) {
                // Both failed
                throw new InvalidArgumentException(
                    "Cannot create EVSE ID from parts. Invalid for ISO ({$eIso->getMessage()}) " .
                    "and invalid for DIN ({$eDin->getMessage()})"
                );
            }
        }
    }
}
