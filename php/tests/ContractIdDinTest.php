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
use Juherr\MobilityId\ContractIdEmi3;
use Juherr\MobilityId\ContractIdIso;
use Juherr\MobilityId\CountryCode;
use Juherr\MobilityId\PartyId;
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class ContractIdDinTest extends TestCase
{
    // Test cases for construction from string
    #[DataProvider('provideValidDinContractIdStrings')]
    public function testOfValidDinContractIdStrings(string $contractIdString, string $expectedCountryCode, string $expectedProviderId, string $expectedInstanceValue, string $expectedCheckDigit): void
    {
        $contractId = ContractIdDin::of($contractIdString);
        $this->assertSame($expectedCountryCode, (string) $contractId->countryCode);
        $this->assertSame($expectedProviderId, (string) $contractId->providerId);
        $this->assertSame($expectedInstanceValue, $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidDinContractIdStrings(): array
    {
        return [
            ['NL-TNM-122045-0', 'NL', 'TNM', '122045', '0'],
            ['NL-TNM-122045-0', 'NL', 'TNM', '122045', '0'], // Duplicate
            ['Nl-TnM-122045-0', 'NL', 'TNM', '122045', '0'], // Case insensitive
            ['nl-TNm-122045-0', 'NL', 'TNM', '122045', '0'], // Case insensitive
            ['NL*TNM*122045*0', 'NL', 'TNM', '122045', '0'], // Asterisk separator
            ['NLTNM122045', 'NL', 'TNM', '122045', '0'],     // No separators, no check digit provided
            ['NL-TNM-A12204-6', 'NL', 'TNM', 'A12204', '6'], // Alphanumeric instance value
        ];
    }

    // Test cases for construction from parts
    #[DataProvider('provideValidDinContractIdParts')]
    public function testOfPartsValidDinContractId(
        string $countryCode,
        string $providerId,
        string $instanceValue,
        ?string $checkDigit,
        string $expectedCheckDigit
    ): void {
        $cc = CountryCode::of($countryCode);
        $pi = ProviderId::of($providerId);
        $contractId = ContractIdDin::ofParts($cc, $pi, $instanceValue, $checkDigit);

        $this->assertSame(strtoupper($countryCode), (string) $contractId->countryCode);
        $this->assertSame(strtoupper($providerId), (string) $contractId->providerId);
        $this->assertSame(strtoupper($instanceValue), $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidDinContractIdParts(): array
    {
        return [
            ['NL', 'TNM', '122045', '0', '0'],
            ['nl', 'tnm', '122045', '0', '0'], // Case insensitive
            ['NL', 'TNM', '122045', null, '0'], // No check digit given, should compute
            ['NL', 'TNM', 'A12204', '6', '6'],
        ];
    }

    // Test cases for invalid input
    #[DataProvider('provideInvalidDinContractIdStrings')]
    public function testOfInvalidDinContractIdStrings(string $contractIdString, string $expectedMessageRegex): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches($expectedMessageRegex);
        ContractIdDin::of($contractIdString);
    }

    public static function provideInvalidDinContractIdStrings(): array
    {
        return [
            ['NL-TNM-122045-X', '/Given check digit \'.*\' is not equal to computed \'.*\'/'], /* Wrong check digit */
            ['NLTNM076', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"],        /* Wrong length for instance value */
            ['X-aargh-131331234', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Wrong length of fields */
            [' \u0000t24\u2396a	', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Nonsense */
            ['NL-T|M-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Illegal char in provider ID */
            ['A-TNM-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Invalid country code (too short) */
            ['NLD-TNM-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Invalid country code (too long) */
            ['NL-TNMNN-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Invalid provider ID (too long) */
            ['NL-TNM-AB-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], /* Instance value too short */
        ];
    }

    // Test for rendering
    public function testRendering(): void
    {
        $contractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '122045');
        $this->assertSame('NL-TNM-122045-0', (string) $contractId);
        $this->assertSame('NLTNM1220450', $contractId->toCompactString());
        $this->assertSame('NLTNM122045', $contractId->toCompactStringWithoutCheckDigit());
    }

    // Test for partyId
    public function testPartyId(): void
    {
        $contractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '122045');
        $partyId = $contractId->partyId();
        $this->assertInstanceOf(PartyId::class, $partyId);
        $this->assertSame('NL-TNM', (string) $partyId);
    }

    // New tests for opt() and convertTo() methods
    public function testOptValidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-122045-0';
        $contractId = ContractIdDin::opt($contractIdString);
        $this->assertNotNull($contractId);
        $this->assertSame('NL', (string) $contractId->countryCode);
        $this->assertSame('TNM', (string) $contractId->providerId);
        $this->assertSame('122045', $contractId->instanceValue);
        $this->assertSame('0', $contractId->checkDigit);
    }

    public function testOptInvalidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-INVALID-X';
        $contractId = ContractIdDin::opt($contractIdString);
        $this->assertNull($contractId);
    }

    public function testConvertToEmi3FromDin(): void
    {
        $dinContractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000110', '7');
        $emi3ContractId = $dinContractId->convertToEmi3();
        $this->assertInstanceOf(ContractIdEmi3::class, $emi3ContractId);
        // Computed check digit for NL-TNM-C00001107 should be 'L' (Actual from CheckDigitIso::calculate)
        $this->assertSame('NL-TNM-C00001107-L', (string) $emi3ContractId);
    }

    public function testConvertToIsoFromDin(): void
    {
        $dinContractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000110', '7');
        $isoContractId = $dinContractId->convertToIso();
        $this->assertInstanceOf(ContractIdIso::class, $isoContractId);
        // Computed check digit for NL-TNM-000001107 should be 'Y' (Actual from CheckDigitIso::calculate)
        $this->assertSame('NL-TNM-000001107-Y', (string) $isoContractId);
    }

    public function testConversionToDinNotSupportedFromIso(): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/NL-TNM-012345678-W cannot be converted to DIN SPEC 91286 format/');

        $isoContractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '012345678', 'W');
        $isoContractId->convertToDin(); // Should throw exception
    }
}
