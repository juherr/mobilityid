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

final class CheckDigitDin
{
    /** Digits map to their value, letters to 10 (A) .. 35 (Z). */
    private const string ALPHABET = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';

    private function __construct() {} // Prevent instantiation

    /**
     * Calculate DIN check digit.
     *
     * @param string $contractId the input contract ID string
     *
     * @return string the calculated check digit character
     */
    public static function calculate(string $contractId): string
    {
        $theString = strtoupper($contractId);
        $lookupResults = [];
        for ($i = 0; $i < \strlen($theString); ++$i) {
            $char = $theString[$i];
            $value = strpos(self::ALPHABET, $char);
            if ($value === false) {
                // Scala version throws sys.error, translating to InvalidArgumentException
                throw new \InvalidArgumentException("Invalid character in contract ID: {$char}");
            }
            $lookupResults[] = $value;
        }

        $sum = 0;
        $coefficient = 0;

        foreach ($lookupResults as $current) {
            if ($current < 10) {
                $sum += self::mult($current, $coefficient);
                ++$coefficient;
            } else {
                // Letters count as two digits, each with its own coefficient
                $sum += self::mult(intdiv($current, 10), $coefficient) + self::mult($current % 10, $coefficient + 1);
                $coefficient += 2;
            }
        }

        $mod = $sum % 11;
        if ($mod >= 10) {
            return 'X';
        }

        return (string) $mod; // Scala: Character.forDigit(mod, 10)
    }

    private static function mult(int $value, int $coeff): int
    {
        // Scala: value * math.pow(2, coeff).toInt; coeff is never negative, so a shift is exact
        return $value << $coeff;
    }
}
