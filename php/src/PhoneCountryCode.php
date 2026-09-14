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

final class PhoneCountryCode
{
    private const REGEX = '\+?([0-9]{1,3})'; // Removed anchors

    private function __construct(
        public string $cc
    ) {
    }

    public static function isValid(string $phoneCountryCode): bool
    {
        return preg_match('/^' . self::REGEX . '$/', $phoneCountryCode) === 1; // Anchors added for validation
    }

    public static function of(string $phoneCountryCode): self
    {
        if (self::isValid($phoneCountryCode)) {
            // Ensure '+' prefix for consistency, as per Scala's EvseIdDin.create
            $formattedCountryCode = str_starts_with($phoneCountryCode, '+') ? $phoneCountryCode : '+' . $phoneCountryCode;

            return new self($formattedCountryCode);
        }

        throw new InvalidArgumentException(
            "Phone Country Code must start with a '+' sign and be followed by 1-3 digits. (Was: $phoneCountryCode)"
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
