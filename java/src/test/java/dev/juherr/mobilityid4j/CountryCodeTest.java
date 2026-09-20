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

import java.util.Locale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CountryCodeTest {
    @ParameterizedTest
    @ValueSource(strings = {"NL", "nl", "GB", "AX", "BQ", "CW", "SS"})
    void acceptsAssignedIso3166Codes(String raw) {
        assertThat(CountryCode.of(raw).value()).isEqualTo(raw.toUpperCase(Locale.ROOT));
        assertThat(CountryCode.parse(raw)).isEqualTo(CountryCode.of(raw));
    }

    // Region codes that CLDR knows but ISO 3166-1 does not assign: every port rejects them.
    @ParameterizedTest
    @ValueSource(strings = {"UK", "EU", "XK", "AN", "SU", "DD", "YU", "XX", "ZZ"})
    void rejectsRegionCodesOutsideIso3166(String raw) {
        assertThat(CountryCode.parse(raw)).isNull();
        assertThatThrownBy(() -> CountryCode.of(raw))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISO 3166-1 alpha-2");
    }
}
