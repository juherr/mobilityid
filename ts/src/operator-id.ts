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
import { partyCodeSpec } from "./provider-id.js";

const OPERATOR_DIN_REGEX = /^[0-9]{3,6}$/;

export type OperatorIdIso = StringId<"OperatorIdIso">;

export const OperatorIdIso: StringIdCompanion<OperatorIdIso> = defineStringId(partyCodeSpec);

export type OperatorIdDin = StringId<"OperatorIdDin">;

export const OperatorIdDin: StringIdCompanion<OperatorIdDin> = defineStringId({
  isValid: (raw) => OPERATOR_DIN_REGEX.test(raw),
  message: () => "OperatorId must have a length of 3-6 chars and be digits",
});

export type OperatorId = OperatorIdIso | OperatorIdDin;
