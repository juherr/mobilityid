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
package dev.juherr.mobilityid4j.interpolators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Every helper returns {@code null} for a non-null invalid value instead of throwing. */
class MobilityIdParsersInvalidInputTest {
    record Case(String name, Function<String, @Nullable Object> parser, String valid, String invalid) {}

    static Stream<Arguments> parsers() {
        return Stream.of(
                        new Case(
                                "parseContractIdIso",
                                MobilityIdParsers::parseContractIdIso,
                                "NL-TNM-000722345-X",
                                "NL-TNM-000722345-Y"),
                        new Case(
                                "parseContractIdDin",
                                MobilityIdParsers::parseContractIdDin,
                                "NL-TNM-722345-8",
                                "NL-TNM-722345-9"),
                        new Case(
                                "parseContractIdEmi3",
                                MobilityIdParsers::parseContractIdEmi3,
                                "NL-TNM-C00722345-N",
                                "NL-TNM-000722345-X"),
                        new Case("parseEvseId", MobilityIdParsers::parseEvseId, "NL*TNM*E840*6487", "NL*TNM*840*6487"),
                        new Case(
                                "parseEvseIdIso",
                                MobilityIdParsers::parseEvseIdIso,
                                "NL*TNM*E840*6487",
                                "+49*810*000*438"),
                        new Case(
                                "parseEvseIdDin",
                                MobilityIdParsers::parseEvseIdDin,
                                "+49*810*000*438",
                                "NL*TNM*E840*6487"),
                        new Case("parseProviderId", MobilityIdParsers::parseProviderId, "ABC", "ABCD"),
                        new Case("parseCountryCode", MobilityIdParsers::parseCountryCode, "NL", "XX"),
                        new Case("parsePhoneCountryCode", MobilityIdParsers::parsePhoneCountryCode, "+31", "+3A"),
                        new Case("parseOperatorIdIso", MobilityIdParsers::parseOperatorIdIso, "TNM", "TN*M"),
                        new Case("parseOperatorIdDin", MobilityIdParsers::parseOperatorIdDin, "456", "45A"))
                .map(c -> Arguments.of(Named.of(c.name(), c)));
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void returnsNullForInvalidValue(Case c) {
        assertThat(c.parser().apply(c.valid())).isNotNull();
        assertThat(c.parser().apply(c.invalid())).isNull();
    }
}
