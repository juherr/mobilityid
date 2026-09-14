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

import { ContractId } from "./contract-id.js";
import { ContractIdStandards } from "./contract-id-standard.js";
import { CountryCode } from "./country-code.js";
import { EvseId, EvseIdDin, EvseIdIso } from "./evse-id.js";
import { OperatorIdDin, OperatorIdIso } from "./operator-id.js";
import { PartyId } from "./party-id.js";
import { PhoneCountryCode } from "./phone-country-code.js";
import { ProviderId } from "./provider-id.js";

export const MobilityIdParsers = {
  parseCountryCode(raw: string): CountryCode | null {
    return CountryCode.parse(raw);
  },

  parsePhoneCountryCode(raw: string): PhoneCountryCode | null {
    return PhoneCountryCode.parse(raw);
  },

  parseProviderId(raw: string): ProviderId | null {
    return ProviderId.parse(raw);
  },

  parseOperatorIdIso(raw: string): OperatorIdIso | null {
    return OperatorIdIso.parse(raw);
  },

  parseOperatorIdDin(raw: string): OperatorIdDin | null {
    return OperatorIdDin.parse(raw);
  },

  parsePartyId(raw: string): PartyId | null {
    return PartyId.parse(raw);
  },

  parseContractIdIso(raw: string): ContractId | null {
    return ContractId.parse(ContractIdStandards.ISO, raw);
  },

  parseContractIdDin(raw: string): ContractId | null {
    return ContractId.parse(ContractIdStandards.DIN, raw);
  },

  parseContractIdEmi3(raw: string): ContractId | null {
    return ContractId.parse(ContractIdStandards.EMI3, raw);
  },

  parseEvseId(raw: string): EvseId | null {
    return EvseId.parse(raw);
  },

  parseEvseIdIso(raw: string): EvseIdIso | null {
    return EvseIdIso.parse(raw);
  },

  parseEvseIdDin(raw: string): EvseIdDin | null {
    return EvseIdDin.parse(raw);
  },
};
