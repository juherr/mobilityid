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

import { asciiUpperCase } from "./ascii.js";
import { CountryCode } from "./country-code.js";
import { OperatorIdDin, OperatorIdIso, type OperatorId } from "./operator-id.js";
import {
  type ParseFailureReason,
  type ParseResult,
  ValidationError,
  attempt,
  failure,
  valueOrNull,
} from "./parse-result.js";
import { PartyId } from "./party-id.js";
import { PhoneCountryCode } from "./phone-country-code.js";

type FormatError = Readonly<{ priority: number; description: string; reason: ParseFailureReason }>;

const ISO_COUNTRY_REGEX = /^([A-Za-z]{2})$/;
const ISO_OPERATOR_REGEX = /^([A-Za-z0-9]{3})$/;
const ISO_POWER_OUTLET_REGEX = /^([A-Za-z0-9*]{1,31})$/;
// A first starred segment that starts with a country and an operator without separator, or a
// second one that starts with an operator and the id type: the input is compact or mixes the
// compact and starred forms (a power outlet id may hold separators), so its segments cannot be
// attributed reliably.
const ISO_COUNTRY_AND_OPERATOR_REGEX = /^[A-Za-z]{2}[A-Za-z0-9]{3}/;
const ISO_OPERATOR_AND_ID_TYPE_REGEX = /^[A-Za-z0-9]{3}[Ee]/;
// The E id type is matched case-insensitively like the rest of the id (OCPI CiString, OCHP ABNF);
// toString() renders it in uppercase.
const ISO_EVSE_REGEX = /^([A-Za-z]{2})\*?([A-Za-z0-9]{3})\*?[Ee]([A-Za-z0-9*]{1,31})$/;

const DIN_COUNTRY_REGEX = /^(\+?[0-9]{1,3})$/;
const DIN_OPERATOR_REGEX = /^([0-9]{3,6})$/;
const DIN_POWER_OUTLET_REGEX = /^([0-9*]{1,32})$/;
// More digits than a phone country code or an operator id holds.
const DIN_COUNTRY_AND_OPERATOR_REGEX = /^\+?[0-9]{4,}$/;
const DIN_OVERLONG_OPERATOR_REGEX = /^[0-9]{7,}$/;
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
      asciiUpperCase(countryCode),
      asciiUpperCase(operatorId),
      asciiUpperCase(powerOutletId),
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
      throw new ValidationError(`Invalid ISO EVSE ID: ${raw}`, diagnoseIso(raw));
    }

    return createIso(asciiUpperCase(match[1]), asciiUpperCase(match[2]), asciiUpperCase(match[3]));
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
      asciiUpperCase(countryCode),
      asciiUpperCase(operatorId),
      asciiUpperCase(powerOutletId),
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
      throw new ValidationError(`Invalid DIN EVSE ID: ${raw}`, diagnoseDin(raw));
    }

    return createDin(asciiUpperCase(match[1]), asciiUpperCase(match[2]), asciiUpperCase(match[3]));
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
    return {
      priority: 1,
      description: "Invalid countryCode for ISO or DIN format",
      reason: "country-code",
    };
  }
  if (!ISO_OPERATOR_REGEX.test(operatorId)) {
    return { priority: 2, description: "Invalid operatorId for ISO format", reason: "operator-id" };
  }
  if (!ISO_POWER_OUTLET_REGEX.test(powerOutletId)) {
    return {
      priority: 3,
      description: "Invalid powerOutletId for ISO format",
      reason: "power-outlet-id",
    };
  }

  return null;
}

function validateDin(
  countryCode: string,
  operatorId: string,
  powerOutletId: string,
): FormatError | null {
  if (!DIN_COUNTRY_REGEX.test(countryCode)) {
    return {
      priority: 1,
      description: "Invalid countryCode for ISO or DIN format",
      reason: "phone-country-code",
    };
  }
  if (!DIN_OPERATOR_REGEX.test(operatorId)) {
    return { priority: 2, description: "Invalid operatorId for DIN format", reason: "operator-id" };
  }
  if (!DIN_POWER_OUTLET_REGEX.test(powerOutletId)) {
    return {
      priority: 3,
      description: "Invalid powerOutletId for DIN format",
      reason: "power-outlet-id",
    };
  }

  return null;
}

/**
 * Names the failing segment of an ISO EVSE id the full pattern rejected. Only the starred form
 * `country*operator*Eoutlet` splits reliably; compact or mixed forms are `format`.
 */
function diagnoseIso(raw: string): ParseFailureReason {
  const [country, operator, ...rest] = raw.split("*");
  if (operator === undefined || rest.length === 0) {
    return "format";
  }
  if (ISO_COUNTRY_AND_OPERATOR_REGEX.test(country ?? "")) {
    return "format";
  }
  if (operator.length > 3 && ISO_OPERATOR_AND_ID_TYPE_REGEX.test(operator)) {
    return "format";
  }
  if (!CountryCode.isValid(country ?? "")) {
    return "country-code";
  }
  if (!ISO_OPERATOR_REGEX.test(operator)) {
    return "operator-id";
  }
  const typedOutlet = rest.join("*");
  if (!typedOutlet.startsWith("E") && !typedOutlet.startsWith("e")) {
    return "id-type";
  }
  if (!ISO_POWER_OUTLET_REGEX.test(typedOutlet.slice(1))) {
    return "power-outlet-id";
  }
  return "format";
}

/**
 * Names the failing segment of a DIN EVSE id, whose separators are mandatory. A digit run too
 * long for its segment may be a missing separator (`+49810*000*1`), so it is `format`.
 */
function diagnoseDin(raw: string): ParseFailureReason {
  const [country, operator, ...rest] = raw.split("*");
  if (operator === undefined || rest.length === 0) {
    return "format";
  }
  if (
    DIN_COUNTRY_AND_OPERATOR_REGEX.test(country ?? "") ||
    DIN_OVERLONG_OPERATOR_REGEX.test(operator)
  ) {
    return "format";
  }
  const error = validateDin(country ?? "", operator, rest.join("*"));
  return error === null ? "format" : error.reason;
}

function createIso(countryCode: string, operatorId: string, powerOutletId: string): EvseIdIso {
  const country = CountryCode.from(countryCode);
  const operator = OperatorIdIso.from(operatorId);
  if (!ISO_POWER_OUTLET_REGEX.test(powerOutletId)) {
    throw new ValidationError("Invalid powerOutletId for ISO format", "power-outlet-id");
  }
  // The E type marker is part of the rendering, not of the power outlet id: parsing already
  // consumed it and a power outlet id given by parts may itself start with E (as in Scala).
  return new EvseIdIso(country, operator, powerOutletId);
}

function createDin(countryCode: string, operatorId: string, powerOutletId: string): EvseIdDin {
  const countryWithPlus = countryCode.startsWith("+") ? countryCode : `+${countryCode}`;
  const country = PhoneCountryCode.from(countryWithPlus);
  const operator = OperatorIdDin.from(operatorId);
  if (!DIN_POWER_OUTLET_REGEX.test(powerOutletId)) {
    throw new ValidationError("Invalid powerOutletId for DIN format", "power-outlet-id");
  }
  return new EvseIdDin(country, operator, powerOutletId);
}

export type EvseId = EvseIdIso | EvseIdDin;

export const EvseId = {
  fromParts(countryCode: string, operatorId: string, powerOutletId: string): EvseId {
    const normalizedCountry = asciiUpperCase(countryCode);
    const normalizedOperator = asciiUpperCase(operatorId);
    const normalizedPowerOutlet = asciiUpperCase(powerOutletId);

    const isoError = validateIso(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    if (isoError === null) {
      return createIso(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    }

    const dinError = validateDin(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    if (dinError === null) {
      return createDin(normalizedCountry, normalizedOperator, normalizedPowerOutlet);
    }

    const error = isoError.priority >= dinError.priority ? isoError : dinError;
    throw new ValidationError(error.description, error.reason);
  },

  // ISO is tried first, then DIN; a failure message carries both reasons since neither format is
  // implied. The reason code is the one of the format the input looks like: a leading letter is
  // ISO, a leading `+` or digit is DIN, anything else is `format`.
  tryParse(raw: string): ParseResult<EvseId> {
    const iso = EvseIdIso.tryParse(raw);
    if (iso.ok) {
      return iso;
    }

    const din = EvseIdDin.tryParse(raw);
    if (din.ok) {
      return din;
    }

    const reason = /^[A-Za-z]/.test(raw) ? iso.reason : /^[+0-9]/.test(raw) ? din.reason : "format";
    return failure(`Invalid EVSE ID: ${raw} (ISO: ${iso.error}; DIN: ${din.error})`, reason);
  },

  parse(raw: string): EvseId | null {
    return valueOrNull(EvseId.tryParse(raw));
  },

  parseStrict(raw: string): EvseId {
    const result = EvseId.tryParse(raw);
    if (!result.ok) {
      throw new ValidationError(result.error, result.reason);
    }

    return result.value;
  },
};
