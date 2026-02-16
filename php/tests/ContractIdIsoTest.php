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
use Juherr\MobilityId\ContractIdDin;
use Juherr\MobilityId\ContractIdIso;
use Juherr\MobilityId\CountryCode;
use Juherr\MobilityId\PartyId;
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class ContractIdIsoTest extends TestCase
{
    // Test cases for construction from string
    #[DataProvider('provideValidIsoContractIdStrings')]
    public function testOfValidIsoContractIdStrings(string $contractIdString, string $expectedCountryCode, string $expectedProviderId, string $expectedInstanceValue, string $expectedCheckDigit): void
    {
        $contractId = ContractIdIso::of($contractIdString);
        $this->assertSame($expectedCountryCode, (string) $contractId->countryCode);
        $this->assertSame($expectedProviderId, (string) $contractId->providerId);
        $this->assertSame($expectedInstanceValue, $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidIsoContractIdStrings(): array
    {
        return [
            ['NL-TNM-000122045-U', 'NL', 'TNM', '000122045', 'U'],
            ['NL-TNM-000122045-U', 'NL', 'TNM', '000122045', 'U'], // Duplicate, just to match Scala's List.fill(5)
            ['Nl-TnM-000122045-U', 'NL', 'TNM', '000122045', 'U'], // Case insensitive
            ['nl-TNm-000122045-u', 'NL', 'TNM', '000122045', 'U'], // Case insensitive
            ['NL-TNM-abc123456-Z', 'NL', 'TNM', 'ABC123456', 'Z'], // Normalizes instance value to uppercase
            ['NLTNM000122045', 'NL', 'TNM', '000122045', 'U'],     // No dashes, no check digit provided
        ];
    }

    // Test cases for construction from parts
    #[DataProvider('provideValidIsoContractIdParts')]
    public function testOfPartsValidIsoContractId(
        string $countryCode,
        string $providerId,
        string $instanceValue,
        ?string $checkDigit,
        string $expectedCheckDigit
    ): void {
        $cc = CountryCode::of($countryCode);
        $pi = ProviderId::of($providerId);
        $contractId = ContractIdIso::ofParts($cc, $pi, $instanceValue, $checkDigit);

        $this->assertSame(strtoupper($countryCode), (string) $contractId->countryCode);
        $this->assertSame(strtoupper($providerId), (string) $contractId->providerId);
        $this->assertSame(strtoupper($instanceValue), $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidIsoContractIdParts(): array
    {
        return [
            ['NL', 'TNM', '000122045', 'U', 'U'],
            ['nl', 'tnm', '000122045', 'u', 'U'], // Case insensitive
            ['NL', 'TNM', '000122045', null, 'U'], // No check digit given, should compute
        ];
    }

    // Test cases for invalid input
    #[DataProvider('provideInvalidIsoContractIdStrings')]
    public function testOfInvalidIsoContractIdStrings(string $contractIdString, string $expectedMessageRegex): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches($expectedMessageRegex);
        ContractIdIso::of($contractIdString);
    }

    public static function provideInvalidIsoContractIdStrings(): array
    {
        return [
            ['NL-TNM-000122045-X', "/Given check digit '.*' is not equal to computed '.*'/"], /* Wrong check digit */
            ['NLTNM076', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],                        /* Wrong length for instance value */
            ['X-aargh-131331234', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],               /* Wrong length of fields */
            [' \u0000t24\u2396a	', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],               /* Nonsense */
            ['NL-T|M-000122045-U', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],               /* Illegal char in provider ID */
            ['A-TNM-000122045-U', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],                             /* Invalid country code (too short) */
            ['NLD-TNM-000122045-U', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],                           /* Invalid country code (too long) */
            ['NL-TNMNN-000122045-U', "/'.*?' is not a valid Contract Id for ISO 15118-1/"],                 /* Invalid provider ID (too long) */
        ];
    }

    // Test for rendering
    public function testRendering(): void
    {
        $contractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000722345');
        $this->assertSame('NL-TNM-000722345-X', (string) $contractId);
        $this->assertSame('NLTNM000722345X', $contractId->toCompactString());
        $this->assertSame('NLTNM000722345', $contractId->toCompactStringWithoutCheckDigit());
    }

    // Test for partyId
    public function testPartyId(): void
    {
        $contractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000722345');
        $partyId = $contractId->partyId();
        $this->assertInstanceOf(PartyId::class, $partyId);
        $this->assertSame('NL-TNM', (string) $partyId);
    }

    // New tests for opt() and convertTo() methods
    public function testOptValidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-000122045-U';
        $contractId = ContractIdIso::opt($contractIdString);
        $this->assertNotNull($contractId);
        $this->assertSame('NL', (string) $contractId->countryCode);
        $this->assertSame('TNM', (string) $contractId->providerId);
        $this->assertSame('000122045', $contractId->instanceValue);
        $this->assertSame('U', $contractId->checkDigit);
    }

    public function testOptInvalidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-INVALID-X';
        $contractId = ContractIdIso::opt($contractIdString);
        $this->assertNull($contractId);
    }

    public function testConvertToDinFromIso(): void
    {
        $isoContractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000122045', 'U');
        $dinContractId = $isoContractId->convertToDin();
        $this->assertInstanceOf(ContractIdDin::class, $dinContractId);
        // Computed check digit for NL-TNM-012204-5 should be '5' (based on Scala tests)
        $this->assertSame('NL-TNM-012204-5', (string) $dinContractId);
    }

    public function testConvertToEmi3FromIso(): void
    {
        // ISO cannot directly convert to EMI3, it needs to go via DIN
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Conversion from Juherr\\\\MobilityId\\\\ContractIdIso to ContractIdEmi3 is not supported./');
        $isoContractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000122045', 'U');
        $isoContractId->convertToEmi3();
    }

    public function testConvertToIsoFromDin(): void
    {
        $dinContractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '012204', '5');
        $isoContractId = $dinContractId->convertToIso();
        $this->assertInstanceOf(ContractIdIso::class, $isoContractId);
        $this->assertSame('NL-TNM-000122045-U', (string) $isoContractId);
    }

    public function testConversionToEmi3NotSupportedFromIso(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/Conversion from Juherr\\\\MobilityId\\\\ContractIdIso to ContractIdEmi3 is not supported./');
        $isoContractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000122045', 'U');
        $isoContractId->convertToEmi3();
    }

    public function testEqualityBetweenStringAndComponentForm(): void
    {
        // Test that ContractId created from string equals ContractId created from parts
        // This mirrors Scala test: "NL-TNM-000122045" equals ContractId("NL", "TNM", "000122045", 'U')
        $id1 = ContractIdIso::of('NL-TNM-000122045-U');
        $id2 = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000122045', 'U');
        $this->assertEquals($id1, $id2);
        $this->assertSame((string) $id1, (string) $id2);
    }
}
