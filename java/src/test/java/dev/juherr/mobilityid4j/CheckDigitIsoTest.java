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

class CheckDigitIsoTest {
    @Test
    void computesKnownCheckDigits() {
        List<String> contractIds = List.of(
                "NN123ABCDEFGHI",
                "FRXYZ123456789",
                "ITA1B2C3E4F5G6",
                "ESZU8WOX834H1D",
                "PT73902837ABCZ",
                "DE83DUIEN83QGZ",
                "DE83DUIEN83ZGQ",
                "DE8AA001234567");

        String computed = contractIds.stream()
                .map(CheckDigitIso::compute)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        assertThat(computed).isEqualTo("T24RZDM0");
    }

    @Test
    void rejectsMalformedInput() {
        assertThatThrownBy(() ->
                        CheckDigitIso.compute("\u0415\u0432\u0440\u043e\u043f\u0430\u0440\u0443\u043b\u0438\u0442123"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("\u00C5\u220F@*(Td\uD83D\uDE3BgaR^&(%"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("\u00C5\u220F@*(Td\uD83D\uDE3BgR^&(%"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("DE8AA0012345678")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CheckDigitIso.compute("DE٨٣DUIEN٨٣QGZ")).isInstanceOf(IllegalArgumentException.class);
    }
}
