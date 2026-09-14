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

use Juherr\MobilityId\ContractIdDin;
use Juherr\MobilityId\ContractIdIso;
use Juherr\MobilityId\CountryCode;
use Juherr\MobilityId\PartyId;
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

final class ContractIdDinTest extends TestCase
{
    // Test cases for construction from string
    #[DataProvider('provideOfValidDinContractIdStringsCases')]
    public function testOfValidDinContractIdStrings(string $contractIdString, string $expectedCountryCode, string $expectedProviderId, string $expectedInstanceValue, string $expectedCheckDigit): void
    {
        $contractId = ContractIdDin::of($contractIdString);
        self::assertSame($expectedCountryCode, (string) $contractId->countryCode);
        self::assertSame($expectedProviderId, (string) $contractId->providerId);
        self::assertSame($expectedInstanceValue, $contractId->instanceValue);
        self::assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideOfValidDinContractIdStringsCases(): iterable
    {
        return [
            ['NL-TNM-122045-0', 'NL', 'TNM', '122045', '0'],
            ['NL-TNM-122045-0', 'NL', 'TNM', '122045', '0'], // Duplicate
            ['Nl-TnM-122045-0', 'NL', 'TNM', '122045', '0'], // Case insensitive
            ['nl-TNm-122045-0', 'NL', 'TNM', '122045', '0'], // Case insensitive
            ['nl-tnm-12a045-0', 'NL', 'TNM', '12A045', '0'], // Lowercase letter inside the instance value
            ['NL*TNM*122045*0', 'NL', 'TNM', '122045', '0'], // Asterisk separator
            ['NLTNM122045', 'NL', 'TNM', '122045', '0'],     // No separators, no check digit provided
            ['NL-TNM-A12204-6', 'NL', 'TNM', 'A12204', '6'], // Alphanumeric instance value
        ];
    }

    // Test cases for construction from parts
    #[DataProvider('provideOfPartsValidDinContractIdCases')]
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

        self::assertSame(strtoupper($countryCode), (string) $contractId->countryCode);
        self::assertSame(strtoupper($providerId), (string) $contractId->providerId);
        self::assertSame(strtoupper($instanceValue), $contractId->instanceValue);
        self::assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideOfPartsValidDinContractIdCases(): iterable
    {
        return [
            ['NL', 'TNM', '122045', '0', '0'],
            ['nl', 'tnm', '122045', '0', '0'], // Case insensitive
            ['nl', 'tnm', '12a045', '0', '0'], // Lowercase letter inside the instance value
            ['in', 'tnm', '000110', 'x', 'X'], // Lowercase given check digit
            ['NL', 'TNM', '122045', null, '0'], // No check digit given, should compute
            ['NL', 'TNM', 'A12204', '6', '6'],
        ];
    }

    // Test cases for invalid input
    #[DataProvider('provideOfInvalidDinContractIdStringsCases')]
    public function testOfInvalidDinContractIdStrings(string $contractIdString, string $expectedMessageRegex): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches($expectedMessageRegex);
        ContractIdDin::of($contractIdString);
    }

    public static function provideOfInvalidDinContractIdStringsCases(): iterable
    {
        return [
            ['NL-TNM-122045-X', '/Given check digit \'.*\' is not equal to computed \'.*\'/'], // Wrong check digit
            ['NLTNM076', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"],        // Wrong length for instance value
            ['X-aargh-131331234', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Wrong length of fields
            [' \u0000t24\u2396a	', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Nonsense
            ['NL-T|M-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Illegal char in provider ID
            ['A-TNM-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Invalid country code (too short)
            ['NLD-TNM-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Invalid country code (too long)
            ['NL-TNMNN-122045-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Invalid provider ID (too long)
            ['NL-TNM-AB-0', "/'.*?' is not a valid Contract Id for DIN SPEC 91286/"], // Instance value too short
        ];
    }

    // Test for rendering
    public function testRendering(): void
    {
        $contractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '122045');
        self::assertSame('NL-TNM-122045-0', (string) $contractId);
        self::assertSame('NLTNM1220450', $contractId->toCompactString());
        self::assertSame('NLTNM122045', $contractId->toCompactStringWithoutCheckDigit());
    }

    // Test for partyId
    public function testPartyId(): void
    {
        $contractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '122045');
        $partyId = $contractId->partyId();
        self::assertSame('NL-TNM', (string) $partyId);
    }

    // New tests for opt() and convertTo() methods
    public function testOptValidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-122045-0';
        $contractId = ContractIdDin::opt($contractIdString);
        self::assertInstanceOf(ContractIdDin::class, $contractId);
        self::assertSame('NL', (string) $contractId->countryCode);
        self::assertSame('TNM', (string) $contractId->providerId);
        self::assertSame('122045', $contractId->instanceValue);
        self::assertSame('0', $contractId->checkDigit);
    }

    public function testOptInvalidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-INVALID-X';
        $contractId = ContractIdDin::opt($contractIdString);
        self::assertNull($contractId);
    }

    public function testConvertToEmi3FromDin(): void
    {
        $dinContractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000110', '7');
        $emi3ContractId = $dinContractId->convertToEmi3();
        // Computed check digit for NL-TNM-C00001107 should be 'L' (Actual from CheckDigitIso::calculate)
        self::assertSame('NL-TNM-C00001107-L', (string) $emi3ContractId);
    }

    public function testConvertToIsoFromDin(): void
    {
        $dinContractId = ContractIdDin::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000110', '7');
        $isoContractId = $dinContractId->convertToIso();
        // Computed check digit for NL-TNM-000001107 should be 'Y' (Actual from CheckDigitIso::calculate)
        self::assertSame('NL-TNM-000001107-Y', (string) $isoContractId);
    }

    public function testConversionToDinNotSupportedFromIso(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessageMatches('/NL-TNM-012345678-W cannot be converted to DIN SPEC 91286 format/');

        $isoContractId = ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '012345678', 'W');
        $isoContractId->convertToDin(); // Should throw exception
    }
}
