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

import java.util.Arrays;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Prints the ISO 3166-1 alpha-2 codes known to the JDK, one per line, sorted. This is the same
 * source the Scala reference and the Java port validate country codes against
 * ({@code Locale.getISOCountries()}), so the Go port stays in parity with them. Run through
 * {@code scripts/generate-iso3166.sh}, not directly.
 */
public final class Iso3166Codes {
    private Iso3166Codes() {}

    public static void main(String[] args) {
        for (String code : new TreeSet<>(Arrays.asList(Locale.getISOCountries()))) {
            System.out.println(code);
        }
    }
}
