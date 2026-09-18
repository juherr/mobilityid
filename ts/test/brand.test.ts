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

import { describe, expect, expectTypeOf, it } from "vitest";
import {
  CountryCode,
  OperatorIdDin,
  OperatorIdIso,
  PhoneCountryCode,
  ProviderId,
} from "../src/index.js";

const ids = [
  {
    name: "CountryCode",
    from: (raw: string): string => CountryCode.from(raw),
    raw: "nl",
    expected: "NL",
  },
  {
    name: "PhoneCountryCode",
    from: (raw: string): string => PhoneCountryCode.from(raw),
    raw: "+31",
    expected: "+31",
  },
  {
    name: "ProviderId",
    from: (raw: string): string => ProviderId.from(raw),
    raw: "tnm",
    expected: "TNM",
  },
  {
    name: "OperatorIdIso",
    from: (raw: string): string => OperatorIdIso.from(raw),
    raw: "ab7",
    expected: "AB7",
  },
  {
    name: "OperatorIdDin",
    from: (raw: string): string => OperatorIdDin.from(raw),
    raw: "810",
    expected: "810",
  },
] as const;

describe("branded string identifiers", () => {
  it.each(ids)("$name is the normalized string itself", ({ from, raw, expected }) => {
    const id = from(raw);
    expect(typeof id).toBe("string");
    expect(id).toBe(expected);
    expect(id === expected).toBe(true);
    expect(`${id}`).toBe(expected);
    expect(id.toString()).toBe(expected);
    expect(JSON.stringify({ id })).toBe(`{"id":"${expected}"}`);
  });

  it("is usable as a Map key and interchangeable with an equal id", () => {
    const seen = new Map<CountryCode, number>([[CountryCode.from("NL"), 1]]);
    expect(seen.get(CountryCode.from("nl"))).toBe(1);
    expect(new Set([ProviderId.from("TNM"), ProviderId.from("tnm")]).size).toBe(1);
  });

  it("is idempotent on an already branded value", () => {
    const cc = CountryCode.from("NL");
    expect(CountryCode.from(cc)).toBe(cc);
    expect(CountryCode.isValid(cc)).toBe(true);
  });

  it("carries a brand distinct from string and from the other identifiers", () => {
    expectTypeOf<CountryCode>().toExtend<string>();
    expectTypeOf<CountryCode>().not.toEqualTypeOf<string>();
    expectTypeOf<CountryCode>().not.toEqualTypeOf<ProviderId>();
    expectTypeOf<ProviderId>().not.toEqualTypeOf<OperatorIdIso>();
    expectTypeOf<OperatorIdIso>().not.toEqualTypeOf<OperatorIdDin>();
    expectTypeOf<CountryCode>().not.toEqualTypeOf<PhoneCountryCode>();

    // @ts-expect-error a raw string is not a CountryCode: only `CountryCode.from` produces one
    const raw: CountryCode = "NL";
    // @ts-expect-error brands are nominal: a CountryCode is not a ProviderId
    const other: ProviderId = CountryCode.from("NL");
    const widened: string = CountryCode.from("NL");
    expect([raw, other, widened]).toHaveLength(3);
  });
});
