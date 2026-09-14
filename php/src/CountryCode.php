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

use League\ISO3166\ISO3166;

final readonly class CountryCode implements \Stringable
{
    private const string REGEX = '([A-Za-z]{2})';

    private function __construct(
        public string $cc
    ) {}

    public function __toString(): string
    {
        return $this->cc;
    }

    public static function isValid(string $countryCode): bool
    {
        if (preg_match('/^' . self::REGEX . '$/', $countryCode) !== 1) {
            return false;
        }

        try {
            new ISO3166()->alpha2($countryCode);

            return true;
        } catch (\OutOfBoundsException) {
            return false;
        }
    }

    public static function of(string $countryCode): self
    {
        if (self::isValid($countryCode)) {
            return new self(strtoupper($countryCode));
        }

        throw new \InvalidArgumentException(
            "Country Code must be valid according to ISO 3166-1 alpha-2. (Was: {$countryCode})"
        );
    }

    public static function getRegex(): string
    {
        return self::REGEX;
    }
}
