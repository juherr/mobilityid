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

use Juherr\MobilityId\CountryCode;
use Juherr\MobilityId\EvseId;
use Juherr\MobilityId\EvseIdIso;
use Juherr\MobilityId\OperatorIdIso;
use PHPUnit\Framework\TestCase;

final class EvseIdIsoTest extends TestCase
{
    // "Accept an ISO EvseId String with separators"
    public function testOfValidIsoEvseIdStringWithSeparators(): void
    {
        $evseIdString = 'DE*AB7*E840*6487';
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('AB7', (string) $evseId->operatorId);
        self::assertSame('840*6487', $evseId->powerOutletId);
        self::assertSame($evseIdString, (string) $evseId);
    }

    // "Accept an ISO EvseId String without separators"
    public function testOfValidIsoEvseIdStringWithoutSeparators(): void
    {
        $evseIdString = 'DEAB7E8406487';
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('AB7', (string) $evseId->operatorId);
        self::assertSame('8406487', $evseId->powerOutletId);
        self::assertSame('DE*AB7*E8406487', (string) $evseId); // Normalized output
    }

    // "Accept a minimum length ISO EvseId String"
    public function testOfMinimumLengthIsoEvseIdString(): void
    {
        $evseIdString = 'DEAB7E1'; // Power outlet ID min 1 char
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('AB7', (string) $evseId->operatorId);
        self::assertSame('1', $evseId->powerOutletId);
    }

    // "Accept a maximum length ISO EvseId String"
    public function testOfMaximumLengthIsoEvseIdString(): void
    {
        $powerOutletId = str_repeat('1', 31); // Max 31 chars
        $evseIdString = 'DEAB7E' . $powerOutletId;
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('AB7', (string) $evseId->operatorId);
        self::assertSame($powerOutletId, $evseId->powerOutletId);
    }

    // "Accept asterisk directly after the E"
    public function testOfIsoEvseIdStringWithAsteriskAfterE(): void
    {
        $evseIdString = 'DE*DES*E*BMW*0113*2';
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('DES', (string) $evseId->operatorId);
        self::assertSame('*BMW*0113*2', $evseId->powerOutletId);
    }

    // "Accept DE*TNM*ETWL*HEDWIGLAUDIENRING*LS12001*0"
    public function testOfSpecificIsoEvseIdString(): void
    {
        $evseIdString = 'DE*TNM*ETWL*HEDWIGLAUDIENRING*LS12001*0';
        $evseId = EvseIdIso::of($evseIdString);
        self::assertSame('DE', (string) $evseId->countryCode);
        self::assertSame('TNM', (string) $evseId->operatorId);
        self::assertSame('TWL*HEDWIGLAUDIENRING*LS12001*0', $evseId->powerOutletId); // PowerOutletId is E + TWL...
    }

    // "Reject an ISO EvseId String that is too long"
    public function testOfRejectTooLongIsoEvseIdString(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid ISO EVSE ID/");
        $powerOutletId = str_repeat('7', 32); // Max 31 chars
        EvseIdIso::of('DE*AB7*E' . $powerOutletId);
    }

    // "Reject an ISO EvseId String with incorrect powerOutletId (must begin with E)"
    public function testOfRejectIsoEvseIdStringWithoutEPrefix(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid ISO EVSE ID/");
        EvseIdIso::of('NL*TNM*840*6487');
    }

    // "Reject to construct an EvseIdIso directly from valid DIN String"
    public function testOfRejectIsoEvseIdStringFromValidDinString(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches("/'.*?' is not a valid ISO EVSE ID/");
        EvseIdIso::of('+49*810*000*438');
    }

    // "Reject to construct with invalid country code"
    public function testOfRejectIsoEvseIdStringWithInvalidCountryCode(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Country Code must be valid according to ISO 3166-1 alpha-2/');
        EvseIdIso::of('ZZ*TNM*E840*64878');
    }

    // "Accept valid combination of ISO parameters"
    public function testOfPartsValidIsoEvseId(): void
    {
        $countryCode = CountryCode::of('NL');
        $operatorId = OperatorIdIso::of('TNM');
        $powerOutletId = '840*6487';
        $evseId = EvseIdIso::ofParts($countryCode, $operatorId, $powerOutletId);
        self::assertSame('NL', (string) $evseId->countryCode);
        self::assertSame('TNM', (string) $evseId->operatorId);
        self::assertSame('840*6487', $evseId->powerOutletId);
    }

    public function testOfPartsNormalizesPowerOutletIdToUppercase(): void
    {
        $evseId = EvseIdIso::ofParts(CountryCode::of('NL'), OperatorIdIso::of('TNM'), 'e84a*6b87');
        self::assertSame('84A*6B87', $evseId->powerOutletId);
        self::assertSame('NL*TNM*E84A*6B87', (string) $evseId);
    }

    public function testOfPartsAcceptsThirtyCharactersAfterTheIdType(): void
    {
        $evseId = EvseIdIso::ofParts(CountryCode::of('NL'), OperatorIdIso::of('TNM'), str_repeat('1', 30));
        self::assertSame('NL*TNM*E' . str_repeat('1', 30), (string) $evseId);
    }

    public function testOfPartsRejectsThirtyOneCharactersAfterTheIdType(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage("'E" . str_repeat('1', 31) . "' is not a valid ISO Power Outlet ID");
        EvseIdIso::ofParts(CountryCode::of('NL'), OperatorIdIso::of('TNM'), str_repeat('1', 31));
    }

    public function testOfPartsValidIsoEvseIdWithEPrefix(): void
    {
        $countryCode = CountryCode::of('NL');
        $operatorId = OperatorIdIso::of('TNM');
        $evseId = EvseIdIso::ofParts($countryCode, $operatorId, 'E840*6487');
        self::assertSame('840*6487', $evseId->powerOutletId);
        self::assertSame('NL*TNM*E840*6487', (string) $evseId);
    }

    public function testOfNormalizesPowerOutletIdToUppercase(): void
    {
        $evseId = EvseIdIso::of('NL*TNM*Eab12');
        self::assertSame('AB12', $evseId->powerOutletId);
        self::assertSame('NL*TNM*EAB12', (string) $evseId);
    }

    // "Reject EvseIdDin's country/operator codes when creating EvseIdIso"
    public function testOfPartsRejectDinCodesForIso(): void
    {
        // This test actually checks a valid ISO EvseId creation.
        // The previous comment was misleading.
        $countryCode = CountryCode::of('NL');
        $operatorId = OperatorIdIso::of('TNM');
        $powerOutletId = '840*6487';
        $evseId = EvseIdIso::ofParts($countryCode, $operatorId, $powerOutletId);
        self::assertSame('NL', (string) $evseId->countryCode);
        self::assertSame('TNM', (string) $evseId->operatorId);
        self::assertSame('840*6487', $evseId->powerOutletId);
    }

    // "Render an EvseId in the Compact ISO form without asterisks"
    public function testToCompactString(): void
    {
        $evseId = EvseIdIso::ofParts(CountryCode::of('NL'), OperatorIdIso::of('TNM'), '840*6487');
        self::assertSame('NLTNME8406487', $evseId->toCompactString());
    }

    // "expose operator's party ID"
    public function testPartyId(): void
    {
        $evseId = EvseIdIso::ofParts(CountryCode::of('NL'), OperatorIdIso::of('TNM'), '840*6487');
        $partyId = $evseId->partyId();
        self::assertSame('NL-TNM', (string) $partyId);
    }

    // Additional test for opt()
    public function testOptValidIsoEvseIdString(): void
    {
        $evseIdString = 'DE*AB7*E840*6487';
        $evseId = EvseIdIso::opt($evseIdString);
        self::assertInstanceOf(EvseIdIso::class, $evseId);
        self::assertSame('DE', (string) $evseId->countryCode);
    }

    public function testOptInvalidIsoEvseIdString(): void
    {
        $evseIdString = 'DE*AB7*840*6487'; // Missing E prefix
        $evseId = EvseIdIso::opt($evseIdString);
        self::assertNull($evseId);
    }
}
