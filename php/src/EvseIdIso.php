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
use Juherr\MobilityId\EvseIdStandard\Iso;
use LogicException;

final class EvseIdIso extends AbstractEvseId implements Iso
{
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
        if (preg_match($regex, $evseIdString, $matches)) {
            $countryCode = CountryCode::of($matches[1]);
            $operatorId = OperatorIdIso::of($matches[2]);
            $powerOutletId = strtoupper($matches[3]);

            return new self($countryCode, $operatorId, $powerOutletId);
        }

        throw new InvalidArgumentException(
            "'$evseIdString' is not a valid ISO EVSE ID"
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

        $fullPowerOutletId = self::ID_TYPE . $normalizedPowerOutletId;
        $regex = '/^' . EvseIdParser::getIsoPowerOutletIdRegex() . '$/'; // Need to validate the powerOutletId separately

        if (! preg_match($regex, $fullPowerOutletId)) {
            throw new InvalidArgumentException(
                "'{$fullPowerOutletId}' is not a valid ISO Power Outlet ID"
            );
        }

        return new self($countryCode, $operatorId, $normalizedPowerOutletId);
    }

    private const ID_TYPE = 'E'; // From Scala EvseIdIso.IdType

    public function toString(): string
    {
        return $this->countryCode->cc . $this->separator . $this->operatorId->id . $this->separator . self::ID_TYPE . $this->powerOutletId;
    }

    public function toCompactString(): string
    {
        return $this->countryCode->cc . $this->operatorId->id . self::ID_TYPE . str_replace($this->separator, '', $this->powerOutletId);
    }

    public function partyId(): PartyId
    {
        if (! $this->countryCode instanceof CountryCode || ! $this->operatorId instanceof OperatorIdIso) {
            throw new LogicException('EvseIdIso must contain ISO country and operator identifiers');
        }

        return PartyId::of($this->countryCode, $this->operatorId);
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
