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

use Juherr\MobilityId\CheckDigitDin;
use Juherr\MobilityId\CheckDigitIso;
use Juherr\MobilityId\ContractIdIso;
use Juherr\MobilityId\CountryCode;
use Juherr\MobilityId\EvseId;
use Juherr\MobilityId\EvseIdIso;
use Juherr\MobilityId\OperatorIdIso;
use Juherr\MobilityId\ProviderId;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

/**
 * Some case mappings turn non-ASCII letters into ASCII ones ('ſ' U+017F to 'S', 'ı' U+0131 to 'I').
 * Only ASCII letters are folded, so these inputs are rejected; the vectors are shared with every port.
 */
final class NonAsciiLetterTest extends TestCase
{
    /**
     * @param \Closure(): mixed $factory
     */
    #[DataProvider('provideRejectsNonAsciiLettersThatUppercaseToAsciiCases')]
    public function testRejectsNonAsciiLettersThatUppercaseToAscii(\Closure $factory): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $factory();
    }

    /**
     * @return iterable<string, array{\Closure(): mixed}>
     */
    public static function provideRejectsNonAsciiLettersThatUppercaseToAsciiCases(): iterable
    {
        yield 'ISO EVSE id power outlet' => [static fn (): mixed => EvseIdIso::ofParts(CountryCode::of('FR'), OperatorIdIso::of('55C'), '84400*ſVG*1')];

        yield 'country code' => [static fn (): mixed => CountryCode::of('ſe')];

        yield 'ISO operator id' => [static fn (): mixed => OperatorIdIso::of('ıBM')];

        yield 'EVSE id power outlet' => [static fn (): mixed => EvseId::ofParts('FR', '55C', '84400*ſVG*1')];

        yield 'EVSE id country code' => [static fn (): mixed => EvseId::ofParts('ſe', '55C', '1')];

        yield 'contract id instance' => [static fn (): mixed => ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '00012204ı')];

        yield 'contract id check digit' => [static fn (): mixed => ContractIdIso::ofParts(CountryCode::of('NL'), ProviderId::of('TNM'), '000000899', 'ı')];

        yield 'ISO check digit' => [static fn (): mixed => CheckDigitIso::calculate('NLſVG000122045')];

        yield 'DIN check digit' => [static fn (): mixed => CheckDigitDin::calculate('NLſVG012204')];
    }
}
