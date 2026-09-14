/*
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Regression guard for the tolerant/strict contract: for the same invalid, non-null input every
 * tolerant parser returns {@code null} while its strict counterpart throws.
 */
class ParseInvalidInputTest {
    /** {@code strict} is null for types that only expose a tolerant string parser. */
    record Family(
            String name,
            @Nullable Function<String, Object> strict,
            Function<String, @Nullable Object> tolerant,
            String validSample,
            String invalidSample) {}

    static Stream<Arguments> families() {
        return Stream.of(
                        new Family("CountryCode", CountryCode::of, CountryCode::parse, "NL", "XX"),
                        new Family("CountryCode (shape)", CountryCode::of, CountryCode::parse, "de", "N1"),
                        new Family("PhoneCountryCode", PhoneCountryCode::of, PhoneCountryCode::parse, "+49", "+4A"),
                        new Family(
                                "PhoneCountryCode (length)",
                                PhoneCountryCode::of,
                                PhoneCountryCode::parse,
                                "31",
                                "+1234"),
                        new Family("ProviderId", ProviderId::of, ProviderId::parse, "TNM", "TN"),
                        new Family("ProviderId (charset)", ProviderId::of, ProviderId::parse, "abc", "T-M"),
                        new Family("OperatorIdIso", OperatorIdIso::of, OperatorIdIso::parse, "TNM", "T*M"),
                        new Family("OperatorIdDin", OperatorIdDin::of, OperatorIdDin::parse, "810", "AB7"),
                        new Family(
                                "OperatorIdDin (length)", OperatorIdDin::of, OperatorIdDin::parse, "810548", "8105481"),
                        new Family("PartyId", null, PartyId::parse, "NL-TNM", "NLTNMA"),
                        new Family(
                                "ContractId ISO",
                                raw -> ContractId.parseStrict(ContractIdStandard.ISO, raw),
                                raw -> ContractId.parse(ContractIdStandard.ISO, raw),
                                "NL-TNM-000122045-U",
                                "NL-TNM-000122045-X"),
                        new Family(
                                "ContractId EMI3",
                                raw -> ContractId.parseStrict(ContractIdStandard.EMI3, raw),
                                raw -> ContractId.parse(ContractIdStandard.EMI3, raw),
                                "NL-TNM-C00122045-K",
                                "NL-TNM-000122045-U"),
                        new Family(
                                "ContractId DIN",
                                raw -> ContractId.parseStrict(ContractIdStandard.DIN, raw),
                                raw -> ContractId.parse(ContractIdStandard.DIN, raw),
                                "NL-TNM-122045-0",
                                "NL-TNM-122045-1"),
                        new Family("EvseIdIso", null, EvseIdIso::parse, "NL*TNM*E840*6487", "+49*810*000*438"),
                        new Family("EvseId", null, EvseId::parse, "NL*TNM*E840*6487", "NL*TNM*840*6487"),
                        new Family("EvseIdDin", null, EvseIdDin::parse, "+49*810*000*438", "NL*TNM*E840*6487"))
                .map(f -> Arguments.of(Named.of(f.name(), f)));
    }

    @ParameterizedTest
    @MethodSource("families")
    void tolerantParserReturnsNullWhereStrictParserThrows(Family family) {
        assertThat(family.tolerant().apply(family.validSample())).isNotNull();
        assertThat(family.tolerant().apply(family.invalidSample())).isNull();
        var strict = family.strict();
        if (strict != null) {
            assertThatThrownBy(() -> strict.apply(family.invalidSample())).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
