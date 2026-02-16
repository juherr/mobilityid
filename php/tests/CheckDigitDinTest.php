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
use Juherr\MobilityId\CheckDigitDin;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class CheckDigitDinTest extends TestCase
{
    #[DataProvider('provideContractIdsForDinCalculation')]
    public function testCalculateDinCheckDigits(string $instance, string $expectedCheckDigit): void
    {
        $this->assertSame($expectedCheckDigit, CheckDigitDin::calculate("INTNM" . $instance));
    }

    public static function provideContractIdsForDinCalculation(): array
    {
        return [
            ['000071', '9'], // calculate(71) === '9' (Scala output)
            ['000110', 'X'], // calculate(110) === 'X' (Scala output)
            ['000124', '0'], // calculate(124) === '0' (Scala output)
            ['000114', '6'], // calculate(114) === '6' (Scala output)
            ['000191', '5'], // calculate(191) === '5' (Scala output)
        ];
    }

    #[DataProvider('provideMalformedInput')]
    public function testFailOnMalformedInput(string $input): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Invalid character in contract ID:/');
        CheckDigitDin::calculate($input);
    }

    public static function provideMalformedInput(): array
    {
        return [
            ['INTNM00007!'], // Contains invalid character
            ['INTNMÅÅÅÅÅÅ'], // Contains non-ASCII character
            ['INTNM '],     // Contains space
        ];
    }
}
