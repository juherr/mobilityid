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
use Juherr\MobilityId\OperatorIdDin;
use Juherr\MobilityId\OperatorIdIso;
use Juherr\MobilityId\PartyId;
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

final class PartyIdTest extends TestCase
{
    #[DataProvider('provideParseValidPartyIdStringsCases')]
    public function testParseValidPartyIdStrings(string $partyIdString, string $expectedCompactString, string $expectedToString): void
    {
        $partyId = PartyId::parse($partyIdString);
        self::assertInstanceOf(PartyId::class, $partyId);
        self::assertSame($expectedCompactString, $partyId->toCompactString());
        self::assertSame($expectedToString, (string) $partyId);
    }

    public static function provideParseValidPartyIdStringsCases(): iterable
    {
        return [
            ['NL-TNM', 'NLTNM', 'NL-TNM'],
            ['NL*TNM', 'NLTNM', 'NL-TNM'],
            ['NLTNM', 'NLTNM', 'NL-TNM'],
            ['DE-AW8', 'DEAW8', 'DE-AW8'],
            ['US-ABC', 'USABC', 'US-ABC'],
            ['FR*123', 'FR123', 'FR-123'],
            ['nl-tnm', 'NLTNM', 'NL-TNM'],
        ];
    }

    #[DataProvider('provideParseInvalidPartyIdStringsCases')]
    public function testParseInvalidPartyIdStrings(string $partyIdString): void
    {
        $partyId = PartyId::parse($partyIdString);
        self::assertNull($partyId);
    }

    public static function provideParseInvalidPartyIdStringsCases(): iterable
    {
        return [
            ['NLTNMA'],      // Too long party code
            ['XYTNM'],       // Invalid country code (not ISO 3166-1 alpha-2)
            ['NL%(@$'],      // Invalid character in party code
            [' NLTNM'],      // Leading space
            [''],            // Empty
            ['XY-TNMaargh'], // Too long party code
            ['НЛ-TNM'],      // Non-ASCII country code
            ['NLT-NM'],      // Invalid separator
            ['NL-TN'],       // Too short party code
            ['NL-1234'],     // Too long party code
            ["\nLTNM"],      // Newline character
            ["\tNLTNM"],     // Tab character
        ];
    }

    public function testOfWithProviderId(): void
    {
        $countryCode = CountryCode::of('NL');
        $providerId = ProviderId::of('TNM');
        $partyId = PartyId::of($countryCode, $providerId);
        self::assertSame('NLTNM', $partyId->toCompactString());
        self::assertSame('NL-TNM', (string) $partyId);
        self::assertSame('NL', $partyId->countryCode->cc);
        self::assertSame('TNM', $partyId->partyCode);
    }

    public function testOfWithOperatorIdIso(): void
    {
        $countryCode = CountryCode::of('DE');
        $operatorIdIso = OperatorIdIso::of('AW8');
        $partyId = PartyId::of($countryCode, $operatorIdIso);
        self::assertSame('DEAW8', $partyId->toCompactString());
        self::assertSame('DE-AW8', (string) $partyId);
        self::assertSame('DE', $partyId->countryCode->cc);
        self::assertSame('AW8', $partyId->partyCode);
    }

    public function testOfWithInvalidPartyCodeFromIdentifier(): void
    {
        // A DIN operator id may carry up to six digits, which is not a three-character party code.
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Invalid party code derived from identifier. (Was: 1234)');
        PartyId::of(CountryCode::of('DE'), OperatorIdDin::of('1234'));
    }
}
