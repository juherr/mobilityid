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

import { readFileSync } from "node:fs";
import { describe, expect, expectTypeOf, it } from "vite-plus/test";
import { CountryCode, ISO_3166_ALPHA2, ValidationError } from "../src/index.js";

// Locale.getISOCountries() lists 249 codes; bump with the JDK when a code is assigned or withdrawn.
const EXPECTED_COUNT = 249;

describe("CountryCode", () => {
  it.each(["NL", "GB", "AX", "SS"])("accepts the ISO 3166-1 alpha-2 code %s", (raw) => {
    expect(CountryCode.isValid(raw)).toBe(true);
    expect(CountryCode.from(raw)).toBe(raw);
    expect(CountryCode.parse(raw)).toBe(raw);
    expect(CountryCode.tryParse(raw)).toStrictEqual({ ok: true, value: raw });
  });

  it("normalizes to uppercase", () => {
    expect(CountryCode.from("nl")).toBe("NL");
    expect(CountryCode.isValid("nl")).toBe(true);
  });

  // CLDR regions Intl.DisplayNames knows but ISO 3166-1 does not assign: the other ports reject
  // them, so must this one.
  it.each(["EU", "UK", "XK", "SU", "ZZ", "XX", "N", "NLD", "", "N1"])("rejects %j", (raw) => {
    expect(CountryCode.isValid(raw)).toBe(false);
    expect(CountryCode.parse(raw)).toBeNull();
    expect(CountryCode.tryParse(raw).ok).toBe(false);
    expect(() => CountryCode.from(raw)).toThrow(ValidationError);
    expect(() => CountryCode.from(raw)).toThrow(
      "Country Code must be valid according to ISO 3166-1 alpha-2",
    );
  });

  it("accepts known literals at the type level and nothing else", () => {
    const cc: CountryCode = "NL";
    expect(cc).toBe("NL");
    expectTypeOf<"NL">().toExtend<CountryCode>();
    expectTypeOf<"nl">().not.toExtend<CountryCode>();
    expectTypeOf<"EU">().not.toExtend<CountryCode>();
    expectTypeOf<string>().not.toExtend<CountryCode>();
    expectTypeOf<CountryCode>().toExtend<string>();
  });
});

describe("ISO_3166_ALPHA2", () => {
  it("is a well-formed table", () => {
    expect(ISO_3166_ALPHA2).toHaveLength(EXPECTED_COUNT);
    expect(new Set(ISO_3166_ALPHA2).size).toBe(EXPECTED_COUNT);
    for (const code of ISO_3166_ALPHA2) {
      expect(code).toMatch(/^[A-Z]{2}$/);
    }
    expect([...ISO_3166_ALPHA2]).toStrictEqual([...ISO_3166_ALPHA2].sort());
  });

  // Same JDK source as go/scripts/generate-iso3166.sh; the two generated tables must not drift.
  it("matches the Go port's generated table", () => {
    const goTable = readFileSync(
      new URL("../../go/mobilityid/iso3166_alpha2.go", import.meta.url),
      "utf8",
    );
    const goCodes = [...goTable.matchAll(/^\t"([A-Z]{2})": \{\},$/gm)].map((m) => m[1]);
    expect(goCodes).toStrictEqual([...ISO_3166_ALPHA2]);
  });
});
