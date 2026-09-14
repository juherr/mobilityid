/*
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
package dev.juherr.mobilityid4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/** Round-trip and conversion invariants for every syntactically valid contract id. */
class ContractIdPropertyTest {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Provide
    Arbitrary<String> countryCodes() {
        return Arbitraries.of(Locale.getISOCountries());
    }

    @Provide
    Arbitrary<String> providerIds() {
        return Arbitraries.strings().withChars(ALPHANUMERIC).ofLength(3);
    }

    @Provide
    Arbitrary<ContractId> contractIds() {
        Arbitrary<ContractIdStandard> standards = Arbitraries.of(ContractIdStandard.class);
        return Combinators.combine(
                        standards,
                        countryCodes(),
                        providerIds(),
                        Arbitraries.strings().withChars(ALPHANUMERIC).ofLength(9))
                .as((standard, country, provider, raw) -> {
                    var instance =
                            switch (standard) {
                                case ISO -> raw;
                                case EMI3 -> "C" + raw.substring(1);
                                case DIN -> raw.substring(3);
                            };
                    return ContractId.of(standard, country, provider, instance);
                });
    }

    @Property
    void normalizedRenderingRoundTrips(@ForAll("contractIds") ContractId id) {
        assertThat(ContractId.parseStrict(id.standard(), id.toString())).isEqualTo(id);
    }

    @Property
    void compactRenderingRoundTrips(@ForAll("contractIds") ContractId id) {
        assertThat(ContractId.parseStrict(id.standard(), id.toCompactString())).isEqualTo(id);
        assertThat(ContractId.parseStrict(id.standard(), id.toCompactStringWithoutCheckDigit()))
                .isEqualTo(id);
    }

    @Property
    void parsingIsCaseInsensitive(@ForAll("contractIds") ContractId id) {
        assertThat(ContractId.parseStrict(id.standard(), id.toString().toLowerCase(Locale.ROOT)))
                .isEqualTo(id);
    }

    @Property
    void forgivingParseAgreesWithStrictParse(@ForAll("contractIds") ContractId id) {
        assertThat(ContractId.parse(id.standard(), id.toString())).contains(id);
    }

    @Property
    void dinConvertsToEmi3AndBack(@ForAll("contractIds") ContractId id) {
        if (id.standard() != ContractIdStandard.DIN) {
            return;
        }
        var emi3 = id.convertTo(ContractIdStandard.EMI3);
        assertThat(emi3.standard()).isEqualTo(ContractIdStandard.EMI3);
        assertThat(emi3.convertTo(ContractIdStandard.DIN)).isEqualTo(id);
    }

    @Property
    void dinConvertsToIsoAndBack(@ForAll("contractIds") ContractId id) {
        if (id.standard() != ContractIdStandard.DIN) {
            return;
        }
        var iso = id.convertTo(ContractIdStandard.ISO);
        assertThat(iso.standard()).isEqualTo(ContractIdStandard.ISO);
        assertThat(iso.convertTo(ContractIdStandard.DIN)).isEqualTo(id);
    }

    @Property
    void emi3ConvertsToIsoKeepingPayload(@ForAll("contractIds") ContractId id) {
        if (id.standard() != ContractIdStandard.EMI3) {
            return;
        }
        var iso = id.convertTo(ContractIdStandard.ISO);
        assertThat(iso.toCompactString()).isEqualTo(id.toCompactString());
    }
}
