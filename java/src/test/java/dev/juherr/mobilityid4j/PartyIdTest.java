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

import org.junit.jupiter.api.Test;

class PartyIdTest {
    @Test
    void parsesAllAcceptedRepresentations() {
        assertThat(PartyId.parse("NL-TNM"))
                .hasValueSatisfying(id -> assertThat(id.toCompactString()).isEqualTo("NLTNM"));
        assertThat(PartyId.parse("NL*TNM"))
                .hasValueSatisfying(id -> assertThat(id.toCompactString()).isEqualTo("NLTNM"));
        assertThat(PartyId.parse("NLTNM"))
                .hasValueSatisfying(id -> assertThat(id.toCompactString()).isEqualTo("NLTNM"));
    }

    @Test
    void rendersWithDashSeparator() {
        assertThat(PartyId.parse("NL*TNM").orElseThrow().toString()).isEqualTo("NL-TNM");
    }

    @Test
    void rejectsNonsense() {
        assertThat(PartyId.parse("NLTNMA")).isEmpty();
        assertThat(PartyId.parse("XYTNM")).isEmpty();
        assertThat(PartyId.parse("NL%(@$")).isEmpty();
        assertThat(PartyId.parse(" NLTNM")).isEmpty();
        assertThat(PartyId.parse("\nLTNM")).isEmpty();
        assertThat(PartyId.parse("")).isEmpty();
        assertThat(PartyId.parse("XY-TNMaargh")).isEmpty();
        assertThat(PartyId.parse("\u041D\u041B-TNM")).isEmpty();
        assertThat(PartyId.parse("NLT-NM")).isEmpty();
    }

    @Test
    void parseReturnsEmptyForNullInput() {
        assertThat(PartyId.parse(null)).isEmpty();
    }
}
