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
package dev.juherr.mobilityid4j.smoke;

import dev.juherr.mobilityid4j.ContractId;
import dev.juherr.mobilityid4j.ContractIdStandard;
import dev.juherr.mobilityid4j.EvseId;
import dev.juherr.mobilityid4j.EvseIdIso;
import dev.juherr.mobilityid4j.interpolators.MobilityIdParsers;
import org.jspecify.annotations.Nullable;

/** Exercises the public API from the module path and checks the JSpecify types are readable. */
public final class Smoke {
    private Smoke() {}

    public static void main(String[] args) {
        var module = ContractId.class.getModule();
        expect(
                module.isNamed() && module.getName().equals("dev.juherr.mobilityid4j"),
                "library loaded as a named module");

        var contract = ContractId.parseStrict(ContractIdStandard.ISO, "NL-TNM-000122045-U");
        expect(
                contract.convertTo(ContractIdStandard.DIN).standard() == ContractIdStandard.DIN,
                "conversion API reachable");
        expect(ContractId.parse(ContractIdStandard.ISO, "NL-TNM-000122045-X") == null, "tolerant parser returns null");

        // Compiles only if org.jspecify is readable through `requires static transitive`.
        @Nullable EvseId evse = MobilityIdParsers.parseEvseId("NL*TNM*E840*6487");
        expect(evse instanceof EvseIdIso, "sealed EvseId resolves to EvseIdIso");

        System.out.println("mobilityid4j consumer smoke: OK (" + contract + ", " + evse + ")");
    }

    private static void expect(boolean condition, String what) {
        if (!condition) {
            throw new IllegalStateException("consumer smoke failed: " + what);
        }
    }
}
