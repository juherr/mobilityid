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
use Juherr\MobilityId\EvseId;
use Juherr\MobilityId\EvseIdDin;
use Juherr\MobilityId\EvseIdIso;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class EvseIdTest extends TestCase
{
    // Tests for EvseId::of(string $evseIdString)

    public function testOfValidIsoEvseIdString(): void
    {
        $evseIdString = "DE*AB7*E840*6487";
        $evseId = EvseId::of($evseIdString);
        $this->assertInstanceOf(EvseIdIso::class, $evseId);
        $this->assertSame("DE", (string) $evseId->countryCode);
        $this->assertSame("AB7", (string) $evseId->operatorId);
        $this->assertSame("840*6487", $evseId->powerOutletId);
        $this->assertSame($evseIdString, (string) $evseId);
    }

    public function testOfValidDinEvseIdString(): void
    {
        $evseIdString = "+49*810*000*438";
        $evseId = EvseId::of($evseIdString);
        $this->assertInstanceOf(EvseIdDin::class, $evseId);
        $this->assertSame("+49", (string) $evseId->countryCode);
        $this->assertSame("810", (string) $evseId->operatorId);
        $this->assertSame("000*438", $evseId->powerOutletId);
        $this->assertSame($evseIdString, (string) $evseId);
    }

    #[DataProvider('provideInvalidEvseIdStrings')]
    public function testOfInvalidEvseIdString(string $evseIdString): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/is not a valid ISO EVSE ID.*and not a valid DIN EVSE ID/");
        EvseId::of($evseIdString);
    }

    public static function provideInvalidEvseIdStrings(): array
    {
        return [
            ['INVALID-EVSE-ID'], // Arbitrary invalid string
            ['NL*TNM*840*6487'], // Looks like ISO but missing 'E'
            ['DE*AB7*E' . str_repeat('7', 32)], // Too long ISO power outlet ID
            ['+49*810*123456789012345678901234567890123'], // Too long DIN power outlet ID
        ];
    }

    public function testOptValidEvseIdString(): void
    {
        $evseIdString = "DE*AB7*E840*6487";
        $evseId = EvseId::opt($evseIdString);
        $this->assertNotNull($evseId);
        $this->assertInstanceOf(EvseIdIso::class, $evseId);
    }

    public function testOptInvalidEvseIdString(): void
    {
        $evseIdString = "INVALID-EVSE-ID";
        $evseId = EvseId::opt($evseIdString);
        $this->assertNull($evseId);
    }

    // Tests for EvseId::ofParts(string $countryCodeString, string $operatorIdString, string $powerOutletIdString)

    public function testOfPartsValidIso(): void
    {
        $evseId = EvseId::ofParts("NL", "TNM", "E840*6487");
        $this->assertInstanceOf(EvseIdIso::class, $evseId);
        $this->assertSame("NL", (string) $evseId->countryCode);
        $this->assertSame("TNM", (string) $evseId->operatorId);
        $this->assertSame("840*6487", $evseId->powerOutletId);
        $this->assertSame("NL*TNM*E840*6487", (string) $evseId);
    }

    public function testOfPartsValidIsoWithoutEPrefix(): void
    {
        $evseId = EvseId::ofParts("NL", "TNM", "840*6487");
        $this->assertInstanceOf(EvseIdIso::class, $evseId);
        $this->assertSame("840*6487", $evseId->powerOutletId);
        $this->assertSame("NL*TNM*E840*6487", (string) $evseId);
    }

    public function testOfPartsValidDin(): void
    {
        $evseId = EvseId::ofParts("+31", "745", "840*6487");
        $this->assertInstanceOf(EvseIdDin::class, $evseId);
        $this->assertSame("+31", (string) $evseId->countryCode);
        $this->assertSame("745", (string) $evseId->operatorId);
        $this->assertSame("840*6487", $evseId->powerOutletId);
    }

    public function testOfPartsRejectMixedFormats(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Cannot create EVSE ID from parts\. Invalid for ISO .*and invalid for DIN .*/");
        EvseId::ofParts("+31", "ABC", "840*6487"); // DIN country code, ISO operator ID
    }

    public function testOfPartsRejectWrongLengths(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/Cannot create EVSE ID from parts\. Invalid for ISO .*and invalid for DIN .*/");
        EvseId::ofParts("A", "TNM", "000122045"); // Invalid country code (too short for ISO/DIN)
    }

    public function testRenderingCaseInsensitive(): void
    {
        $evseId = EvseId::of("Nl*tnM*E000122045");
        $this->assertInstanceOf(EvseIdIso::class, $evseId);
        $this->assertSame("NL", (string) $evseId->countryCode);
        $this->assertSame("TNM", (string) $evseId->operatorId);
        $this->assertSame("000122045", $evseId->powerOutletId);
        $this->assertSame("NL*TNM*E000122045", (string) $evseId);
    }

    public function testRenderingDinEvseIdFromFactory(): void
    {
        // Test that DIN EVSE ID renders correctly when created via factory
        // This mirrors Scala test: EvseId("+31*745*840*6487").get.toString = "+31*745*840*6487"
        $evseId = EvseId::of("+31*745*840*6487");
        $this->assertInstanceOf(EvseIdDin::class, $evseId);
        $this->assertSame("+31*745*840*6487", (string) $evseId);

        // Also test via ofParts
        $evseId2 = EvseId::ofParts("+31", "745", "840*6487");
        $this->assertInstanceOf(EvseIdDin::class, $evseId2);
        $this->assertSame("+31*745*840*6487", (string) $evseId2);
    }
}
