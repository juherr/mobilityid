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

import dev.juherr.mobilityid4j.interpolators.MobilityIdParsers;
import org.junit.jupiter.api.Test;

class ParseNullHandlingTest {
    @Test
    void directParsersReturnEmptyOnNull() {
        assertThat(CountryCode.parse(null)).isEmpty();
        assertThat(PhoneCountryCode.parse(null)).isEmpty();
        assertThat(ProviderId.parse(null)).isEmpty();
        assertThat(OperatorIdIso.parse(null)).isEmpty();
        assertThat(OperatorIdDin.parse(null)).isEmpty();
    }

    @Test
    void convenienceParsersReturnEmptyOnNull() {
        assertThat(MobilityIdParsers.parseContractIdIso(null)).isEmpty();
        assertThat(MobilityIdParsers.parseContractIdDin(null)).isEmpty();
        assertThat(MobilityIdParsers.parseContractIdEmi3(null)).isEmpty();
        assertThat(MobilityIdParsers.parseEvseId(null)).isEmpty();
        assertThat(MobilityIdParsers.parseEvseIdIso(null)).isEmpty();
        assertThat(MobilityIdParsers.parseEvseIdDin(null)).isEmpty();
        assertThat(MobilityIdParsers.parseProviderId(null)).isEmpty();
        assertThat(MobilityIdParsers.parseCountryCode(null)).isEmpty();
        assertThat(MobilityIdParsers.parsePhoneCountryCode(null)).isEmpty();
        assertThat(MobilityIdParsers.parseOperatorIdIso(null)).isEmpty();
        assertThat(MobilityIdParsers.parseOperatorIdDin(null)).isEmpty();
    }
}
