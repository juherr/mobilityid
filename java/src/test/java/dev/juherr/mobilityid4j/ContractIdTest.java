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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class ContractIdTest {
    @Test
    void rejectsWrongLengthsAndInvalidCharacters() {
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "A", "TNM", "000122045"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "NL", "TNMN", "000722345"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "72245"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "NL", "T|M", "000122045"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.DIN, "NL", "TNM", "000122045"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isCaseInsensitiveWhenConstructedFromParts() {
        assertThat(ContractId.of(ContractIdStandard.ISO, "Nl", "tnM", "000122045"))
                .isEqualTo(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045"));
    }

    @Test
    void parsesAndNormalizesIsoContractIds() {
        List<ContractId> ids = List.of(
                ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U"),
                ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U"),
                ContractId.parseStrict(ContractIdStandard.ISO, "Nl-TnM-000122045-U"),
                ContractId.parseStrict(ContractIdStandard.ISO, "nl-TNm-000122045-u"),
                ContractId.parseStrict(ContractIdStandard.ISO, "NLTNM000122045"));

        assertThat(ids).containsOnly(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045"));
    }

    @Test
    void parsesAndNormalizesEmi3ContractIds() {
        List<ContractId> ids = List.of(
                ContractId.parseStrict(ContractIdStandard.EMI3, "NL-TNM-C00122045-K"),
                ContractId.parseStrict(ContractIdStandard.EMI3, "Nl-TnM-C00122045-K"),
                ContractId.parseStrict(ContractIdStandard.EMI3, "nl-TNm-C00122045-k"),
                ContractId.parseStrict(ContractIdStandard.EMI3, "NLTNMC00122045"));

        assertThat(ids).containsOnly(ContractId.of(ContractIdStandard.EMI3, "NL", "TNM", "C00122045"));
    }

    @Test
    void parsesAndNormalizesDinContractIds() {
        List<ContractId> ids = List.of(
                ContractId.parseStrict(ContractIdStandard.DIN, "NL-TNM-122045-0"),
                ContractId.parseStrict(ContractIdStandard.DIN, "Nl-TnM-122045-0"),
                ContractId.parseStrict(ContractIdStandard.DIN, "nl-TNm-122045-0"),
                ContractId.parseStrict(ContractIdStandard.DIN, "NL*TNM*122045*0"),
                ContractId.parseStrict(ContractIdStandard.DIN, "NLTNM122045"));

        assertThat(ids).containsOnly(ContractId.of(ContractIdStandard.DIN, "NL", "TNM", "122045"));
    }

    @Test
    void rendersCompactAndCanonicalForms() {
        ContractId id = ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000722345");

        assertThat(id.toString()).isEqualTo("NL-TNM-000722345-X");
        assertThat(id.toCompactString()).isEqualTo("NLTNM000722345X");
        assertThat(id.toCompactStringWithoutCheckDigit()).isEqualTo("NLTNM000722345");
    }

    @Test
    void convertsBetweenStandards() {
        ContractId din = ContractId.parseStrict(ContractIdStandard.DIN, "NL-TNM-012204-5");

        assertThat(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045")
                        .convertTo(ContractIdStandard.DIN)
                        .toString())
                .isEqualTo("NL-TNM-012204-5");
        assertThat(din.convertTo(ContractIdStandard.ISO).toString()).isEqualTo("NL-TNM-000122045-U");
        assertThat(din.convertTo(ContractIdStandard.EMI3).toString()).isEqualTo("NL-TNM-C00122045-K");
        assertThat(ContractId.parseStrict(ContractIdStandard.EMI3, "NL-TNM-C00122045-K")
                        .convertTo(ContractIdStandard.DIN)
                        .toString())
                .isEqualTo("NL-TNM-012204-5");
        assertThat(ContractId.parseStrict(ContractIdStandard.EMI3, "NL-TNM-C00122045-K")
                        .convertTo(ContractIdStandard.ISO)
                        .toString())
                .isEqualTo("NL-TNM-C00122045-K");
    }

    @Test
    void rejectsNonConvertibleIsoToDin() {
        assertThatThrownBy(() -> ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "012345678")
                        .convertTo(ContractIdStandard.DIN))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsInvalidInputs() {
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-X"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, "NLTNM076"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, "X-aargh-131331234"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, " \u0000t24\u2396a\t"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ContractId.parseStrict(ContractIdStandard.ISO, "NL-T|M-000122045-U"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void supportsEqualityAndAccessors() {
        assertThat(ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045"))
                .isEqualTo(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045", 'U'));
        assertThat(ContractId.parseStrict(ContractIdStandard.ISO, "NLTNM012345678"))
                .isNotEqualTo(ContractId.parseStrict(ContractIdStandard.ISO, "NLTNM876543210"));

        ContractId id = ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000122045");
        assertThat(id.countryCode()).isEqualTo(CountryCode.of("NL"));
        assertThat(id.providerId()).isEqualTo(ProviderId.of("TNM"));
        assertThat(id.instanceValue()).isEqualTo("000122045");
        assertThat(id.checkDigit()).isEqualTo('U');
    }

    @Test
    void exposesPartyId() {
        assertThat(ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000722345")
                        .partyId())
                .isEqualTo(PartyId.parse("NLTNM").orElseThrow());
    }

    @Test
    void parseReturnsEmptyForNullInput() {
        assertThat(ContractId.parse(ContractIdStandard.ISO, null)).isEmpty();
        assertThat(ContractId.parse(null, "NL-TNM-000722345-X")).isEmpty();
    }
}
