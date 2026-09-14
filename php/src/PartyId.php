<?php

declare(strict_types=1);

/*
 * This file is part of the Mobility ID library.
 *
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

final readonly class PartyId implements \Stringable
{
    private const string PARTY_CODE_REGEX = '/^([A-Za-z0-9]{3})$/';
    private const string PARTY_ID_REGEX = '/^([A-Za-z]{2})[-*]?([A-Za-z0-9]{3})$/';

    public function __construct(
        public CountryCode|PhoneCountryCode $countryCode,
        public string $partyCode
    ) {}

    public function __toString(): string
    {
        return $this->toString();
    }

    public static function parse(string $partyIdString): ?self
    {
        if (preg_match(self::PARTY_ID_REGEX, $partyIdString, $matches) === 1) {
            try {
                $countryCode = CountryCode::of($matches[1]);
                $partyCode = strtoupper($matches[2]);
                if (preg_match(self::PARTY_CODE_REGEX, $partyCode) === 1) { // Validate partyCode part
                    return new self($countryCode, $partyCode);
                }
            } catch (\InvalidArgumentException) {
                // CountryCode validation failed, return null
            }
        }

        return null;
    }

    public static function of(CountryCode|PhoneCountryCode $countryCode, OperatorIdDin|OperatorIdIso|ProviderId $identifier): self
    {
        // The Scala version uses pattern matching to extract the partyCode from ProviderId/OperatorIdIso.
        // In PHP, we can directly access the 'id' property; the factories already uppercased it.
        $partyCode = $identifier->id;

        if (preg_match(self::PARTY_CODE_REGEX, $partyCode) === 1) {
            return new self($countryCode, $partyCode);
        }

        // This case should ideally not be reached if ProviderId/OperatorIdIso are valid
        throw new \InvalidArgumentException(
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
}
