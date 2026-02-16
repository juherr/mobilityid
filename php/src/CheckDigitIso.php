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
use Juherr\MobilityId\CheckDigitIso\Matrix;

final class CheckDigitIso
{
    private function __construct()
    {
    } // Prevent instantiation

    private static Matrix $P1;

    private static Matrix $P2;

    /** @var array<int, Matrix> */
    private static array $P1S = [];

    /** @var array<int, Matrix> */
    private static array $P2S = [];

    private static Matrix $NEG_P2_MINUS_15;

    /** @var array<string|int, Matrix> */
    private static array $ENCODING = [];

    private static bool $initialized = false;

    private static function initialize(): void
    {
        if (self::$initialized) {
            return;
        }

        self::$P1 = new Matrix(0, 1, 1, 1);
        self::$P2 = new Matrix(0, 1, 1, 2);

        self::$P1S = [];
        $currentP1 = self::$P1;
        for ($i = 0; $i < 14; $i++) {
            self::$P1S[] = $currentP1;
            $currentP1 = $currentP1->multiply(self::$P1);
        }

        self::$P2S = [];
        $currentP2 = self::$P2;
        for ($i = 0; $i < 14; $i++) {
            self::$P2S[] = $currentP2;
            $currentP2 = $currentP2->multiply(self::$P2);
        }

        self::$NEG_P2_MINUS_15 = new Matrix(0, 2, 2, 1);

        $rawEncoding = [
            '0' => 0, '1' => 16, '2' => 32,
            '3' => 4, '4' => 20, '5' => 36,
            '6' => 8, '7' => 24, '8' => 40,
            '9' => 2, 'A' => 18, 'B' => 34,
            'C' => 6, 'D' => 22, 'E' => 38,
            'F' => 10, 'G' => 26, 'H' => 42,
            'I' => 1, 'J' => 17, 'K' => 33,
            'L' => 5, 'M' => 21, 'N' => 37,
            'O' => 9, 'P' => 25, 'Q' => 41,
            'R' => 3, 'S' => 19, 'T' => 35,
            'U' => 7, 'V' => 23, 'W' => 39,
            'X' => 11, 'Y' => 27, 'Z' => 43
        ];

        self::$ENCODING = [];
        foreach ($rawEncoding as $char => $value) {
            self::$ENCODING[(string) $char] = self::decodeIntToMatrix($value);
        }

        self::$initialized = true;
    }

    private static function decodeIntToMatrix(int $x): Matrix
    {
        return new Matrix(
            $x & 1,
            ($x >> 1) & 1,
            ($x >> 2) & 3,
            $x >> 4
        );
    }

    private static function createCallbackForT1(): callable
    {
        return fn (CheckDigitIso\Matrix $m) => new CheckDigitIso\Vec($m->m11, $m->m12);
    }

    private static function createCallbackForT2(): callable
    {
        return fn (CheckDigitIso\Matrix $m) => new CheckDigitIso\Vec($m->m21, $m->m22);
    }

    /**
     * @param array<CheckDigitIso\Matrix> $ps
     * @param callable(CheckDigitIso\Matrix): CheckDigitIso\Vec $f
     */
    private static function sumEq(
        array $ps,
        callable $f,
        string $code
    ): CheckDigitIso\Vec {
        $v = new CheckDigitIso\Vec(0, 0);
        for ($i = 0; $i < count($ps); $i++) {
            $char = $code[$i];
            if (! isset(self::$ENCODING[$char])) {
                throw new InvalidArgumentException("Invalid character: $char.");
            }
            $qr = $f(self::$ENCODING[$char]);
            $v = $v->add($qr->multiply($ps[$i]));
        }

        return $v;
    }

    /**
     * Calculate ISO check digit.
     *
     * @param string $code The input code string (e.g., "NN123ABCDEFGHI").
     * @return string The calculated check digit character.
     * @throws InvalidArgumentException If the code is malformed.
     */
    public static function calculate(string $code): string
    {
        self::initialize();

        $code = strtoupper($code);
        $codeLength = strlen($code);

        if ($codeLength !== 14) {
            throw new InvalidArgumentException(
                "Code must have a length of 14. (Was: $codeLength)"
            );
        }

        // Check for ASCII uppercase letters and digits
        if (! preg_match('/^[A-Z0-9]+$/', $code)) {
            throw new InvalidArgumentException(
                "Code must consist of uppercase ASCII letters and digits. (Was: $code)"
            );
        }

        $t1 = self::sumEq(self::$P1S, self::createCallbackForT1(), $code);
        $t2 = self::sumEq(self::$P2S, self::createCallbackForT2(), $code)->multiply(self::$NEG_P2_MINUS_15);

        $m15 = new CheckDigitIso\Matrix($t1->v1 & 1, $t1->v2 & 1, $t2->v1 % 3, $t2->v2 % 3);

        // Find the matching char in decoding map
        foreach (self::$ENCODING as $char => $matrix) {
            if ($m15->equals($matrix)) {
                return (string) $char; // Cast to string
            }
        }


        // Fallback if not found (shouldn't happen with valid input and correct logic)
        throw new InvalidArgumentException("Undecodable matrix: ({$m15->m11},{$m15->m12},{$m15->m21},{$m15->m22}).");
    }
}
