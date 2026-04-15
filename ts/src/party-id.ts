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

import { CountryCode } from "./country-code.js";
import type { OperatorIdIso } from "./operator-id.js";
import { ProviderId } from "./provider-id.js";

const PARTY_ID_REGEX = /^([A-Za-z]{2})[-*]?([A-Za-z0-9]{3})$/;

export class PartyId {
  public readonly countryCode: CountryCode;
  public readonly partyCode: string;

  private constructor(countryCode: CountryCode, partyCode: string) {
    this.countryCode = countryCode;
    this.partyCode = partyCode;
    Object.freeze(this);
  }

  public static parse(raw: string): PartyId | null {
    const match = PARTY_ID_REGEX.exec(raw);
    if (!match) {
      return null;
    }

    try {
      const country = match[1];
      const party = match[2];
      if (country === undefined || party === undefined) {
        return null;
      }

      const countryCode = CountryCode.from(country);
      const partyCode = ProviderId.from(party).value;
      return new PartyId(countryCode, partyCode);
    } catch {
      return null;
    }
  }

  public static parseStrict(raw: string): PartyId {
    const parsed = PartyId.parse(raw);
    if (parsed === null) {
      throw new TypeError(`Invalid party ID: ${raw}`);
    }

    return parsed;
  }

  public static fromCountryAndProvider(countryCode: CountryCode, providerId: ProviderId): PartyId {
    return new PartyId(countryCode, providerId.value);
  }

  public static fromCountryAndOperator(
    countryCode: CountryCode,
    operatorId: OperatorIdIso,
  ): PartyId {
    return new PartyId(countryCode, operatorId.value);
  }

  public toCompactString(): string {
    return `${this.countryCode.toString()}${this.partyCode}`;
  }

  public toString(): string {
    return `${this.countryCode.toString()}-${this.partyCode}`;
  }
}
