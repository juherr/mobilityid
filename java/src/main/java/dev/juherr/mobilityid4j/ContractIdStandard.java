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

/** Supported contract ID standards. */
public enum ContractIdStandard {
    /** ISO 15118-1 format. */
    ISO("ISO 15118-1"),
    /** EMI3 format. */
    EMI3("EMI3"),
    /** DIN SPEC 91286 format. */
    DIN("DIN SPEC 91286");

    private final String displayName;

    ContractIdStandard(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable standard name.
     *
     * @return display name
     */
    public String displayName() {
        return displayName;
    }
}
