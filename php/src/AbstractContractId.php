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

abstract class AbstractContractId
{
    protected function __construct(
        public readonly CountryCode $countryCode,
        public readonly ProviderId $providerId,
        public readonly string $instanceValue,
        public readonly string $checkDigit
    ) {
    }

    public function toString(): string
    {
        return $this->countryCode->cc . '-' . $this->providerId->id . '-' . $this->instanceValue . '-' . $this->checkDigit;
    }

    public function toCompactString(): string
    {
        return $this->countryCode->cc . $this->providerId->id . $this->instanceValue . $this->checkDigit;
    }

    public function toCompactStringWithoutCheckDigit(): string
    {
        return $this->countryCode->cc . $this->providerId->id . $this->instanceValue;
    }

    public function partyId(): PartyId
    {
        return PartyId::of($this->countryCode, $this->providerId);
    }

    public function __toString(): string
    {
        return $this->toString();
    }

    // Conversion methods

    /**
     * Converts to ContractIdDin. Only applicable if the current ContractId is EMI3 or ISO and follows specific formats.
     * @return ContractIdDin
     * @throws \InvalidArgumentException
     */
    public function convertToDin(): ContractIdDin
    {
        if ($this instanceof ContractIdEmi3) {
            if (! str_starts_with($this->instanceValue, 'C0')) {
                throw new \InvalidArgumentException(
                    "{$this->toString()} cannot be converted to DIN SPEC 91286 format"
                );
            }
            $dinInstance = substr($this->instanceValue, 2, 6);
            $dinCheck = substr($this->instanceValue, 8, 1);

            return ContractIdDin::ofParts($this->countryCode, $this->providerId, $dinInstance, $dinCheck);
        } elseif ($this instanceof ContractIdIso) {
            if (! str_starts_with($this->instanceValue, '00')) {
                throw new \InvalidArgumentException(
                    "{$this->toString()} cannot be converted to DIN SPEC 91286 format"
                );
            }
            $dinInstance = substr($this->instanceValue, 2, 6);
            $dinCheck = substr($this->instanceValue, 8, 1);

            return ContractIdDin::ofParts($this->countryCode, $this->providerId, $dinInstance, $dinCheck);
        }

        throw new \InvalidArgumentException(
            "Conversion from " . get_class($this) . " to ContractIdDin is not supported."
        );
    }

    /**
     * Converts to ContractIdEmi3. Only applicable if the current ContractId is DIN.
     * @return ContractIdEmi3
     * @throws \InvalidArgumentException
     */
    public function convertToEmi3(): ContractIdEmi3
    {
        if ($this instanceof ContractIdDin) {
            return ContractIdEmi3::ofParts($this->countryCode, $this->providerId, "C0" . $this->instanceValue . $this->checkDigit);
        }

        throw new \InvalidArgumentException(
            "Conversion from " . get_class($this) . " to ContractIdEmi3 is not supported."
        );
    }

    /**
     * Converts to ContractIdIso. Only applicable if the current ContractId is DIN or EMI3.
     * @return ContractIdIso
     * @throws \InvalidArgumentException
     */
    public function convertToIso(): ContractIdIso
    {
        if ($this instanceof ContractIdDin) {
            return ContractIdIso::ofParts($this->countryCode, $this->providerId, "00" . $this->instanceValue . $this->checkDigit);
        } elseif ($this instanceof ContractIdEmi3) {
            return ContractIdIso::ofParts($this->countryCode, $this->providerId, $this->instanceValue, $this->checkDigit);
        }

        throw new \InvalidArgumentException(
            "Conversion from " . get_class($this) . " to ContractIdIso is not supported."
        );
    }
}
