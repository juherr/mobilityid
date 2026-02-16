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

import org.junit.jupiter.api.Test;

class MobilityIdParsersTest {
    @Test
    void parsesMainTypes() {
        assertThat(MobilityIdParsers.parseContractIdIso("NL-TNM-000722345-X")).isPresent();
        assertThat(MobilityIdParsers.parseContractIdDin("NL-TNM-722345-8")).isPresent();
        assertThat(MobilityIdParsers.parseContractIdEmi3("NL-TNM-C00722345-N")).isPresent();
        assertThat(MobilityIdParsers.parseEvseId("NL*TNM*E840*6487")).isPresent();
        assertThat(MobilityIdParsers.parseEvseIdIso("NL*TNM*E840*6487")).isPresent();
        assertThat(MobilityIdParsers.parseEvseIdDin("+49*810*000*438")).isPresent();
        assertThat(MobilityIdParsers.parseProviderId("ABC")).isPresent();
        assertThat(MobilityIdParsers.parseCountryCode("NL")).isPresent();
        assertThat(MobilityIdParsers.parsePhoneCountryCode("+31")).isPresent();
        assertThat(MobilityIdParsers.parseOperatorIdIso("TNM")).isPresent();
        assertThat(MobilityIdParsers.parseOperatorIdDin("456")).isPresent();
    }
}
