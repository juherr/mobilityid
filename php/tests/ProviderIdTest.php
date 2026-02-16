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
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class ProviderIdTest extends TestCase
{
    #[DataProvider('provideValidProviderIds')]
    public function testValidProviderIds(string $id, string $expectedId): void
    {
        $providerId = ProviderId::of($id);
        $this->assertSame($expectedId, (string) $providerId);
        $this->assertTrue(ProviderId::isValid($id));
    }

    public static function provideValidProviderIds(): array
    {
        return [
            ['TNM', 'TNM'],
            ['tnm', 'TNM'], // Lowercase input
            ['A1B', 'A1B'], // Alphanumeric
            ['Z9Z', 'Z9Z'], // Alphanumeric
        ];
    }

    #[DataProvider('provideInvalidProviderIds')]
    public function testInvalidProviderIds(string $id): void
    {
        $this->assertFalse(ProviderId::isValid($id));
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Provider ID must have a length of 3 and be ASCII letters or digits\. \(Was: .*\)/");
        ProviderId::of($id);
    }

    public static function provideInvalidProviderIds(): array
    {
        return [
            ['TNM1'],    // Too long
            ['TN'],      // Too short
            ['TN-M'],    // Invalid character
            ['TN M'],    // Contains space
            [''],        // Empty
            [' '],       // Space
        ];
    }
}
