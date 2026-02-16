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

class CheckDigitDinTest {
    @Test
    void matchesKnownExamples() {
        assertThat(calculate(71)).isEqualTo('9');
        assertThat(calculate(110)).isEqualTo('X');
        assertThat(calculate(124)).isEqualTo('0');
        assertThat(calculate(114)).isEqualTo('6');
        assertThat(calculate(191)).isEqualTo('5');
    }

    private static char calculate(int instance) {
        return CheckDigitDin.compute("INTNM" + String.format("%06d", instance));
    }
}
