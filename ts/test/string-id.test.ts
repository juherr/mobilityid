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

import { describe, expect, expectTypeOf, it } from "vite-plus/test";
import {
  CountryCode,
  OperatorIdDin,
  OperatorIdIso,
  PhoneCountryCode,
  ProviderId,
} from "../src/index.js";
import type { StringIdCompanion } from "../src/string-id.js";

// `StringIdCompanion<T>` only uses `T` in return positions, so every companion widens to the
// string one and the table exercises the four entry points `defineStringId` builds.
const ids: ReadonlyArray<{
  name: string;
  id: StringIdCompanion<string>;
  raw: string;
  expected: string;
}> = [
  { name: "CountryCode", id: CountryCode, raw: "nl", expected: "NL" },
  { name: "PhoneCountryCode", id: PhoneCountryCode, raw: "+31", expected: "+31" },
  { name: "ProviderId", id: ProviderId, raw: "tnm", expected: "TNM" },
  { name: "OperatorIdIso", id: OperatorIdIso, raw: "ab7", expected: "AB7" },
  { name: "OperatorIdDin", id: OperatorIdDin, raw: "810", expected: "810" },
];

describe("branded string identifiers", () => {
  it.each(ids)("$name is the normalized string itself", ({ id, raw, expected }) => {
    const value = id.from(raw);
    expect(typeof value).toBe("string");
    expect(value).toBe(expected);
    expect(`${value}`).toBe(expected);
    expect(JSON.stringify({ value })).toBe(`{"value":"${expected}"}`);
  });

  it.each(ids)("$name entry points agree on the same string", ({ id, raw, expected }) => {
    expect(id.isValid(raw)).toBe(true);
    expect(id.parse(raw)).toBe(expected);
    expect(id.tryParse(raw)).toStrictEqual({ ok: true, value: expected });
  });

  it.each(ids)("$name is idempotent on an already branded value", ({ id, raw }) => {
    const value = id.from(raw);
    expect(id.from(value)).toBe(value);
    expect(id.isValid(value)).toBe(true);
  });

  it("is usable as a Map key and interchangeable with an equal id", () => {
    const seen = new Map<CountryCode, number>([[CountryCode.from("NL"), 1]]);
    expect(seen.get(CountryCode.from("nl"))).toBe(1);
    expect(new Set([ProviderId.from("TNM"), ProviderId.from("tnm")]).size).toBe(1);
  });

  it("carries a brand distinct from string and from the other identifiers", () => {
    expectTypeOf<CountryCode>().toExtend<string>();
    expectTypeOf<string>().not.toExtend<CountryCode>();
    expectTypeOf<CountryCode>().not.toExtend<ProviderId>();
    expectTypeOf<ProviderId>().not.toExtend<OperatorIdIso>();
  });
});
