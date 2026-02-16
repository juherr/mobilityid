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

namespace Juherr\MobilityId\Tests;

use InvalidArgumentException;
use Juherr\MobilityId\CheckDigitIso;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class CheckDigitIsoTest extends TestCase
{
    #[DataProvider('provideContractIds')]
    public function testCalculateCheckDigits(string $contractId, string $expectedCheckDigit): void
    {
        $this->assertSame($expectedCheckDigit, CheckDigitIso::calculate($contractId));
    }

    public static function provideContractIds(): array
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

    #[DataProvider('provideMalformedInput')]
    public function testFailOnMalformedInput(string $input): void
    {
        $this->expectException(InvalidArgumentException::class);
        CheckDigitIso::calculate($input);
    }

    public static function provideMalformedInput(): array
    {
        return [
            ['Европарулит123'],       // Non-ASCII
            ['DE٨٣DUIEN٨٣QGZ'],     // Non-ASCII
            ['Å∏@*(Td\uD83D\uDE3BgaR^&(%'], // Non-ASCII
            ['Å∏@*(Td\uD83D\uDE3BgR^&(%'], // Non-ASCII
            [''],                       // Empty
            ['DE8AA0012345678'],        // Too long
            ['DE8AA00123456'],          // Too short (if length should be 14)
        ];
    }
}
