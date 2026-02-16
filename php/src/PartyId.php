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

final class PartyId
{
    private const PARTY_CODE_REGEX = '/^([A-Za-z0-9]{3})$/';
    private const PARTY_ID_REGEX = '/^([A-Za-z]{2})[-*]?([A-Za-z0-9]{3})$/';

    public function __construct(
        public readonly CountryCode|PhoneCountryCode $countryCode,
        public readonly string $partyCode
    ) {
    }

    public static function parse(string $partyIdString): ?self
    {
        if (preg_match(self::PARTY_ID_REGEX, $partyIdString, $matches)) {
            try {
                $countryCode = CountryCode::of($matches[1]);
                $partyCode = strtoupper($matches[2]);
                if (preg_match(self::PARTY_CODE_REGEX, $partyCode) === 1) { // Validate partyCode part
                    return new self($countryCode, $partyCode);
                }
            } catch (InvalidArgumentException $e) {
                // CountryCode validation failed, return null
            }
        }

        return null;
    }

    public static function of(CountryCode|PhoneCountryCode $countryCode, ProviderId|OperatorIdIso|OperatorIdDin $identifier): self
    {
        // The Scala version uses pattern matching to extract the partyCode from ProviderId/OperatorIdIso.
        // In PHP, we can directly access the 'id' property as both ProviderId and OperatorIdIso have it.
        $partyCode = strtoupper($identifier->id);

        if (preg_match(self::PARTY_CODE_REGEX, $partyCode) === 1) {
            return new self($countryCode, $partyCode);
        }

        // This case should ideally not be reached if ProviderId/OperatorIdIso are valid
        throw new InvalidArgumentException(
            "Invalid party code derived from identifier. (Was: {$identifier->id})"
        );
    }

    public function toString(): string
    {
        return $this->countryCode->cc . '-' . $this->partyCode;
    }

    public function toCompactString(): string
    {
        return $this->countryCode->cc . $this->partyCode;
    }

    public function __toString(): string
    {
        return $this->toString();
    }
}
