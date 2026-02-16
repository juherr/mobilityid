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
use Juherr\MobilityId\OperatorIdDin;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class OperatorIdDinTest extends TestCase
{
    #[DataProvider('provideValidOperatorIdsDin')]
    public function testValidOperatorIdsDin(string $id): void
    {
        $operatorIdDin = OperatorIdDin::of($id);
        $this->assertSame($id, (string) $operatorIdDin);
        $this->assertTrue(OperatorIdDin::isValid($id));
    }

    public static function provideValidOperatorIdsDin(): array
    {
        return [
            ['123'],     // 3 digits
            ['123456'],  // 6 digits
            ['000'],     // All zeros
            ['999999'],  // All nines
        ];
    }

    #[DataProvider('provideInvalidOperatorIdsDin')]
    public function testInvalidOperatorIdsDin(string $id): void
    {
        $this->assertFalse(OperatorIdDin::isValid($id));
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Operator ID \(DIN\) must be a numeric string between 3 and 6 digits\. \(Was: .*\)/");
        OperatorIdDin::of($id);
    }

    public static function provideInvalidOperatorIdsDin(): array
    {
        return [
            ['12'],        // Less than 3 digits
            ['1234567'],   // More than 6 digits
            ['12A'],       // Contains letters
            ['12 3'],      // Contains space
            [''],          // Empty
            [' '],         // Space
        ];
    }
}
