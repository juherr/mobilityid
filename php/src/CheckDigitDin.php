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

final class CheckDigitDin
{
    /** @var array<string|int, int> */
    private static array $TO_NUMERIC_VALUE = [];

    private static bool $initialized = false;

    private function __construct()
    {
    } // Prevent instantiation

    private static function initialize(): void
    {
        if (self::$initialized) {
            return;
        }

        $chars = array_merge(range('0', '9'), range('A', 'Z'));
        foreach ($chars as $index => $char) {
            self::$TO_NUMERIC_VALUE[(string) $char] = $index; // Cast to string
        }

        self::$initialized = true;
    }

    private static function mult(int $value, int $coeff): int
    {
        // Scala: value * math.pow(2, coeff).toInt
        return $value * (2 ** $coeff);
    }

    /**
     * Calculate DIN check digit.
     *
     * @param string $contractId The input contract ID string.
     * @return string The calculated check digit character.
     */
    public static function calculate(string $contractId): string
    {
        self::initialize();

        $theString = strtoupper($contractId);
        $lookupResults = [];
        for ($i = 0; $i < strlen($theString); $i++) {
            $char = $theString[$i];
            if (! isset(self::$TO_NUMERIC_VALUE[$char])) {
                // Scala version throws sys.error, translating to InvalidArgumentException
                throw new \InvalidArgumentException("Invalid character in contract ID: $char");
            }
            $lookupResults[] = self::$TO_NUMERIC_VALUE[$char];
        }

        $sum = 0;
        $coefficient = 0;

        foreach ($lookupResults as $index => $current) {
            $calculatedStepResult = 0; // Initialize
            $newCoefficient = 0; // Initialize

            if ($current < 10) {
                $calculatedStepResult = self::mult($current, $coefficient);
                $newCoefficient = $coefficient + 1;
            } else {
                $val1 = intdiv($current, 10);
                $val2 = $current % 10;
                $stepResult1 = self::mult($val1, $coefficient);
                $stepResult2 = self::mult($val2, $coefficient + 1); // Uses coefficient from start of iteration
                $calculatedStepResult = $stepResult1 + $stepResult2;
                $newCoefficient = $coefficient + 2;
            }
            $sum += $calculatedStepResult;
            $coefficient = $newCoefficient; // Update coefficient at the end of iteration
        }

        $mod = $sum % 11;
        if ($mod >= 10) {
            return 'X';
        } else {
            return (string) $mod; // Scala: Character.forDigit(mod, 10)
        }
    }
}
