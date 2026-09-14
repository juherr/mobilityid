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

use Juherr\MobilityId\EvseIdStandard\Iso;

final readonly class EvseIdIso extends AbstractEvseId implements Iso
{
    private const string ID_TYPE = 'E'; // From Scala EvseIdIso.IdType

    private function __construct(
        CountryCode $countryCode,
        OperatorIdIso $operatorId,
        string $powerOutletId
    ) {
        parent::__construct($countryCode, $operatorId, $powerOutletId);
    }

    public static function of(string $evseIdString): self
    {
        $regex = EvseIdParser::getIsoEvseIdRegex();
        if (preg_match($regex, $evseIdString, $matches) === 1) {
            $countryCode = CountryCode::of($matches[1]);
            $operatorId = OperatorIdIso::of($matches[2]);
            $powerOutletId = strtoupper($matches[3]);

            return new self($countryCode, $operatorId, $powerOutletId);
        }

        throw new \InvalidArgumentException(
            "'{$evseIdString}' is not a valid ISO EVSE ID"
        );
    }

    public static function ofParts(
        CountryCode $countryCode,
        OperatorIdIso $operatorId,
        string $powerOutletId
    ): self {
        $normalizedPowerOutletId = strtoupper($powerOutletId);
        if (str_starts_with($normalizedPowerOutletId, self::ID_TYPE)) {
            $normalizedPowerOutletId = substr($normalizedPowerOutletId, 1);
        }

        // Scala validates the power outlet id after the id type: E + ([A-Za-z0-9*]{1,31}).
        $regex = '/^' . EvseIdParser::getIsoPowerOutletIdRegex() . '$/';
        if (preg_match($regex, $normalizedPowerOutletId) !== 1) {
            throw new \InvalidArgumentException(
                "'" . self::ID_TYPE . $normalizedPowerOutletId . "' is not a valid ISO Power Outlet ID"
            );
        }

        return new self($countryCode, $operatorId, $normalizedPowerOutletId);
    }

    public function toString(): string
    {
        return $this->countryCode->cc . self::SEPARATOR . $this->operatorId->id . self::SEPARATOR . self::ID_TYPE . $this->powerOutletId;
    }

    public function toCompactString(): string
    {
        return $this->countryCode->cc . $this->operatorId->id . self::ID_TYPE . str_replace(self::SEPARATOR, '', $this->powerOutletId);
    }

    public function partyId(): PartyId
    {
        return PartyId::of($this->countryCode, $this->operatorId);
    }

    public static function opt(string $evseIdString): ?self
    {
        try {
            return self::of($evseIdString);
        } catch (\InvalidArgumentException) {
            return null;
        }
    }
}
