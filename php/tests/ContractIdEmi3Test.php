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

class ContractIdEmi3Test extends TestCase
{
    // Test cases for construction from string
    #[DataProvider('provideValidEmi3ContractIdStrings')]
    public function testOfValidEmi3ContractIdStrings(string $contractIdString, string $expectedCountryCode, string $expectedProviderId, string $expectedInstanceValue, string $expectedCheckDigit): void
    {
        $contractId = ContractIdEmi3::of($contractIdString);
        $this->assertSame($expectedCountryCode, (string) $contractId->countryCode);
        $this->assertSame($expectedProviderId, (string) $contractId->providerId);
        $this->assertSame($expectedInstanceValue, $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidEmi3ContractIdStrings(): array
    {
        return [
            ['NL-TNM-C00122045-K', 'NL', 'TNM', 'C00122045', 'K'],
            ['NL-TNM-C00122045-K', 'NL', 'TNM', 'C00122045', 'K'], // Duplicate
            ['Nl-TnM-c00122045-K', 'NL', 'TNM', 'C00122045', 'K'], // Case insensitive
            ['nl-TNm-C00122045-k', 'NL', 'TNM', 'C00122045', 'K'], // Case insensitive
            ['NLTNMC00122045', 'NL', 'TNM', 'C00122045', 'K'],     // No dashes, no check digit provided
            ['NL-TNM-C10122045-J', 'NL', 'TNM', 'C10122045', 'J'], // Valid EMI3 not convertible to DIN
        ];
    }

    // Test cases for construction from parts
    #[DataProvider('provideValidEmi3ContractIdParts')]
    public function testOfPartsValidEmi3ContractId(
        string $countryCode,
        string $providerId,
        string $instanceValue,
        ?string $checkDigit,
        string $expectedCheckDigit
    ): void {
        $cc = CountryCode::of($countryCode);
        $pi = ProviderId::of($providerId);
        $contractId = ContractIdEmi3::ofParts($cc, $pi, $instanceValue, $checkDigit);

        $this->assertSame(strtoupper($countryCode), (string) $contractId->countryCode);
        $this->assertSame(strtoupper($providerId), (string) $contractId->providerId);
        $this->assertSame(strtoupper($instanceValue), $contractId->instanceValue);
        $this->assertSame($expectedCheckDigit, $contractId->checkDigit);
    }

    public static function provideValidEmi3ContractIdParts(): array
    {
        return [
            ['NL', 'TNM', 'C00122045', 'K', 'K'],
            ['nl', 'tnm', 'c00122045', 'k', 'K'], // Case insensitive
            ['NL', 'TNM', 'C00122045', null, 'K'], // No check digit given, should compute
            ['NL', 'TNM', 'C10122045', null, 'J'],
        ];
    }

    // Test cases for invalid input
    #[DataProvider('provideInvalidEmi3ContractIdStrings')]
    public function testOfInvalidEmi3ContractIdStrings(string $contractIdString, string $expectedMessageRegex): void
    {
        $this->expectException(InvalidArgumentException::class);
        $this->expectExceptionMessageMatches($expectedMessageRegex);
        ContractIdEmi3::of($contractIdString);
    }

    public static function provideInvalidEmi3ContractIdStrings(): array
    {
        return [
            ['NL-TNM-C00122045-X', "/Given check digit '.*' is not equal to computed '.*'/"], /* Wrong check digit */
            ['NLTNM076', "/'.*?' is not a valid Contract Id for EMI3/"],                        /* Wrong length for instance value */
            ['X-aargh-131331234', "/'.*?' is not a valid Contract Id for EMI3/"],               /* Wrong length of fields */
            [' \u0000t24\u2396a	', "/'.*?' is not a valid Contract Id for EMI3/"],               /* Nonsense */
            ['NL-T|M-C00122045-K', "/'.*?' is not a valid Contract Id for EMI3/"],               /* Illegal char in provider ID */
            ['A-TNM-C00122045-K', "/'.*?' is not a valid Contract Id for EMI3/"],                     /* Invalid country code (too short) */
            ['NLD-TNM-C00122045-K', "/'.*?' is not a valid Contract Id for EMI3/"],                   /* Invalid country code (too long) */
            ['NL-TNMNN-C00122045-K', "/'.*?' is not a valid Contract Id for EMI3/"],                 /* Invalid provider ID (too long) */
            ['NLTNM012345678', "/'.*?' is not a valid Contract Id for EMI3/"],                  /* Instance value without 'C0' prefix */
            ['NLTNMZ12345678', "/'.*?' is not a valid Contract Id for EMI3/"],
        ];
    }

    // Test for rendering
    public function testRendering(): void
    {
        $contractId = ContractIdEmi3::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), 'C00122045');
        $this->assertSame('NL-TNM-C00122045-K', (string) $contractId);
        $this->assertSame('NLTNMC00122045K', $contractId->toCompactString());
        $this->assertSame('NLTNMC00122045', $contractId->toCompactStringWithoutCheckDigit());
    }

    // Test for partyId
    public function testPartyId(): void
    {
        $contractId = ContractIdEmi3::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), 'C00122045');
        $partyId = $contractId->partyId();
        $this->assertInstanceOf(PartyId::class, $partyId);
        $this->assertSame('NL-TNM', (string) $partyId);
    }

    // New tests for opt() and convertTo() methods
    public function testOptValidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-C00122045-K';
        $contractId = ContractIdEmi3::opt($contractIdString);
        $this->assertNotNull($contractId);
        $this->assertSame('NL', (string) $contractId->countryCode);
        $this->assertSame('TNM', (string) $contractId->providerId);
        $this->assertSame('C00122045', $contractId->instanceValue);
        $this->assertSame('K', $contractId->checkDigit);
    }

    public function testOptInvalidContractIdString(): void
    {
        $contractIdString = 'NL-TNM-INVALID-X';
        $contractId = ContractIdEmi3::opt($contractIdString);
        $this->assertNull($contractId);
    }

    public function testConvertToDinFromEmi3(): void
    {
        $emi3ContractId = ContractIdEmi3::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), 'C00122045', 'K');
        $dinContractId = $emi3ContractId->convertToDin();
        $this->assertInstanceOf(ContractIdDin::class, $dinContractId);
        // Computed check digit for NL-TNM-012204-5 should be '5' (based on Scala tests)
        $this->assertSame('NL-TNM-012204-5', (string) $dinContractId);
    }

    public function testConvertToIsoFromEmi3(): void
    {
        $emi3ContractId = ContractIdEmi3::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), 'C00122045', 'K');
        $isoContractId = $emi3ContractId->convertToIso();
        $this->assertInstanceOf(ContractIdIso::class, $isoContractId);
        $this->assertSame('NL-TNM-C00122045-K', (string) $isoContractId);
    }
}
