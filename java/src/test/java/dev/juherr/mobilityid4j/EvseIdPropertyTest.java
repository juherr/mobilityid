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
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/** Case invariants for every syntactically valid ISO EVSE id. */
class EvseIdPropertyTest {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Provide
    Arbitrary<EvseIdIso> isoEvseIds() {
        return Combinators.combine(
                        Arbitraries.of(Locale.getISOCountries()),
                        Arbitraries.strings().withChars(ALPHANUMERIC).ofLength(3),
                        Arbitraries.strings()
                                .withChars(ALPHANUMERIC + "*")
                                .ofMinLength(1)
                                .ofMaxLength(31))
                .as(EvseIdIso::of);
    }

    @Property
    void lowercaseRenderingParsesToTheSameId(@ForAll("isoEvseIds") EvseIdIso evseId) {
        assertThat(EvseIdIso.parse(evseId.toString().toLowerCase(Locale.ROOT))).isEqualTo(evseId);
        assertThat(EvseId.parse(evseId.toString().toLowerCase(Locale.ROOT))).isEqualTo(evseId);
    }

    @Property
    void lowercaseCompactRenderingParsesLikeTheUppercaseOne(@ForAll("isoEvseIds") EvseIdIso evseId) {
        var compact = evseId.toCompactString();
        // An outlet made of separators only has no compact form.
        Assume.that(EvseIdIso.parse(compact) != null);
        assertThat(EvseIdIso.parse(compact.toLowerCase(Locale.ROOT)))
                .isNotNull()
                .isEqualTo(EvseIdIso.parse(compact));
    }
}
