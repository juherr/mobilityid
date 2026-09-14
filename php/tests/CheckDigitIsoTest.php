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

use Juherr\MobilityId\CheckDigitIso;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

final class CheckDigitIsoTest extends TestCase
{
    #[DataProvider('provideCalculateCheckDigitsCases')]
    public function testCalculateCheckDigits(string $contractId, string $expectedCheckDigit): void
    {
        self::assertSame($expectedCheckDigit, CheckDigitIso::calculate($contractId));
    }

    public static function provideCalculateCheckDigitsCases(): iterable
    {
        return [
            ['NN123ABCDEFGHI', 'T'],
            ['FRXYZ123456789', '2'],
            ['ITA1B2C3E4F5G6', '4'],
            ['ESZU8WOX834H1D', 'R'],
            ['PT73902837ABCZ', 'Z'],
            ['DE83DUIEN83QGZ', 'D'],
            ['DE83DUIEN83ZGQ', 'M'],
            ['DE8AA001234567', '0'],
        ];
    }

    #[DataProvider('provideFailOnMalformedInputCases')]
    public function testFailOnMalformedInput(string $input, string $expectedMessage): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage($expectedMessage);
        CheckDigitIso::calculate($input);
    }

    public static function provideFailOnMalformedInputCases(): iterable
    {
        return [
            ['Европарулит123', 'Code must have a length of 14. (Was: 25)'],       // Non-ASCII
            ['DE٨٣DUIEN٨٣QGZ', 'Code must have a length of 14. (Was: 18)'],     // Non-ASCII
            ['Å∏@*(Td\uD83D\uDE3BgaR^&(%', 'Code must have a length of 14. (Was: 29)'], // Non-ASCII
            ['Å∏@*(Td\uD83D\uDE3BgR^&(%', 'Code must have a length of 14. (Was: 28)'], // Non-ASCII
            ['', 'Code must have a length of 14. (Was: 0)'],                       // Empty
            ['DE8AA0012345678', 'Code must have a length of 14. (Was: 15)'],        // Too long
            ['DE8AA00123456', 'Code must have a length of 14. (Was: 13)'],          // Too short (if length should be 14)
            ['DE8AA0012345 7', 'Code must consist of uppercase ASCII letters and digits. (Was: DE8AA0012345 7)'], // Space inside
            [' E8AA00123456', 'Code must have a length of 14. (Was: 13)'],          // Leading space, wrong length
            [' E8AA001234567', 'Code must consist of uppercase ASCII letters and digits. (Was:  E8AA001234567)'], // Leading space
            ['DE8AA00123456 ', 'Code must consist of uppercase ASCII letters and digits. (Was: DE8AA00123456 )'], // Trailing space
            ['DE8AA0012345é', 'Code must consist of uppercase ASCII letters and digits. (Was: DE8AA0012345é)'], // Non-ASCII, 14 bytes (strtoupper is byte-based)
        ];
    }

    public function testCalculateIsCaseInsensitive(): void
    {
        self::assertSame('D', CheckDigitIso::calculate('de83duien83qgz'));
    }

    /**
     * 200 payloads with the check digit computed by the TypeScript and Go ports (which agree on
     * every row), so the PHP algorithm is pinned to the other workspaces well beyond the handful of
     * Scala fixtures.
     */
    #[DataProvider('provideCrossLanguageFixturesCases')]
    public function testCrossLanguageFixtures(string $code, string $expectedCheckDigit): void
    {
        self::assertSame($expectedCheckDigit, CheckDigitIso::calculate($code));
    }

    /**
     * @return iterable<string, array{string, string}>
     */
    public static function provideCrossLanguageFixturesCases(): iterable
    {
        $lines = file(__DIR__ . '/fixtures/check-digit-iso.csv', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
        self::assertNotFalse($lines);
        foreach ($lines as $line) {
            [$code, $checkDigit] = explode(',', $line);

            yield $code => [$code, $checkDigit];
        }
    }
}
