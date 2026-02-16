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
use Juherr\MobilityId\EvseId;
use Juherr\MobilityId\EvseIdDin;
use Juherr\MobilityId\OperatorIdDin;
use Juherr\MobilityId\PhoneCountryCode;
use PHPUnit\Framework\TestCase;

class EvseIdDinTest extends TestCase
{
    // "Accept a DIN EvseId String"
    public function testOfValidDinEvseIdString(): void
    {
        $evseIdString = "+49*810*000*438";
        $evseId = EvseIdDin::of($evseIdString);
        $this->assertSame("+49", (string) $evseId->countryCode);
        $this->assertSame("810", (string) $evseId->operatorId);
        $this->assertSame("000*438", $evseId->powerOutletId);
        $this->assertSame($evseIdString, (string) $evseId);
    }

    // "Accept a minimum length DIN EvseId String"
    public function testOfMinimumLengthDinEvseIdString(): void
    {
        $evseIdString = "+49*810*1"; // Power outlet ID min 1 char
        $evseId = EvseIdDin::of($evseIdString);
        $this->assertSame("+49", (string) $evseId->countryCode);
        $this->assertSame("810", (string) $evseId->operatorId);
        $this->assertSame("1", $evseId->powerOutletId);
    }

    // "Accept a maximum length DIN EvseId String"
    public function testOfMaximumLengthDinEvseIdString(): void
    {
        $powerOutletId = str_repeat('1', 32); // Max 32 chars
        $evseIdString = "+49*810*" . $powerOutletId;
        $evseId = EvseIdDin::of($evseIdString);
        $this->assertSame("+49", (string) $evseId->countryCode);
        $this->assertSame("810", (string) $evseId->operatorId);
        $this->assertSame($powerOutletId, $evseId->powerOutletId);
    }

    // "Reject an DIN EvseId String that is too long"
    public function testOfRejectTooLongDinEvseIdString(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid DIN EVSE ID/");
        $powerOutletId = str_repeat('7', 33); // Max 32 chars
        EvseIdDin::of("+49*810*$powerOutletId");
    }

    // "Reject a DIN EvseId String with incorrect operator id"
    public function testOfRejectDinEvseIdStringWithIncorrectOperatorId(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid DIN EVSE ID/");
        EvseIdDin::of("+49*AB7*840*6487"); // AB7 is ISO operator, not DIN
    }

    // "Reject a DIN EvseId String with incorrect powerOutletId"
    public function testOfRejectDinEvseIdStringWithIncorrectPowerOutletId(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid DIN EVSE ID/");
        EvseIdDin::of("+49*645*E840*6487"); // Power outlet ID should not start with E
    }

    // "Reject to construct an EvseIdDin directly from valid ISO String"
    public function testOfRejectDinEvseIdStringFromValidIsoString(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid DIN EVSE ID/");
        EvseIdDin::of("DE*AB7*E840*6487");
    }

    // "Accept country codes with and without plus sign"
    public function testOfDinEvseIdStringWithAndWithoutPlusSign(): void
    {
        $evseIdWithPlus = EvseIdDin::of("+49*810*000*438");
        $evseIdWithoutPlus = EvseIdDin::of("49*810*000*438");
        $this->assertEquals($evseIdWithPlus, $evseIdWithoutPlus);
    }

    // "Reject to construct a DIN EvseId with invalid country code"
    public function testOfRejectDinEvseIdStringWithInvalidCountryCode(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid DIN EVSE ID/");
        EvseIdDin::of("+4A*810*000*438"); // +4A is invalid
    }

    // "Accept a DIN EvseId String with operator of 6 chars"
    public function testOfDinEvseIdStringWith6CharOperator(): void
    {
        $evseIdString = "+49*810548*1234567890";
        $evseId = EvseIdDin::of($evseIdString);
        $this->assertSame("+49", (string) $evseId->countryCode);
        $this->assertSame("810548", (string) $evseId->operatorId);
        $this->assertSame("1234567890", $evseId->powerOutletId);
    }

    // "Accept valid combination of DIN parameters when creating EvseIdDin directly"
    public function testOfPartsValidDinEvseId(): void
    {
        $countryCode = PhoneCountryCode::of("+31");
        $operatorId = OperatorIdDin::of("745");
        $powerOutletId = "840*6487";
        $evseId = EvseIdDin::ofParts($countryCode, $operatorId, $powerOutletId);
        $this->assertSame("+31", (string) $evseId->countryCode);
        $this->assertSame("745", (string) $evseId->operatorId);
        $this->assertSame("840*6487", $evseId->powerOutletId);
    }

    // "Reject EvseIdIso's country/operator codes when creating EvseIdDin"
    public function testOfPartsRejectIsoCodesForDin(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Country code must be of type PhoneCountryCode for DIN EVSE ID/");
        // Using CountryCode for country code, which is invalid for DIN
        EvseIdDin::ofParts(CountryCode::of("NL"), OperatorIdDin::of("745"), "840*6487");
    }

    // Additional test for opt()
    public function testOptValidDinEvseIdString(): void
    {
        $evseIdString = "+49*810*000*438";
        $evseId = EvseIdDin::opt($evseIdString);
        $this->assertNotNull($evseId);
        $this->assertSame("+49", (string) $evseId->countryCode);
    }

    public function testOptInvalidDinEvseIdString(): void
    {
        $evseIdString = "DE*AB7*E840*6487"; // ISO format
        $evseId = EvseIdDin::opt($evseIdString);
        $this->assertNull($evseId);
    }
}
