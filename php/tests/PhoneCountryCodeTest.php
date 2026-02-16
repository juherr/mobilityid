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
use Juherr\MobilityId\PhoneCountryCode;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class PhoneCountryCodeTest extends TestCase
{
    #[DataProvider('provideValidPhoneCountryCodes')]
    public function testValidPhoneCountryCodes(string $code, ?string $expectedCode = null): void
    {
        $phoneCountryCode = PhoneCountryCode::of($code);
        $this->assertSame($expectedCode ?? $code, (string) $phoneCountryCode);
        $this->assertTrue(PhoneCountryCode::isValid($code));
    }

    public static function provideValidPhoneCountryCodes(): array
    {
        return [
            ['+1'],
            ['+31'],
            ['+49'],
            ['+999'],
            ['1', '+1'], // Without '+'
            ['31', '+31'], // Without '+'
            ['49', '+49'], // Without '+'
            ['999', '+999'], // Without '+'
        ];
    }

    #[DataProvider('provideInvalidPhoneCountryCodes')]
    public function testInvalidPhoneCountryCodes(string $code): void
    {
        $this->assertFalse(PhoneCountryCode::isValid($code));
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Phone Country Code must start with a '\\+' sign and be followed by 1-3 digits\\. \\(Was: .*\\)/");
        PhoneCountryCode::of($code);
    }

    public static function provideInvalidPhoneCountryCodes(): array
    {
        return [
            ['+'],       // Only '+'
            ['+1234'],   // Too long
            ['1234'],    // Too long, no '+'
            ['+A'],      // Contains letter
            ['+ 1'],     // Contains space
            ['++1'],     // Double '+'
            [''],        // Empty
            [' '],       // Space
            ['abc'],     // Letters only
        ];
    }
}
