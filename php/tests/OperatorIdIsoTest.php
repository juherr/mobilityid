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
use Juherr\MobilityId\OperatorIdIso;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class OperatorIdIsoTest extends TestCase
{
    #[DataProvider('provideValidOperatorIdsIso')]
    public function testValidOperatorIdsIso(string $id, string $expectedId): void
    {
        $operatorIdIso = OperatorIdIso::of($id);
        $this->assertSame($expectedId, (string) $operatorIdIso);
        $this->assertTrue(OperatorIdIso::isValid($id));
    }

    public static function provideValidOperatorIdsIso(): array
    {
        return [
            ['TNM', 'TNM'],
            ['tnm', 'TNM'], // Lowercase input
            ['AB2', 'AB2'], // Alphanumeric
            ['Z9Z', 'Z9Z'], // Alphanumeric
        ];
    }

    #[DataProvider('provideInvalidOperatorIdsIso')]
    public function testInvalidOperatorIdsIso(string $id): void
    {
        $this->assertFalse(OperatorIdIso::isValid($id));
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Operator ID \(ISO\) must have a length of 3 and be ASCII letters or digits\. \(Was: .*\)/");
        OperatorIdIso::of($id);
    }

    public static function provideInvalidOperatorIdsIso(): array
    {
        return [
            ['AB'],      // Less than 3 digits/chars
            ['ABCD'],    // More than 3 digits/chars
            ['AB-2'],    // Invalid character
            ['AB C'],    // Contains space
            [''],        // Empty
            [' '],       // Space
        ];
    }
}
