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
use Juherr\MobilityId\ContractIdStandard\Iso;

final class ContractIdIso extends AbstractContractId implements Iso
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
        $regex = ContractIdParser::getIsoFullRegex();
        if (preg_match($regex, $contractIdString, $matches)) {
            $countryCode = CountryCode::of($matches[1]);
            $providerId = ProviderId::of($matches[2]);
            $instanceValue = strtoupper($matches[3]);
            $checkDigitGiven = $matches[4] ?? null;

            $inputForCheckDigit = $countryCode->cc . $providerId->id . $instanceValue;
            $computedCheckDigit = ContractIdParser::computeIsoCheckDigit($inputForCheckDigit);

            if ($checkDigitGiven !== null && strtoupper($checkDigitGiven) !== $computedCheckDigit) {
                throw new InvalidArgumentException(
                    "Given check digit '{$checkDigitGiven}' is not equal to computed '{$computedCheckDigit}'"
                );
            }

            return new self($countryCode, $providerId, $instanceValue, $computedCheckDigit);
        }

        throw new InvalidArgumentException(
            "'$contractIdString' is not a valid Contract Id for ISO 15118-1"
        );
    }

    public static function ofParts(
        CountryCode $countryCode,
        ProviderId $providerId,
        string $instanceValue,
        ?string $checkDigit = null
    ): self {
        $inputForCheckDigit = $countryCode->cc . $providerId->id . strtoupper($instanceValue);
        $computedCheckDigit = ContractIdParser::computeIsoCheckDigit($inputForCheckDigit);

        if ($checkDigit !== null && strtoupper($checkDigit) !== $computedCheckDigit) {
            throw new InvalidArgumentException(
                "Given check digit '{$checkDigit}' is not equal to computed '{$computedCheckDigit}'"
            );
        }

        return new self($countryCode, $providerId, strtoupper($instanceValue), $computedCheckDigit);
    }

    public static function opt(string $contractIdString): ?self
    {
        try {
            return self::of($contractIdString);
        } catch (InvalidArgumentException $e) {
            return null;
        }
    }
}
