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

namespace Juherr\MobilityId\Tests;

use Juherr\MobilityId\CheckDigitDin;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

final class CheckDigitDinTest extends TestCase
{
    #[DataProvider('provideCalculateDinCheckDigitsCases')]
    public function testCalculateDinCheckDigits(string $instance, string $expectedCheckDigit): void
    {
        self::assertSame($expectedCheckDigit, CheckDigitDin::calculate('INTNM' . $instance));
    }

    public static function provideCalculateDinCheckDigitsCases(): iterable
    {
        return [
            ['000071', '9'], // calculate(71) === '9' (Scala output)
            ['000110', 'X'], // calculate(110) === 'X' (Scala output)
            ['000124', '0'], // calculate(124) === '0' (Scala output)
            ['000114', '6'], // calculate(114) === '6' (Scala output)
            ['000191', '5'], // calculate(191) === '5' (Scala output)
        ];
    }

    #[DataProvider('provideFailOnMalformedInputCases')]
    public function testFailOnMalformedInput(string $input): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Invalid character in contract ID:/');
        CheckDigitDin::calculate($input);
    }

    public static function provideFailOnMalformedInputCases(): iterable
    {
        return [
            ['INTNM00007!'], // Contains invalid character
            ['INTNMÅÅÅÅÅÅ'], // Contains non-ASCII character
            ['INTNM '],     // Contains space
        ];
    }

    public function testCalculateIsCaseInsensitive(): void
    {
        self::assertSame(CheckDigitDin::calculate('INTNM000071'), CheckDigitDin::calculate('intnm000071'));
    }

    /**
     * 200 payloads with the check digit computed by the TypeScript and Go ports (which agree on
     * every row), so the PHP algorithm is pinned to the other workspaces well beyond the handful of
     * Scala fixtures.
     */
    #[DataProvider('provideCrossLanguageFixturesCases')]
    public function testCrossLanguageFixtures(string $code, string $expectedCheckDigit): void
    {
        self::assertSame($expectedCheckDigit, CheckDigitDin::calculate($code));
    }

    /**
     * @return iterable<string, array{string, string}>
     */
    public static function provideCrossLanguageFixturesCases(): iterable
    {
        $lines = file(__DIR__ . '/fixtures/check-digit-din.csv', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
        self::assertNotFalse($lines);
        foreach ($lines as $line) {
            [$code, $checkDigit] = explode(',', $line);

            yield $code => [$code, $checkDigit];
        }
    }
}
