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

import {
  type StringId,
  type StringIdCompanion,
  type StringIdSpec,
  defineStringId,
} from "./string-id.js";

const PARTY_CODE_REGEX = /^[A-Za-z0-9]{3}$/;

/** Shared by `ProviderId` and `OperatorIdIso`: both are the 3-character party code of a party id. */
export const partyCodeSpec: StringIdSpec = {
  isValid: (raw) => PARTY_CODE_REGEX.test(raw),
  message: () => "OperatorId must have a length of 3 and be ASCII letters or digits",
};

export type ProviderId = StringId<"ProviderId">;

export const ProviderId: StringIdCompanion<ProviderId> = defineStringId(partyCodeSpec);
