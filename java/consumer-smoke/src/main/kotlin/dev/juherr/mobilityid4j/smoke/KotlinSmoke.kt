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
package dev.juherr.mobilityid4j.smoke

import dev.juherr.mobilityid4j.ContractId
import dev.juherr.mobilityid4j.ContractIdStandard
import dev.juherr.mobilityid4j.EvseId
import dev.juherr.mobilityid4j.EvseIdDin
import dev.juherr.mobilityid4j.EvseIdIso
import dev.juherr.mobilityid4j.interpolators.MobilityIdParsers

/**
 * Compiles only if Kotlin reads the JSpecify contracts: strict factories are non-null, tolerant
 * parsers are `T?`, sealed interfaces are exhaustive in `when`.
 */
object KotlinSmoke {
    @JvmStatic
    fun main(args: Array<String>) {
        // Non-null: assigning to a non-null type without `!!` must compile.
        val strict: ContractId = ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U")

        // Nullable: the safe-call chain must be required (a plain call would not compile under -Werror).
        val tolerant: ContractId? = ContractId.parse(ContractIdStandard.ISO, "NL-TNM-000122045-X")
        check(tolerant?.toCompactString() == null) { "tolerant parser should return null" }

        val evse: EvseId? = MobilityIdParsers.parseEvseId("+49*810*000*438")
        val rendered = when (evse) {
            is EvseIdIso -> "iso:" + evse.toCompactString()
            is EvseIdDin -> "din:" + evse.toString()
            null -> "invalid"
        }
        check(rendered.startsWith("din:")) { "expected a DIN EVSE id, got " }
        check(strict.countryCode().value() == "NL")
        println("mobilityid4j Kotlin consumer smoke: OK ($strict, $rendered)")
    }
}
