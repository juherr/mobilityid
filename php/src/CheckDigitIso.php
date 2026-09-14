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

use Juherr\MobilityId\CheckDigitIso\Matrix;
use Juherr\MobilityId\CheckDigitIso\Vec;

final class CheckDigitIso
{
    private const int LENGTH = 14;

    /**
     * ISO 15118-1 Annex A encoding table: each character maps to a 2x2 matrix over the integers,
     * packed as bits (m11 = bit 0, m12 = bit 1, m21 = bits 2-3, m22 = bits 4+).
     */
    private const array ENCODING = [
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
        'X' => 11, 'Y' => 27, 'Z' => 43,
    ];

    private function __construct() {} // Prevent instantiation

    /**
     * Calculate ISO check digit.
     *
     * @param string $code The input code string (e.g., "NN123ABCDEFGHI").
     *
     * @return string the calculated check digit character
     *
     * @throws \InvalidArgumentException if the code is malformed
     */
    public static function calculate(string $code): string
    {
        $code = strtoupper($code);
        $codeLength = \strlen($code);

        if ($codeLength !== self::LENGTH) {
            throw new \InvalidArgumentException(
                'Code must have a length of ' . self::LENGTH . ". (Was: {$codeLength})"
            );
        }

        // Check for ASCII uppercase letters and digits
        if (preg_match('/^[A-Z0-9]+$/', $code) !== 1) {
            throw new \InvalidArgumentException(
                "Code must consist of uppercase ASCII letters and digits. (Was: {$code})"
            );
        }

        // The tables are cheap to rebuild (14 matrix products), so nothing is cached: a static
        // cache would only be exercised by the first test of a process and hide table mutations
        // from mutation testing.
        $p1s = self::powers(new Matrix(0, 1, 1, 1));
        $p2s = self::powers(new Matrix(0, 1, 1, 2));
        $negP2Minus15 = new Matrix(0, 2, 2, 1);

        $t1 = self::sumEq($p1s, static fn (Matrix $m): Vec => new Vec($m->m11, $m->m12), $code);
        $t2 = self::sumEq($p2s, static fn (Matrix $m): Vec => new Vec($m->m21, $m->m22), $code)->multiply($negP2Minus15);

        $m15 = new Matrix($t1->v1 & 1, $t1->v2 & 1, $t2->v1 % 3, $t2->v2 % 3);

        // Find the matching char in decoding map
        foreach (self::ENCODING as $char => $value) {
            if ($m15->equals(self::decode($value))) {
                return (string) $char; // Integer-like keys ('0'..'9') are stored as int by PHP
            }
        }

        // Fallback if not found (shouldn't happen with valid input and correct logic)
        throw new \InvalidArgumentException("Undecodable matrix: ({$m15->m11},{$m15->m12},{$m15->m21},{$m15->m22}).");
    }

    /**
     * @return list<Matrix> $base^1 .. $base^LENGTH
     */
    private static function powers(Matrix $base): array
    {
        $powers = [];
        $current = $base;
        for ($i = 0; $i < self::LENGTH; ++$i) {
            $powers[] = $current;
            $current = $current->multiply($base);
        }

        return $powers;
    }

    private static function decode(int $x): Matrix
    {
        return new Matrix(
            $x & 1,
            ($x >> 1) & 1,
            ($x >> 2) & 3,
            $x >> 4
        );
    }

    /**
     * @param list<Matrix>          $ps
     * @param callable(Matrix): Vec $f
     */
    private static function sumEq(array $ps, callable $f, string $code): Vec
    {
        $v = new Vec(0, 0);
        foreach ($ps as $i => $p) {
            $char = $code[$i];
            if (! isset(self::ENCODING[$char])) {
                throw new \InvalidArgumentException("Invalid character: {$char}.");
            }
            $qr = $f(self::decode(self::ENCODING[$char]));
            $v = $v->add($qr->multiply($p));
        }

        return $v;
    }
}
