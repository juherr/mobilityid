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

import fc from "fast-check";
import { describe, expect, it } from "vite-plus/test";

import {
  checkDigitDin,
  checkDigitIso,
  ContractId,
  ContractIdStandards,
  CountryCode,
  ISO_3166_ALPHA2,
  EvseId,
  EvseIdDin,
  EvseIdIso,
  OperatorIdDin,
  OperatorIdIso,
  PartyId,
  PhoneCountryCode,
  ProviderId,
  ValidationError,
  type ContractIdStandard,
  type ParseResult,
} from "../src/index.js";

const ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split("");
const DIGITS = "0123456789".split("");

const alnum = (length: number): fc.Arbitrary<string> =>
  fc.string({ unit: fc.constantFrom(...ALNUM), minLength: length, maxLength: length });
const digits = (min: number, max: number): fc.Arbitrary<string> =>
  fc.string({ unit: fc.constantFrom(...DIGITS), minLength: min, maxLength: max });

// Country codes are validated against the generated ISO 3166-1 table, so draw from it.
const countryCode = fc.constantFrom(...ISO_3166_ALPHA2);
const unassignedCountryCode = fc
  .string({
    unit: fc.constantFrom(..."ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("")),
    minLength: 2,
    maxLength: 2,
  })
  .filter((code) => !(ISO_3166_ALPHA2 as ReadonlyArray<string>).includes(code));
const partyCode = alnum(3);
const instanceValue: Readonly<Record<ContractIdStandard, fc.Arbitrary<string>>> = {
  ISO: alnum(9),
  EMI3: alnum(8).map((tail) => `C${tail}`),
  DIN: alnum(6),
};

// Randomly lowercases characters: every parser must normalize to uppercase.
const mixedCase = (value: string): fc.Arbitrary<string> =>
  fc.array(fc.boolean(), { minLength: value.length, maxLength: value.length }).map((flags) =>
    value
      .split("")
      .map((c, i) => (flags[i] ? c.toLowerCase() : c))
      .join(""),
  );

// Replaces one character by a different one from the same alphabet.
const substituteOne = (value: string, alphabet: readonly string[]): fc.Arbitrary<string> =>
  fc
    .tuple(fc.nat({ max: value.length - 1 }), fc.constantFrom(...alphabet))
    .filter(([index, replacement]) => value[index] !== replacement)
    .map(([index, replacement]) => value.slice(0, index) + replacement + value.slice(index + 1));

describe("check digit properties", () => {
  it("ISO: one alphanumeric character, case-insensitive, and every single substitution is detected", () => {
    fc.assert(
      fc.property(
        alnum(14).chain((code) =>
          fc.tuple(fc.constant(code), mixedCase(code), substituteOne(code, ALNUM)),
        ),
        ([code, lowered, corrupted]) => {
          const digit = checkDigitIso(code);
          expect(digit).toMatch(/^[A-Z0-9]$/);
          expect(checkDigitIso(lowered)).toBe(digit);
          expect(checkDigitIso(corrupted)).not.toBe(digit);
        },
      ),
      { numRuns: 1000 },
    );
  });

  it("DIN: a digit or X, case-insensitive, and every single digit substitution is detected", () => {
    const payload = fc.tuple(countryCode, partyCode, digits(6, 6)).map((parts) => parts.join(""));
    fc.assert(
      fc.property(
        payload.chain((code) =>
          fc.tuple(fc.constant(code), mixedCase(code), substituteOne(code, DIGITS)),
        ),
        ([code, lowered, corrupted]) => {
          const digit = checkDigitDin(code);
          expect(digit).toMatch(/^[0-9X]$/);
          expect(checkDigitDin(lowered)).toBe(digit);
          // Only the last six characters are digits; the substitution may land on a letter, which
          // the weighted sum does not always detect (a letter counts as two digits).
          if (corrupted.slice(0, 5) === code.slice(0, 5)) {
            expect(checkDigitDin(corrupted)).not.toBe(digit);
          }
        },
      ),
      { numRuns: 1000 },
    );
  });
});

describe("parser round trips", () => {
  it("ContractId: canonical and compact renderings parse back to an equal value, in any case", () => {
    for (const standard of Object.values(ContractIdStandards)) {
      fc.assert(
        fc.property(
          fc
            .tuple(countryCode, partyCode, instanceValue[standard])
            .chain(([cc, provider, instance]) => {
              const id = ContractId.fromParts(standard, cc, provider, instance);
              return fc.tuple(
                fc.constant(id),
                mixedCase(id.toString()),
                mixedCase(id.toCompactString()),
              );
            }),
          ([id, canonical, compact]) => {
            expect(ContractId.parseStrict(standard, canonical)).toStrictEqual(id);
            expect(ContractId.parseStrict(standard, compact)).toStrictEqual(id);
            expect(
              ContractId.parseStrict(standard, id.toCompactStringWithoutCheckDigit()),
            ).toStrictEqual(id);
            expect(id.checkDigit).toBe(
              standard === ContractIdStandards.DIN
                ? checkDigitDin(id.toCompactStringWithoutCheckDigit())
                : checkDigitIso(id.toCompactStringWithoutCheckDigit()),
            );
          },
        ),
      );
    }
  });

  it("ContractId: DIN <-> EMI3 <-> ISO conversions are inverses", () => {
    fc.assert(
      fc.property(
        fc.tuple(countryCode, partyCode, instanceValue.DIN),
        ([cc, provider, instance]) => {
          const din = ContractId.fromParts(ContractIdStandards.DIN, cc, provider, instance);
          const emi3 = din.convertTo(ContractIdStandards.EMI3);
          const iso = din.convertTo(ContractIdStandards.ISO);
          expect(emi3.convertTo(ContractIdStandards.DIN)).toStrictEqual(din);
          expect(iso.convertTo(ContractIdStandards.DIN)).toStrictEqual(din);
          // ISO <-> EMI3 copy the instance value, so the EMI3 form survives a trip through ISO.
          expect(
            emi3.convertTo(ContractIdStandards.ISO).convertTo(ContractIdStandards.EMI3),
          ).toStrictEqual(emi3);
        },
      ),
    );
  });

  it("EvseIdIso: the canonical rendering parses back, and EvseId picks the ISO format", () => {
    const outlet = fc.string({ unit: fc.constantFrom(...ALNUM, "*"), minLength: 1, maxLength: 31 });
    fc.assert(
      fc.property(fc.tuple(countryCode, partyCode, outlet), ([cc, operator, powerOutlet]) => {
        const id = EvseIdIso.fromParts(cc, operator, powerOutlet);
        expect(EvseIdIso.parseStrict(id.toString())).toStrictEqual(id);
        expect(EvseId.parseStrict(id.toString())).toStrictEqual(id);
        // Country and operator are case-insensitive; the `E` type marker is not (as in Scala).
        expect(
          EvseIdIso.parseStrict(
            `${cc.toLowerCase()}*${operator.toLowerCase()}*E${id.powerOutletId}`,
          ),
        ).toStrictEqual(id);
      }),
    );
  });

  it("EvseIdDin: the canonical rendering parses back, with or without the leading +", () => {
    const outlet = fc.string({
      unit: fc.constantFrom(...DIGITS, "*"),
      minLength: 1,
      maxLength: 32,
    });
    fc.assert(
      fc.property(fc.tuple(digits(1, 3), digits(3, 6), outlet), ([cc, operator, powerOutlet]) => {
        const id = EvseIdDin.fromParts(cc, operator, powerOutlet);
        expect(EvseIdDin.parseStrict(id.toString())).toStrictEqual(id);
        expect(EvseId.parseStrict(id.toString())).toStrictEqual(id);
        expect(EvseIdDin.parseStrict(id.toString().slice(1))).toStrictEqual(id);
      }),
    );
  });

  it("PartyId: dash, star and compact renderings parse back to an equal value", () => {
    fc.assert(
      fc.property(fc.tuple(countryCode, partyCode), ([cc, party]) => {
        const id = PartyId.parseStrict(`${cc}-${party}`);
        expect(PartyId.parseStrict(`${cc}*${party}`)).toStrictEqual(id);
        expect(PartyId.parseStrict(id.toCompactString())).toStrictEqual(id);
        expect(PartyId.parseStrict(id.toString().toLowerCase())).toStrictEqual(id);
      }),
    );
  });

  it("CountryCode: every table entry parses in any case, every other two-letter string is rejected", () => {
    fc.assert(
      fc.property(countryCode.chain(mixedCase), (raw) => {
        expect(CountryCode.parse(raw)).toBe(raw.toUpperCase());
      }),
    );
    fc.assert(
      fc.property(unassignedCountryCode.chain(mixedCase), (raw) => {
        expect(CountryCode.parse(raw)).toBeNull();
      }),
    );
  });
});

describe("parse, parseStrict and tryParse agree on any input", () => {
  // Arbitrary strings plus near-misses of real identifiers, so both branches get exercised.
  const inputs = fc.oneof(
    fc.string(),
    fc.string({ unit: fc.constantFrom(...ALNUM, "*", "-", "+", " ", "é") }),
    fc.constantFrom(
      "NL-TNM-000122045-U",
      "NL-TNM-000122045-X",
      "NL*TNM*122045*0",
      "DE*AB7*E840*6487",
      "+49*810*000*438",
      "NL-TNM",
      "+31",
      "TNM",
      "810",
    ),
  );

  const parsers: ReadonlyArray<
    readonly [
      string,
      (raw: string) => unknown,
      (raw: string) => unknown,
      (raw: string) => ParseResult<unknown>,
    ]
  > = [
    [
      "CountryCode",
      (raw) => CountryCode.parse(raw),
      (raw) => CountryCode.from(raw),
      (raw) => CountryCode.tryParse(raw),
    ],
    [
      "PhoneCountryCode",
      (raw) => PhoneCountryCode.parse(raw),
      (raw) => PhoneCountryCode.from(raw),
      (raw) => PhoneCountryCode.tryParse(raw),
    ],
    [
      "ProviderId",
      (raw) => ProviderId.parse(raw),
      (raw) => ProviderId.from(raw),
      (raw) => ProviderId.tryParse(raw),
    ],
    [
      "OperatorIdIso",
      (raw) => OperatorIdIso.parse(raw),
      (raw) => OperatorIdIso.from(raw),
      (raw) => OperatorIdIso.tryParse(raw),
    ],
    [
      "OperatorIdDin",
      (raw) => OperatorIdDin.parse(raw),
      (raw) => OperatorIdDin.from(raw),
      (raw) => OperatorIdDin.tryParse(raw),
    ],
    [
      "PartyId",
      (raw) => PartyId.parse(raw),
      (raw) => PartyId.parseStrict(raw),
      (raw) => PartyId.tryParse(raw),
    ],
    [
      "EvseIdIso",
      (raw) => EvseIdIso.parse(raw),
      (raw) => EvseIdIso.parseStrict(raw),
      (raw) => EvseIdIso.tryParse(raw),
    ],
    [
      "EvseIdDin",
      (raw) => EvseIdDin.parse(raw),
      (raw) => EvseIdDin.parseStrict(raw),
      (raw) => EvseIdDin.tryParse(raw),
    ],
    [
      "EvseId",
      (raw) => EvseId.parse(raw),
      (raw) => EvseId.parseStrict(raw),
      (raw) => EvseId.tryParse(raw),
    ],
    ...Object.values(ContractIdStandards).map(
      (standard) =>
        [
          `ContractId(${standard})`,
          (raw: string) => ContractId.parse(standard, raw),
          (raw: string) => ContractId.parseStrict(standard, raw),
          (raw: string) => ContractId.tryParse(standard, raw),
        ] as const,
    ),
  ];

  for (const [name, parse, strict, tryParse] of parsers) {
    it(name, () => {
      fc.assert(
        fc.property(inputs, (raw) => {
          const result = tryParse(raw);
          if (result.ok) {
            expect(parse(raw)).toStrictEqual(result.value);
            expect(strict(raw)).toStrictEqual(result.value);
            expect(String(result.value)).not.toBe("");
          } else {
            expect(parse(raw)).toBeNull();
            expect(() => strict(raw)).toThrow(new ValidationError(result.error));
          }
        }),
      );
    });
  }
});
