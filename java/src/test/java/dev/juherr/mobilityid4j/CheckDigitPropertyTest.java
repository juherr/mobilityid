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

import java.util.Locale;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/** Algorithm-level invariants that hold for every well-formed payload, not only the known vectors. */
class CheckDigitPropertyTest {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Provide
    Arbitrary<String> isoPayloads() {
        return Arbitraries.strings().withChars(ALPHABET).ofLength(14);
    }

    @Provide
    Arbitrary<String> dinPayloads() {
        return Arbitraries.strings().withChars(ALPHABET).ofLength(11);
    }

    @Property
    void isoCheckDigitIsInTheAlphabet(@ForAll("isoPayloads") String payload) {
        assertThat(ALPHABET).contains(String.valueOf(CheckDigitIso.compute(payload)));
    }

    @Property
    void isoCheckDigitIgnoresCase(@ForAll("isoPayloads") String payload) {
        assertThat(CheckDigitIso.compute(payload.toLowerCase(Locale.ROOT))).isEqualTo(CheckDigitIso.compute(payload));
    }

    @Property
    void isoCheckDigitDetectsAnySingleSubstitution(
            @ForAll("isoPayloads") String payload,
            @ForAll("index14") int index,
            @ForAll("alphabetChar") char replacement) {
        var original = payload.charAt(index);
        if (original == replacement) {
            return;
        }
        var mutated = payload.substring(0, index) + replacement + payload.substring(index + 1);
        assertThat(CheckDigitIso.compute(mutated)).isNotEqualTo(CheckDigitIso.compute(payload));
    }

    @Property
    void dinCheckDigitIsADigitOrX(@ForAll("dinPayloads") String payload) {
        // Modulo-11 scheme: the remainder 10 is rendered as 'X', as in ISBN-10.
        assertThat("0123456789X").contains(String.valueOf(CheckDigitDin.compute(payload)));
    }

    @Property
    void dinCheckDigitIgnoresCase(@ForAll("dinPayloads") String payload) {
        assertThat(CheckDigitDin.compute(payload.toLowerCase(Locale.ROOT))).isEqualTo(CheckDigitDin.compute(payload));
    }

    @Provide
    Arbitrary<Integer> index14() {
        return Arbitraries.integers().between(0, 13);
    }

    @Provide
    Arbitrary<Character> alphabetChar() {
        return Arbitraries.chars().with(ALPHABET);
    }
}
