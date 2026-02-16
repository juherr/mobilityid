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

import { checkDigitDin } from "./check-digit-din.js";
import { checkDigitIso } from "./check-digit-iso.js";
import { ContractIdStandards, type ContractIdStandard } from "./contract-id-standard.js";
import { CountryCode } from "./country-code.js";
import { PartyId } from "./party-id.js";
import { ProviderId } from "./provider-id.js";

type Parser = Readonly<{
  instanceRegex: RegExp;
  fullRegex: RegExp;
  name: string;
  computeCheckDigit: (value: string) => string;
}>;

const parsers: Readonly<Record<ContractIdStandard, Parser>> = {
  ISO: {
    instanceRegex: /^([A-Za-z0-9]{9})$/,
    fullRegex:
      /^([A-Za-z]{2})(?:-?)([A-Za-z0-9]{3})(?:-?)([A-Za-z0-9]{9})(?:(?:-?)([A-Za-z0-9]))?$/,
    name: "ISO 15118-1",
    computeCheckDigit: checkDigitIso,
  },
  EMI3: {
    instanceRegex: /^([Cc][A-Za-z0-9]{8})$/,
    fullRegex:
      /^([A-Za-z]{2})(?:-?)([A-Za-z0-9]{3})(?:-?)([Cc][A-Za-z0-9]{8})(?:(?:-?)([A-Za-z0-9]))?$/,
    name: "EMI3",
    computeCheckDigit: checkDigitIso,
  },
  DIN: {
    instanceRegex: /^([A-Za-z0-9]{6})$/,
    fullRegex:
      /^([A-Za-z]{2})(?:[*-]?)([A-Za-z0-9]{3})(?:[*-]?)([A-Za-z0-9]{6})(?:(?:[*-]?)([A-Za-z0-9]))?$/,
    name: "DIN SPEC 91286",
    computeCheckDigit: checkDigitDin,
  },
};

export class ContractId {
  public readonly standard: ContractIdStandard;
  public readonly countryCode: CountryCode;
  public readonly providerId: ProviderId;
  public readonly instanceValue: string;
  public readonly checkDigit: string;

  private constructor(
    standard: ContractIdStandard,
    countryCode: CountryCode,
    providerId: ProviderId,
    instanceValue: string,
    checkDigit: string,
  ) {
    this.standard = standard;
    this.countryCode = countryCode;
    this.providerId = providerId;
    this.instanceValue = instanceValue;
    this.checkDigit = checkDigit;
    Object.freeze(this);
  }

  public static fromParts(
    standard: ContractIdStandard,
    countryCode: string | CountryCode,
    providerId: string | ProviderId,
    instanceValue: string,
    checkDigit?: string,
  ): ContractId {
    const parser = parsers[standard];
    const cc = typeof countryCode === "string" ? CountryCode.from(countryCode) : countryCode;
    const provider = typeof providerId === "string" ? ProviderId.from(providerId) : providerId;
    const normalizedInstance = instanceValue.toUpperCase();

    if (!parser.instanceRegex.test(normalizedInstance)) {
      throw new TypeError(
        `${instanceValue} is not a valid instance value for ${parser.name} format`,
      );
    }

    const computed = parser.computeCheckDigit(`${cc}${provider}${normalizedInstance}`);
    if (checkDigit !== undefined && checkDigit.toUpperCase() !== computed) {
      throw new TypeError(
        `Given check digit '${checkDigit}' is not equal to computed '${computed}'`,
      );
    }

    return new ContractId(standard, cc, provider, normalizedInstance, computed);
  }

  public static parse(standard: ContractIdStandard, raw: string): ContractId | null {
    try {
      return ContractId.parseStrict(standard, raw);
    } catch {
      return null;
    }
  }

  public static parseStrict(standard: ContractIdStandard, raw: string): ContractId {
    const parser = parsers[standard];
    const match = parser.fullRegex.exec(raw);
    if (!match || match[1] === undefined || match[2] === undefined || match[3] === undefined) {
      throw new TypeError(`${raw} is not a valid Contract Id for ${parser.name}`);
    }

    const [, country, provider, instance, check] = match;
    return ContractId.fromParts(standard, country, provider, instance, check);
  }

  public toCompactString(): string {
    return `${this.countryCode}${this.providerId}${this.instanceValue}${this.checkDigit}`;
  }

  public toCompactStringWithoutCheckDigit(): string {
    return `${this.countryCode}${this.providerId}${this.instanceValue}`;
  }

  public get partyId(): PartyId {
    return PartyId.fromCountryAndProvider(this.countryCode, this.providerId);
  }

  public convertTo(targetStandard: ContractIdStandard): ContractId {
    if (targetStandard === this.standard) {
      return this;
    }

    if (this.standard === ContractIdStandards.DIN && targetStandard === ContractIdStandards.EMI3) {
      return ContractId.fromParts(
        ContractIdStandards.EMI3,
        this.countryCode,
        this.providerId,
        `C0${this.instanceValue}${this.checkDigit}`,
      );
    }

    if (this.standard === ContractIdStandards.EMI3 && targetStandard === ContractIdStandards.DIN) {
      if (!this.instanceValue.startsWith("C0")) {
        throw new TypeError(`${this} cannot be converted to ${parsers.DIN.name} format`);
      }

      const dinInstance = this.instanceValue.slice(2, 8);
      const dinCheck = this.instanceValue.slice(8, 9);
      return ContractId.fromParts(
        ContractIdStandards.DIN,
        this.countryCode,
        this.providerId,
        dinInstance,
        dinCheck,
      );
    }

    if (this.standard === ContractIdStandards.ISO && targetStandard === ContractIdStandards.DIN) {
      if (!this.instanceValue.startsWith("00")) {
        throw new TypeError(`${this} cannot be converted to ${parsers.DIN.name} format`);
      }

      const dinInstance = this.instanceValue.slice(2, 8);
      const dinCheck = this.instanceValue.slice(8, 9);
      return ContractId.fromParts(
        ContractIdStandards.DIN,
        this.countryCode,
        this.providerId,
        dinInstance,
        dinCheck,
      );
    }

    if (this.standard === ContractIdStandards.DIN && targetStandard === ContractIdStandards.ISO) {
      return ContractId.fromParts(
        ContractIdStandards.ISO,
        this.countryCode,
        this.providerId,
        `00${this.instanceValue}${this.checkDigit}`,
      );
    }

    if (this.standard === ContractIdStandards.EMI3 && targetStandard === ContractIdStandards.ISO) {
      return ContractId.fromParts(
        ContractIdStandards.ISO,
        this.countryCode,
        this.providerId,
        this.instanceValue,
        this.checkDigit,
      );
    }

    if (this.standard === ContractIdStandards.ISO && targetStandard === ContractIdStandards.EMI3) {
      return ContractId.fromParts(
        ContractIdStandards.EMI3,
        this.countryCode,
        this.providerId,
        this.instanceValue,
        this.checkDigit,
      );
    }

    throw new TypeError(`It is not possible to convert ${this.standard} to ${targetStandard}`);
  }

  public toString(): string {
    return `${this.countryCode}-${this.providerId}-${this.instanceValue}-${this.checkDigit}`;
  }
}
