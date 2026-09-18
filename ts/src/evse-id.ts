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

import { CountryCode } from "./country-code.js";
import { OperatorIdDin, OperatorIdIso, type OperatorId } from "./operator-id.js";
import {
  type ParseResult,
  ValidationError,
  attempt,
  failure,
  valueOrNull,
} from "./parse-result.js";
import { PartyId } from "./party-id.js";
import { PhoneCountryCode } from "./phone-country-code.js";

type FormatError = Readonly<{ priority: number; description: string }>;

const ISO_COUNTRY_REGEX = /^([A-Za-z]{2})$/;
const ISO_OPERATOR_REGEX = /^([A-Za-z0-9]{3})$/;
const ISO_POWER_OUTLET_REGEX = /^([A-Za-z0-9*]{1,31})$/;
const ISO_EVSE_REGEX = /^([A-Za-z]{2})\*?([A-Za-z0-9]{3})\*?E([A-Za-z0-9*]{1,31})$/;

const DIN_COUNTRY_REGEX = /^(\+?[0-9]{1,3})$/;
const DIN_OPERATOR_REGEX = /^([0-9]{3,6})$/;
const DIN_POWER_OUTLET_REGEX = /^([0-9*]{1,32})$/;
const DIN_EVSE_REGEX = /^(\+?[0-9]{1,3})\*([0-9]{3,6})\*([0-9*]{1,32})$/;

abstract class EvseIdBase {
  public abstract readonly countryCode: CountryCode | PhoneCountryCode;
  public abstract readonly operatorId: OperatorId;
  public abstract readonly powerOutletId: string;

  public abstract toString(): string;
}

export class EvseIdIso extends EvseIdBase {
  public readonly countryCode: CountryCode;
  public readonly operatorId: OperatorIdIso;
  public readonly powerOutletId: string;

  public constructor(countryCode: CountryCode, operatorId: OperatorIdIso, powerOutletId: string) {
    super();
    this.countryCode = countryCode;
    this.operatorId = operatorId;
    this.powerOutletId = powerOutletId;
    Object.freeze(this);
  }

  public static fromParts(
    countryCode: string,
    operatorId: string,
    powerOutletId: string,
  ): EvseIdIso {
    return createIso(
      countryCode.toUpperCase(),
      operatorId.toUpperCase(),
      powerOutletId.toUpperCase(),
    );
  }

  public static tryParse(raw: string): ParseResult<EvseIdIso> {
    return attempt(() => EvseIdIso.parseStrict(raw));
  }

  public static parse(raw: string): EvseIdIso | null {
    return valueOrNull(EvseIdIso.tryParse(raw));
  }

  public static parseStrict(raw: string): EvseIdIso {
    const match = ISO_EVSE_REGEX.exec(raw);
    if (!match || match[1] === undefined || match[2] === undefined || match[3] === undefined) {
      throw new ValidationError(`Invalid ISO EVSE ID: ${raw}`);
    }

    return createIso(match[1].toUpperCase(), match[2].toUpperCase(), match[3].toUpperCase());
  }

  public get partyId(): PartyId {
    return PartyId.fromCountryAndOperator(this.countryCode, this.operatorId);
  }

  public toCompactString(): string {
    return this.toString().replaceAll("*", "");
  }

  public toString(): string {
    return `${this.countryCode}*${this.operatorId}*E${this.powerOutletId}`;
  }
}

export class EvseIdDin extends EvseIdBase {
  public readonly countryCode: PhoneCountryCode;
  public readonly operatorId: OperatorIdDin;
  public readonly powerOutletId: string;

  public constructor(
    countryCode: PhoneCountryCode,
    operatorId: OperatorIdDin,
    powerOutletId: string,
  ) {
    super();
    this.countryCode = countryCode;
    this.operatorId = operatorId;
    this.powerOutletId = powerOutletId;
    Object.freeze(this);
  }

  public static fromParts(
    countryCode: string,
    operatorId: string,
    powerOutletId: string,
  ): EvseIdDin {
    return createDin(
      countryCode.toUpperCase(),
      operatorId.toUpperCase(),
      powerOutletId.toUpperCase(),
    );
  }

  public static tryParse(raw: string): ParseResult<EvseIdDin> {
    return attempt(() => EvseIdDin.parseStrict(raw));
  }

  public static parse(raw: string): EvseIdDin | null {
    return valueOrNull(EvseIdDin.tryParse(raw));
  }

  public static parseStrict(raw: string): EvseIdDin {
    const match = DIN_EVSE_REGEX.exec(raw);
    if (!match || match[1] === undefined || match[2] === undefined || match[3] === undefined) {
      throw new ValidationError(`Invalid DIN EVSE ID: ${raw}`);
    }

    return createDin(match[1].toUpperCase(), match[2].toUpperCase(), match[3].toUpperCase());
  }

  public toString(): string {
    return `${this.countryCode}*${this.operatorId}*${this.powerOutletId}`;
  }
}

function validateIso(
  countryCode: string,
  operatorId: string,
  powerOutletId: string,
): FormatError | null {
  if (!ISO_COUNTRY_REGEX.test(countryCode)) {
    return { priority: 1, description: "Invalid countryCode for ISO or DIN format" };
  }
  if (!ISO_OPERATOR_REGEX.test(operatorId)) {
    return { priority: 2, description: "Invalid operatorId for ISO format" };
  }
  if (!ISO_POWER_OUTLET_REGEX.test(powerOutletId)) {
    return { priority: 3, description: "Invalid powerOutletId for ISO format" };
  }

  return null;
}

function validateDin(
  countryCode: string,
  operatorId: string,
  powerOutletId: string,
): FormatError | null {
  if (!DIN_COUNTRY_REGEX.test(countryCode)) {
    return { priority: 1, description: "Invalid countryCode for ISO or DIN format" };
  }
  if (!DIN_OPERATOR_REGEX.test(operatorId)) {
    return { priority: 2, description: "Invalid operatorId for DIN format" };
  }
  if (!DIN_POWER_OUTLET_REGEX.test(powerOutletId)) {
    return { priority: 3, description: "Invalid powerOutletId for DIN format" };
  }

  return null;
}

function createIso(countryCode: string, operatorId: string, powerOutletId: string): EvseIdIso {
  const country = CountryCode.from(countryCode);
  const operator = OperatorIdIso.from(operatorId);
  // The E type marker is part of the rendering, not of the power outlet id: parsing already
  // consumed it and a power outlet id given by parts may itself start with E (as in Scala).
  return new EvseIdIso(country, operator, powerOutletId);
}

function createDin(countryCode: string, operatorId: string, powerOutletId: string): EvseIdDin {
  const countryWithPlus = countryCode.startsWith("+") ? countryCode : `+${countryCode}`;
  const country = PhoneCountryCode.from(countryWithPlus);
  const operator = OperatorIdDin.from(operatorId);
  return new EvseIdDin(country, operator, powerOutletId);
}

export type EvseId = EvseIdIso | EvseIdDin;

export const EvseId = {
  fromParts(countryCode: string, operatorId: string, powerOutletId: string): EvseId {
    const normalizedCountry = countryCode.toUpperCase();
    const normalizedOperator = operatorId.toUpperCase();
    const normalizedPowerOutlet = powerOutletId.toUpperCase();

    const isoError = validateIso(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    if (isoError === null) {
      return createIso(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    }

    const dinError = validateDin(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    if (dinError === null) {
      return createDin(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    }

    if (isoError.priority >= dinError.priority) {
      throw new ValidationError(isoError.description);
    }

    throw new ValidationError(dinError.description);
  },

  // ISO is tried first, then DIN; a failure carries both reasons since neither format is implied.
  tryParse(raw: string): ParseResult<EvseId> {
    const iso = EvseIdIso.tryParse(raw);
    if (iso.ok) {
      return iso;
    }

    const din = EvseIdDin.tryParse(raw);
    if (din.ok) {
      return din;
    }

    return failure(`Invalid EVSE ID: ${raw} (ISO: ${iso.error}; DIN: ${din.error})`);
  },

  parse(raw: string): EvseId | null {
    return valueOrNull(EvseId.tryParse(raw));
  },

  parseStrict(raw: string): EvseId {
    const result = EvseId.tryParse(raw);
    if (!result.ok) {
      throw new ValidationError(result.error);
    }

    return result.value;
  },
};
