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

import { describe, expect, it } from "vitest";

import { checkDigitDin, checkDigitIso } from "../src/index.js";

describe("checkDigitIso", () => {
  it("calculates check digits", () => {
    const contractIds = [
      "NN123ABCDEFGHI",
      "FRXYZ123456789",
      "ITA1B2C3E4F5G6",
      "ESZU8WOX834H1D",
      "PT73902837ABCZ",
      "DE83DUIEN83QGZ",
      "DE83DUIEN83ZGQ",
      "DE8AA001234567",
    ];

    expect(contractIds.map((contractId) => checkDigitIso(contractId)).join("")).toBe("T24RZDM0");
  });

  it("throws on malformed input", () => {
    expect(() => checkDigitIso("")).toThrow(TypeError);
    expect(() => checkDigitIso("DE8AA0012345678")).toThrow(TypeError);
    expect(() => checkDigitIso("DE٨٣DUIEN٨٣QGZ")).toThrow(TypeError);
  });
});

describe("checkDigitDin", () => {
  it("matches known values", () => {
    const calculate = (instance: number): string =>
      checkDigitDin(`INTNM${String(instance).padStart(6, "0")}`);
    expect(calculate(71)).toBe("9");
    expect(calculate(110)).toBe("X");
    expect(calculate(124)).toBe("0");
    expect(calculate(114)).toBe("6");
    expect(calculate(191)).toBe("5");
  });
});
