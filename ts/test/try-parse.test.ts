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

import { describe, expect, it } from "vite-plus/test";

import {
  checkDigitDin,
  checkDigitIso,
  ContractId,
  ContractIdStandards,
  CountryCode,
  EvseId,
  EvseIdDin,
  EvseIdIso,
  MobilityIdParsers,
  OperatorIdDin,
  OperatorIdIso,
  PartyId,
  PhoneCountryCode,
  ProviderId,
  ValidationError,
  type ParseResult,
} from "../src/index.js";
import { attempt } from "../src/parse-result.js";

// A failed result must not carry a value, a successful one must not carry an error: the union is
// what lets callers narrow on `ok` without a cast.
function expectFailure<T>(result: ParseResult<T>): string {
  expect(result.ok).toBe(false);
  if (result.ok) {
    throw new Error("expected a failure");
  }
  expect(result).not.toHaveProperty("value");
  return result.error;
}

function expectSuccess<T>(result: ParseResult<T>): T {
  if (!result.ok) {
    throw new Error(`expected a success, got: ${result.error}`);
  }
  expect(result).not.toHaveProperty("error");
  return result.value;
}

describe("tryParse", () => {
  it("returns the value the strict parser would return", () => {
    expect(expectSuccess(CountryCode.tryParse("nl")).toString()).toBe("NL");
    expect(expectSuccess(PhoneCountryCode.tryParse("+49")).toString()).toBe("+49");
    expect(expectSuccess(ProviderId.tryParse("tnm")).toString()).toBe("TNM");
    expect(expectSuccess(OperatorIdIso.tryParse("ab7")).toString()).toBe("AB7");
    expect(expectSuccess(OperatorIdDin.tryParse("810")).toString()).toBe("810");
    expect(expectSuccess(PartyId.tryParse("NL*TNM")).toString()).toBe("NL-TNM");
    expect(
      expectSuccess(ContractId.tryParse(ContractIdStandards.ISO, "NLTNM000122045")).toString(),
    ).toBe("NL-TNM-000122045-U");
    expect(expectSuccess(EvseIdIso.tryParse("DE*AB7*E840*6487")).toString()).toBe(
      "DE*AB7*E840*6487",
    );
    expect(expectSuccess(EvseIdDin.tryParse("+49*810*000*438")).toString()).toBe("+49*810*000*438");
    expect(expectSuccess(EvseId.tryParse("+49*810*000*438"))).toBeInstanceOf(EvseIdDin);
    expect(expectSuccess(EvseId.tryParse("DE*AB7*E840*6487"))).toBeInstanceOf(EvseIdIso);
  });

  it("reports the same reason the strict parser throws", () => {
    const cases: ReadonlyArray<readonly [() => ParseResult<unknown>, () => unknown]> = [
      [() => CountryCode.tryParse("ZZ"), () => CountryCode.from("ZZ")],
      [() => PhoneCountryCode.tryParse("+4A"), () => PhoneCountryCode.from("+4A")],
      [() => ProviderId.tryParse("T|M"), () => ProviderId.from("T|M")],
      [() => OperatorIdIso.tryParse("ABCD"), () => OperatorIdIso.from("ABCD")],
      [() => OperatorIdDin.tryParse("12A"), () => OperatorIdDin.from("12A")],
      [() => PartyId.tryParse("XY-TNM"), () => PartyId.parseStrict("XY-TNM")],
      [
        () => ContractId.tryParse(ContractIdStandards.ISO, "NL-TNM-000122045-X"),
        () => ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-X"),
      ],
      [() => EvseIdIso.tryParse("NL*TNM*840*6487"), () => EvseIdIso.parseStrict("NL*TNM*840*6487")],
      [() => EvseIdDin.tryParse("+49*AB7*840"), () => EvseIdDin.parseStrict("+49*AB7*840")],
      [() => EvseId.tryParse("NL*TNM*840*6487"), () => EvseId.parseStrict("NL*TNM*840*6487")],
    ];

    for (const [tryParse, strict] of cases) {
      let thrown: unknown;
      try {
        strict();
      } catch (error) {
        thrown = error;
      }
      expect(thrown).toBeInstanceOf(TypeError);
      expect(expectFailure(tryParse())).toBe((thrown as TypeError).message);
    }
  });

  it("names the failed field, not just the input", () => {
    expect(expectFailure(ContractId.tryParse(ContractIdStandards.ISO, "NL-TNM-000122045-X"))).toBe(
      "Given check digit 'X' is not equal to computed 'U'",
    );
    expect(expectFailure(ContractId.tryParse(ContractIdStandards.DIN, "NL-TNM-00012"))).toBe(
      "NL-TNM-00012 is not a valid Contract Id for DIN SPEC 91286",
    );
    expect(expectFailure(EvseIdIso.tryParse("ZZ*TNM*E840"))).toBe(
      "Country Code must be valid according to ISO 3166-1 alpha-2",
    );
    expect(expectFailure(EvseIdIso.tryParse("NL*TNM*840*6487"))).toBe(
      "Invalid ISO EVSE ID: NL*TNM*840*6487",
    );
    expect(expectFailure(EvseId.tryParse("NL*TNM*840*6487"))).toBe(
      "Invalid EVSE ID: NL*TNM*840*6487 (ISO: Invalid ISO EVSE ID: NL*TNM*840*6487; DIN: Invalid DIN EVSE ID: NL*TNM*840*6487)",
    );
  });

  it("only captures ValidationError: any other error is a bug and keeps propagating", () => {
    expect(() =>
      attempt(() => {
        throw new RangeError("not an invalid input");
      }),
    ).toThrow(RangeError);
    // A native TypeError from a programming mistake must not become a failure result.
    const broken = null as unknown as { value: string };
    expect(() => attempt(() => broken.value.length)).toThrow(TypeError);
    expect(() =>
      attempt(() => {
        throw new TypeError("plain TypeError, not a validation failure");
      }),
    ).toThrow(TypeError);
  });

  it("strict parsers throw ValidationError, which is still a TypeError", () => {
    for (const strict of [
      () => CountryCode.from("ZZ"),
      () => ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-X"),
      () => EvseId.parseStrict("NL*TNM*840*6487"),
      () => checkDigitIso(""),
      () => checkDigitDin("é"),
    ]) {
      expect(strict).toThrow(ValidationError);
      expect(strict).toThrow(TypeError);
    }
    const error = attempt(() => CountryCode.from("ZZ"));
    expect(error.ok).toBe(false);
    expect(new ValidationError("x").name).toBe("ValidationError");
  });

  it("is frozen, like the domain objects", () => {
    expect(Object.isFrozen(CountryCode.tryParse("NL"))).toBe(true);
    expect(Object.isFrozen(CountryCode.tryParse("ZZ"))).toBe(true);
  });

  it("is mirrored by MobilityIdParsers", () => {
    expect(expectSuccess(MobilityIdParsers.tryParseCountryCode("NL")).toString()).toBe("NL");
    expect(expectSuccess(MobilityIdParsers.tryParsePhoneCountryCode("+31")).toString()).toBe("+31");
    expect(expectSuccess(MobilityIdParsers.tryParseProviderId("TNM")).toString()).toBe("TNM");
    expect(expectSuccess(MobilityIdParsers.tryParseOperatorIdIso("TNM")).toString()).toBe("TNM");
    expect(expectSuccess(MobilityIdParsers.tryParseOperatorIdDin("456")).toString()).toBe("456");
    expect(expectSuccess(MobilityIdParsers.tryParsePartyId("NL-TNM")).toString()).toBe("NL-TNM");
    expect(
      expectSuccess(MobilityIdParsers.tryParseContractIdIso("NL-TNM-000722345-X")).toString(),
    ).toBe("NL-TNM-000722345-X");
    expect(
      expectSuccess(MobilityIdParsers.tryParseContractIdDin("NL-TNM-722345-8")).toString(),
    ).toBe("NL-TNM-722345-8");
    expect(
      expectSuccess(MobilityIdParsers.tryParseContractIdEmi3("NL-TNM-C00722345-N")).toString(),
    ).toBe("NL-TNM-C00722345-N");
    expect(expectSuccess(MobilityIdParsers.tryParseEvseId("NL*TNM*E840*6487")).toString()).toBe(
      "NL*TNM*E840*6487",
    );
    expect(expectSuccess(MobilityIdParsers.tryParseEvseIdIso("NL*TNM*E840*6487")).toString()).toBe(
      "NL*TNM*E840*6487",
    );
    expect(expectSuccess(MobilityIdParsers.tryParseEvseIdDin("+49*810*000*438")).toString()).toBe(
      "+49*810*000*438",
    );
    expect(expectFailure(MobilityIdParsers.tryParseContractIdIso("NL-TNM-000122045-X"))).toBe(
      "Given check digit 'X' is not equal to computed 'U'",
    );
  });
});
