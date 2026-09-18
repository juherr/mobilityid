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

import { type StringId, type StringIdCompanion, defineStringId } from "./string-id.js";

const COUNTRY_CODE_REGEX = /^[A-Za-z]{2}$/;

const regionNames = new Intl.DisplayNames(["en"], { type: "region" });

function isKnownRegion(code: string): boolean {
  const display = regionNames.of(code);
  return display !== undefined && display !== code && display !== "Unknown Region";
}

export type CountryCode = StringId<"CountryCode">;

export const CountryCode: StringIdCompanion<CountryCode> = defineStringId({
  isValid: (raw) => COUNTRY_CODE_REGEX.test(raw) && isKnownRegion(raw.toUpperCase()),
  message: () => "Country Code must be valid according to ISO 3166-1 alpha-2",
});
