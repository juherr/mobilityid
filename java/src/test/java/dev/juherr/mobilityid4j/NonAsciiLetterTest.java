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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@code toUpperCase} maps some non-ASCII letters to ASCII ones ('ſ' U+017F to 'S', 'ı' U+0131 to
 * 'I'). Only ASCII letters are folded, so these inputs are rejected like in every other port; the
 * vectors are shared with them.
 */
class NonAsciiLetterTest {
    static Stream<ThrowingCallable> nonAsciiInputs() {
        return Stream.of(
                () -> EvseIdIso.of("FR", "55C", "84400*ſVG*1"),
                () -> EvseIdIso.of("ſe", "55C", "1"),
                () -> EvseIdIso.of("NL", "ıBM", "1"),
                () -> new EvseIdIso(CountryCode.of("FR"), OperatorIdIso.of("55C"), "84400*ſVG*1"),
                () -> EvseId.of("FR", "55C", "84400*ſVG*1"),
                () -> EvseId.of("ſe", "55C", "1"),
                () -> ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "00012204ı"),
                () -> ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000000899", 'ı'),
                () -> CheckDigitIso.compute("NLſVG000122045"),
                () -> CheckDigitDin.compute("NLſVG012204"));
    }

    @ParameterizedTest
    @MethodSource("nonAsciiInputs")
    void rejectsNonAsciiLettersThatUppercaseToAscii(ThrowingCallable factory) {
        assertThatThrownBy(factory).isInstanceOf(IllegalArgumentException.class);
    }
}
