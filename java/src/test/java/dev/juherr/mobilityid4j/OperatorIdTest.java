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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OperatorIdTest {
    @Test
    void operatorIdDinRejectsTooShortIds() {
        assertThatThrownBy(() -> OperatorIdDin.of("12")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operatorIdDinRejectsLetters() {
        assertThatThrownBy(() -> OperatorIdDin.of("12A")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operatorIdDinRejectsTooLongIds() {
        assertThatThrownBy(() -> OperatorIdDin.of("1234567")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operatorIdDinAcceptsCorrectFormat() {
        assertThatCode(() -> OperatorIdDin.of("12345")).doesNotThrowAnyException();
    }

    @Test
    void operatorIdIsoRejectsTooShortIds() {
        assertThatThrownBy(() -> OperatorIdIso.of("AB")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operatorIdIsoRejectsTooLongIds() {
        assertThatThrownBy(() -> OperatorIdIso.of("ABCD")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operatorIdIsoAcceptsCorrectFormat() {
        assertThatCode(() -> OperatorIdIso.of("AB2")).doesNotThrowAnyException();
    }
}
