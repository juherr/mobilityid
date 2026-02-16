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

final class ContractIdParser
{
    private const CHECK_DIGIT_REGEX = '([A-Za-z0-9])';

    private function __construct()
    {
    } // Prevent instantiation

    public static function getIsoFullRegex(): string
    {
        return '/^' .
               CountryCode::getRegex() .
               '(?:-?)' .
               ProviderId::getRegex() . // ProviderId contains the PartyCode regex
               '(?:-?)' .
               '([A-Za-z0-9]{9})' . // InstanceRegex for ISO
               '(?:(?:-?)' .
               self::CHECK_DIGIT_REGEX .
               ')?$/';
    }

    public static function getEmi3FullRegex(): string
    {
        return '/^' .
               CountryCode::getRegex() .
               '(?:-?)' .
               ProviderId::getRegex() . // ProviderId contains the PartyCode regex
               '(?:-?)' .
               '([Cc][A-Za-z0-9]{8})' . // InstanceRegex for EMI3
               '(?:(?:-?)' .
               self::CHECK_DIGIT_REGEX .
               ')?$/';
    }

    public static function getDinFullRegex(): string
    {
        return '/^' .
               CountryCode::getRegex() .
               '(?:[*-]?)' .
               ProviderId::getRegex() . // ProviderId contains the PartyCode regex
               '(?:[*-]?)' .
               '([A-Za-z0-9]{6})' . // InstanceRegex for DIN
               '(?:(?:[*-]?)' .
               self::CHECK_DIGIT_REGEX .
               ')?$/';
    }

    public static function computeIsoCheckDigit(string $input): string
    {
        return CheckDigitIso::calculate($input);
    }

    public static function computeEmi3CheckDigit(string $input): string
    {
        return CheckDigitIso::calculate($input); // EMI3 also uses ISO check digit
    }

    public static function computeDinCheckDigit(string $input): string
    {
        return CheckDigitDin::calculate($input);
    }
}
