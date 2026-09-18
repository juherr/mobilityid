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

const PHONE_COUNTRY_CODE_REGEX = /^\+?[0-9]{1,3}$/;

export type PhoneCountryCode = StringId<"PhoneCountryCode">;

export const PhoneCountryCode: StringIdCompanion<PhoneCountryCode> = defineStringId({
  isValid: (raw) => PHONE_COUNTRY_CODE_REGEX.test(raw),
  message: (raw) =>
    `phone Country Code must start with a '+' sign and be followed by 1-3 digits. (Was: ${raw})`,
});
