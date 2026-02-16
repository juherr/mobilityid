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
use Juherr\MobilityId\CountryCode;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class CountryCodeTest extends TestCase
{
    #[DataProvider('provideValidCountryCodes')]
    public function testValidCountryCodes(string $code, string $expectedOutput): void
    {
        $countryCode = CountryCode::of($code);
        $this->assertSame($expectedOutput, (string) $countryCode);
    }

    public static function provideValidCountryCodes(): array
    {
        return [
            ['NL', 'NL'],
            ['BE', 'BE'],
            ['DE', 'DE'],
            ['US', 'US'],
            ['fr', 'FR'], // Case insensitivity
            ['gB', 'GB'], // Case insensitivity
        ];
    }

    #[DataProvider('provideInvalidFormatCountryCodes')]
    public function testInvalidFormatCountryCodes(string $code): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Country Code must be valid according to ISO 3166-1 alpha-2\./');
        CountryCode::of($code);
    }

    public static function provideInvalidFormatCountryCodes(): array
    {
        return [
            ['NLD'], // Too long
            ['N'],   // Too short
            ['NL1'], // Contains numbers
            ['N L'], // Contains space
            [''],    // Empty
            [' '],   // Space
        ];
    }

    #[DataProvider('provideNonExistentCountryCodes')]
    public function testNonExistentCountryCodes(string $code): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Country Code must be valid according to ISO 3166-1 alpha-2\./');
        CountryCode::of($code);
    }

    public static function provideNonExistentCountryCodes(): array
    {
        return [
            ['XX'], // Not a real country code
            ['ZZ'], // Not a real country code
        ];
    }
}
