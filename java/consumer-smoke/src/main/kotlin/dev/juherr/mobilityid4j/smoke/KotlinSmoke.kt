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
package dev.juherr.mobilityid4j.smoke

import dev.juherr.mobilityid4j.ContractId
import dev.juherr.mobilityid4j.ContractIdStandard
import dev.juherr.mobilityid4j.EvseIdDin
import dev.juherr.mobilityid4j.EvseIdIso
import dev.juherr.mobilityid4j.interpolators.MobilityIdParsers

/**
 * Compile-time proof that Kotlin reads the JSpecify contracts. Every statement is written so that
 * the opposite nullability fails the build under `-Xjspecify-annotations=strict -Werror`:
 * - a strict factory result is assigned to a non-null type without `!!` (fails if it became nullable);
 * - a tolerant parser result is used through `?.` and matched against `null` directly on the call
 *   expression, never through a widened `T?` declaration (fails with "unnecessary safe call" /
 *   "senseless null" if it became non-null).
 */
object KotlinSmoke {
    @JvmStatic
    fun main(args: Array<String>) {
        val strict: ContractId = ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U")
        check(strict.countryCode().value() == "NL")

        val invalid = ContractId.parse(ContractIdStandard.ISO, "NL-TNM-000122045-X")?.toCompactString()
        check(invalid == null) { "tolerant parser should return null" }

        val rendered = when (val evse = MobilityIdParsers.parseEvseId("+49*810*000*438")) {
            is EvseIdIso -> "iso:" + evse.toCompactString()
            is EvseIdDin -> "din:" + evse.toString()
            null -> "invalid"
        }
        check(rendered.startsWith("din:")) { "expected a DIN EVSE id, got $rendered" }

        val country = MobilityIdParsers.parseCountryCode("XX")?.value() ?: "none"
        check(country == "none")

        println("mobilityid4j Kotlin consumer smoke: OK ($strict, $rendered)")
    }
}
