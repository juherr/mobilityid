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

import { ISO_3166_ALPHA2 } from "./iso3166-alpha2.js";
import { type StringIdCompanion, defineStringId } from "./string-id.js";

const COUNTRY_CODE_REGEX = /^[A-Za-z]{2}$/;

const KNOWN_CODES: ReadonlySet<string> = new Set(ISO_3166_ALPHA2);

// A literal union rather than a brand: the set is closed and published, so known literals
// type-check directly and a switch over codes is exhaustiveness-checkable. Lowercase literals do
// not type-check, but `CountryCode.from` still normalizes them: external input goes through it.
export type CountryCode = (typeof ISO_3166_ALPHA2)[number];

export const CountryCode: StringIdCompanion<CountryCode> = defineStringId({
  isValid: (raw) => COUNTRY_CODE_REGEX.test(raw) && KNOWN_CODES.has(raw.toUpperCase()),
  message: () => "Country Code must be valid according to ISO 3166-1 alpha-2",
});
