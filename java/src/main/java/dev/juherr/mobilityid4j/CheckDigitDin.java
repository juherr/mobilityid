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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DIN check digit calculator for contract identifiers.
 */
public final class CheckDigitDin {
    private static final Map<Character, Integer> TO_NUMERIC_VALUE = buildLookup();

    private CheckDigitDin() {}

    /**
     * Computes the DIN check digit for a contract ID body.
     *
     * @param contractId identifier payload without computed check digit
     * @return computed check digit
     */
    public static char compute(String contractId) {
        String normalized = contractId.toUpperCase(Locale.ROOT);
        int[] values = new int[normalized.length()];
        for (int i = 0; i < normalized.length(); i++) {
            Character c = normalized.charAt(i);
            Integer lookup = TO_NUMERIC_VALUE.get(c);
            if (lookup == null) {
                throw new IllegalArgumentException("Invalid character for DIN check digit: " + c);
            }
            values[i] = lookup;
        }

        int coefficient = 0;
        int sum = 0;
        for (int value : values) {
            if (value < 10) {
                sum += mult(value, coefficient);
                coefficient += 1;
            } else {
                sum += mult(value / 10, coefficient) + mult(value % 10, coefficient + 1);
                coefficient += 2;
            }
        }

        int mod = sum % 11;
        return mod >= 10 ? 'X' : Character.forDigit(mod, 10);
    }

    private static int mult(int value, int coeff) {
        return value * (1 << coeff);
    }

    private static Map<Character, Integer> buildLookup() {
        Map<Character, Integer> lookup = new HashMap<>();
        int idx = 0;
        for (char c = '0'; c <= '9'; c++) {
            lookup.put(c, idx++);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            lookup.put(c, idx++);
        }
        return Map.copyOf(lookup);
    }
}
