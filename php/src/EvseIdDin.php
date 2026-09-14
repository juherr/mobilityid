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
use Juherr\MobilityId\EvseIdStandard\Din;

final class EvseIdDin extends AbstractEvseId implements Din
{
    protected string $separator = '*';

    private function __construct(
        PhoneCountryCode $countryCode,
        OperatorIdDin $operatorId,
        string $powerOutletId
    ) {
        parent::__construct($countryCode, $operatorId, $powerOutletId);
    }

    public static function of(string $evseIdString): self
    {
        $regex = EvseIdParser::getDinEvseIdRegex();
        if (preg_match($regex, $evseIdString, $matches)) {
            $countryCode = PhoneCountryCode::of($matches[1]);
            $operatorId = OperatorIdDin::of($matches[2]);
            $powerOutletId = $matches[3];

            return new self($countryCode, $operatorId, $powerOutletId);
        }

        throw new InvalidArgumentException(
            "'$evseIdString' is not a valid DIN EVSE ID"
        );
    }

    public static function ofParts(
        CountryCode|PhoneCountryCode $countryCode,
        OperatorIdDin $operatorId,
        string $powerOutletId
    ): self {
        // Scala EvseIdDin.create does: val ccWithPlus = if (cc.startsWith("+")) cc else s"+$cc"
        // PhoneCountryCode::of already handles the + sign. So we can just pass the countryCode.
        // The PhoneCountryCode needs to be built from the countryCode string to ensure proper validation.

        if (! $countryCode instanceof PhoneCountryCode) {
            throw new InvalidArgumentException(
                "Country code must be of type PhoneCountryCode for DIN EVSE ID"
            );
        }

        $regex = '/^' . EvseIdParser::getDinPowerOutletIdRegex() . '$/'; // Need to validate the powerOutletId separately
        if (! preg_match($regex, $powerOutletId)) {
            throw new InvalidArgumentException(
                "'{$powerOutletId}' is not a valid DIN Power Outlet ID"
            );
        }

        return new self($countryCode, $operatorId, $powerOutletId);
    }

    public static function opt(string $evseIdString): ?self
    {
        try {
            return self::of($evseIdString);
        } catch (InvalidArgumentException $e) {
            return null;
        }
    }
}
