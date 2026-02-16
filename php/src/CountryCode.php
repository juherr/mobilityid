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
use League\ISO3166\ISO3166;

final class CountryCode
{
    private const REGEX = '([A-Za-z]{2})';

    /** @var list<string> */
    private static array $isoCountries;

    private function __construct(
        public string $cc
    ) {
    }

    private static function initializeIsoCountries(): void
    {
        if (! isset(self::$isoCountries)) {
            $iso3166 = new ISO3166();
            // array_values re-indexes the array to ensure sequential integer keys (list)
            self::$isoCountries = array_values(array_map(fn ($country) => $country['alpha2'], $iso3166->all()));
        }
    }

    public static function isValid(string $countryCode): bool
    {
        self::initializeIsoCountries();

        return preg_match('/^' . self::REGEX . '$/', $countryCode) === 1 && in_array(strtoupper($countryCode), self::$isoCountries, true);
    }

    public static function of(string $countryCode): self
    {
        if (self::isValid($countryCode)) {
            return new self(strtoupper($countryCode));
        }

        throw new InvalidArgumentException(
            "Country Code must be valid according to ISO 3166-1 alpha-2. (Was: $countryCode)"
        );
    }

    public function __toString(): string
    {
        return $this->cc;
    }

    public static function getRegex(): string
    {
        return self::REGEX;
    }
}
