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

import dev.juherr.mobilityid4j.interpolators.MobilityIdParsers;
import org.junit.jupiter.api.Test;

class ParseNullHandlingTest {
    @Test
    void directParsersReturnEmptyOnNull() {
        assertThat(CountryCode.parse(null)).isNull();
        assertThat(PhoneCountryCode.parse(null)).isNull();
        assertThat(ProviderId.parse(null)).isNull();
        assertThat(OperatorIdIso.parse(null)).isNull();
        assertThat(OperatorIdDin.parse(null)).isNull();
    }

    @Test
    void convenienceParsersReturnEmptyOnNull() {
        assertThat(MobilityIdParsers.parseContractIdIso(null)).isNull();
        assertThat(MobilityIdParsers.parseContractIdDin(null)).isNull();
        assertThat(MobilityIdParsers.parseContractIdEmi3(null)).isNull();
        assertThat(MobilityIdParsers.parseEvseId(null)).isNull();
        assertThat(MobilityIdParsers.parseEvseIdIso(null)).isNull();
        assertThat(MobilityIdParsers.parseEvseIdDin(null)).isNull();
        assertThat(MobilityIdParsers.parseProviderId(null)).isNull();
        assertThat(MobilityIdParsers.parseCountryCode(null)).isNull();
        assertThat(MobilityIdParsers.parsePhoneCountryCode(null)).isNull();
        assertThat(MobilityIdParsers.parseOperatorIdIso(null)).isNull();
        assertThat(MobilityIdParsers.parseOperatorIdDin(null)).isNull();
    }
}
