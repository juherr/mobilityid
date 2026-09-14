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

use Juherr\MobilityId\ContractIdStandard\Din;

final readonly class ContractIdDin extends AbstractContractId implements Din
{
    private function __construct(
        CountryCode $countryCode,
        ProviderId $providerId,
        string $instanceValue,
        string $checkDigit
    ) {
        parent::__construct($countryCode, $providerId, $instanceValue, $checkDigit);
    }

    public static function of(string $contractIdString): self
    {
        $regex = ContractIdParser::getDinFullRegex();
        if (preg_match($regex, $contractIdString, $matches) === 1) {
            return self::ofParts(CountryCode::of($matches[1]), ProviderId::of($matches[2]), $matches[3], $matches[4] ?? null);
        }

        throw new \InvalidArgumentException(
            "'{$contractIdString}' is not a valid Contract Id for DIN SPEC 91286"
        );
    }

    public static function ofParts(
        CountryCode $countryCode,
        ProviderId $providerId,
        string $instanceValue,
        ?string $checkDigit = null
    ): self {
        $instanceValue = strtoupper($instanceValue);
        $computedCheckDigit = ContractIdParser::computeDinCheckDigit($countryCode->cc . $providerId->id . $instanceValue);

        if ($checkDigit !== null && strtoupper($checkDigit) !== $computedCheckDigit) {
            throw new \InvalidArgumentException(
                "Given check digit '{$checkDigit}' is not equal to computed '{$computedCheckDigit}'"
            );
        }

        return new self($countryCode, $providerId, $instanceValue, $computedCheckDigit);
    }

    public static function opt(string $contractIdString): ?self
    {
        try {
            return self::of($contractIdString);
        } catch (\InvalidArgumentException) {
            return null;
        }
    }
}
