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

import org.junit.jupiter.api.Test;

class EvseIdTest {
    @Test
    void parsesIsoWithAndWithoutSeparators() {
        assertThat(EvseId.parse("DE*AB7*E840*6487")).isPresent();
        assertThat(EvseId.parse("DEAB7E8406487")).isPresent();
    }

    @Test
    void parsesIsoBoundaryAndSpecialCases() {
        assertThat(EvseId.parse("DEAB7E1")).isPresent();
        assertThat(EvseId.parse("DE*AB7*E1234567890ABCDEFGHIJ1234567890")).isPresent();
        assertThat(EvseId.parse("DE*DES*E*BMW*0113*2")).isPresent();
        assertThat(EvseId.parse("DE*TNM*ETWL*HEDWIGLAUDIENRING*LS12001*0")).isPresent();
    }

    @Test
    void rejectsInvalidIsoStrings() {
        String tooLongOutletId = "77777777777777777777777777777777";
        assertThat(EvseId.parse("DE*AB7*E" + tooLongOutletId)).isEmpty();
        assertThat(EvseId.parse("NL*TNM*840*6487")).isEmpty();
        assertThat(EvseIdIso.parse("+49*810*000*438")).isEmpty();
        assertThat(EvseIdIso.parse("ZZ*TNM*840*64878")).isEmpty();
    }

    @Test
    void parsesDinFormat() {
        EvseIdDin evse = EvseIdDin.parse("+49*810*000*438").orElseThrow();
        assertThat(EvseId.parse("+49*810*000*438")).isPresent();
        assertThat(evse.countryCode()).isEqualTo(PhoneCountryCode.of("+49"));
        assertThat(evse.operatorId()).isEqualTo(OperatorIdDin.of("810"));
        assertThat(evse.powerOutletId()).isEqualTo("000*438");
    }

    @Test
    void parsesDinBoundaryAndValidationCases() {
        assertThat(EvseId.parse("+49*810*1")).isPresent();
        assertThat(EvseId.parse("+49*810*12345678901234567890123456789012")).isPresent();
        assertThat(EvseId.parse("+49*810*123456789012345678901234567890123")).isEmpty();
        assertThat(EvseId.parse("+49*AB7*840*6487")).isEmpty();
        assertThat(EvseId.parse("+49*645*E840*6487")).isEmpty();
        assertThat(EvseIdDin.parse("DE*AB7*E840*6487")).isEmpty();
        assertThat(EvseIdIso.parse("+4A*810*000*438")).isEmpty();
        assertThat(EvseId.parse("+49*810548*1234567890")).isPresent();

        assertThat(EvseId.parse("+49*810*000*438")).isEqualTo(EvseId.parse("49*810*000*438"));
    }

    @Test
    void buildsIsoAndDinFromFields() {
        EvseId iso = EvseId.of("NL", "TNM", "840*6487");
        EvseId din = EvseId.of("+31", "745", "840*6487");

        assertThat(iso).isInstanceOf(EvseIdIso.class);
        assertThat(iso.toString()).isEqualTo("NL*TNM*E840*6487");
        assertThat(din).isInstanceOf(EvseIdDin.class);
        assertThat(din.toString()).isEqualTo("+31*745*840*6487");
    }

    @Test
    void buildsSpecificTypesFromFields() {
        EvseIdIso iso = EvseIdIso.of("NL", "TNM", "E840*6487");
        EvseIdDin din = EvseIdDin.of("+31", "745", "840*6487");

        assertThat(iso.countryCode()).isEqualTo(CountryCode.of("NL"));
        assertThat(iso.operatorId()).isEqualTo(OperatorIdIso.of("TNM"));
        assertThat(iso.powerOutletId()).isEqualTo("E840*6487");
        assertThat(iso.toString()).isEqualTo("NL*TNM*EE840*6487");

        assertThat(din.countryCode()).isEqualTo(PhoneCountryCode.of("+31"));
        assertThat(din.operatorId()).isEqualTo(OperatorIdDin.of("745"));
        assertThat(din.powerOutletId()).isEqualTo("840*6487");
    }

    @Test
    void parseReturnsEmptyForNullInput() {
        assertThat(EvseId.parse(null)).isEmpty();
        assertThat(EvseIdIso.parse(null)).isEmpty();
        assertThat(EvseIdDin.parse(null)).isEmpty();
    }

    @Test
    void rejectsWrongTypeCombinationsAndMixedFormats() {
        assertThatThrownBy(() -> EvseIdDin.of("NL", "TNM", "840*6487")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseIdIso.of("+31", "745", "840*6487")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("+31", "ABC", "840*6487")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("+31", "745", "E840*6487")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("+31", "745", "840*6487E")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidFieldLengths() {
        assertThatThrownBy(() -> EvseId.of("A", "TNM", "000122045")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("NL", "TNMN", "000722345")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvseId.of("NL", "T|M", "000122045")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsCountryCodeWithAndWithoutPlusSignFromParts() {
        assertThat(EvseId.of("+31", "745", "840*6487")).isEqualTo(EvseId.of("31", "745", "840*6487"));
    }

    @Test
    void rendersCanonicalAndCompactForms() {
        EvseIdIso iso = (EvseIdIso) EvseId.of("NL", "TNM", "840*6487");
        assertThat(iso.toString()).isEqualTo("NL*TNM*E840*6487");
        assertThat(iso.toCompactString()).isEqualTo("NLTNME8406487");
        assertThat(EvseId.of("+31", "745", "840*6487").toString()).isEqualTo("+31*745*840*6487");
        assertThat(EvseId.parse("+31*745*840*6487").orElseThrow().toString()).isEqualTo("+31*745*840*6487");
    }

    @Test
    void isCaseInsensitive() {
        assertThat(EvseId.of("Nl", "tnM", "E000122045")).isEqualTo(EvseId.of("NL", "TNM", "E000122045"));
    }

    @Test
    void exposesOperatorPartyId() {
        assertThat(EvseIdIso.of("NL", "TNM", "000122045").partyId())
                .isEqualTo(PartyId.parse("NLTNM").orElseThrow());
    }
}
