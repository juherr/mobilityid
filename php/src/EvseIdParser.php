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

final class EvseIdParser
{
    private function __construct()
    {
    } // Prevent instantiation

    // EVSE ID DIN Format
    public static function getDinCountryCodeRegex(): string
    {
        // PhoneCountryCode.Regex is '+?([0-9]{1,3})'. We need to remove the '^' and '$' if present.
        return PhoneCountryCode::getRegex();
    }

    public static function getDinOperatorCodeRegex(): string
    {
        return OperatorIdDin::getRegex();
    }

    public static function getDinPowerOutletIdRegex(): string
    {
        return '([0-9\*]{1,32})';
    }

    public static function getDinEvseIdRegex(): string
    {
        return '/^' .
               self::getDinCountryCodeRegex() .
               '\*' .
               self::getDinOperatorCodeRegex() .
               '\*' .
               self::getDinPowerOutletIdRegex() .
               '$/';
    }

    // EVSE ID ISO Format
    public static function getIsoCountryCodeRegex(): string
    {
        return CountryCode::getRegex();
    }

    public static function getIsoOperatorCodeRegex(): string
    {
        // PartyCode.Regex is '([A-Za-z0-9]{3})'. ProviderId provides this.
        return ProviderId::getRegex();
    }

    public static function getIsoPowerOutletIdRegex(): string
    {
        return '([A-Za-z0-9\*]{1,31})';
    }

    public static function getIsoEvseIdRegex(): string
    {
        return '/^' .
               self::getIsoCountryCodeRegex() .
               '\*?' . // Separator is optional
               self::getIsoOperatorCodeRegex() .
               '\*?' . // Separator is optional
               'E' . // IdType
               self::getIsoPowerOutletIdRegex() .
               '$/';
    }
}
