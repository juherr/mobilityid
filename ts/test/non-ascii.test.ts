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

import { describe, expect, it } from "vite-plus/test";

import {
  checkDigitDin,
  checkDigitIso,
  ContractId,
  ContractIdStandards,
  EvseId,
  EvseIdDin,
  EvseIdIso,
  ValidationError,
} from "../src/index.js";

// `toUpperCase` maps some non-ASCII letters to ASCII ones ('ſ' U+017F to 'S', 'ı' U+0131 to
// 'I'). Only ASCII letters are folded, so these inputs are rejected like in every other port; the
// vectors are shared with them.
describe("non-ASCII letters that uppercase to ASCII", () => {
  it("are rejected by every factory and parser", () => {
    for (const strict of [
      () => EvseIdIso.fromParts("FR", "55C", "84400*ſVG*1"),
      () => EvseIdIso.fromParts("ſe", "55C", "1"),
      () => EvseIdIso.fromParts("NL", "ıBM", "1"),
      () => EvseId.fromParts("FR", "55C", "84400*ſVG*1"),
      () => EvseId.fromParts("ſe", "55C", "1"),
      () => ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "00012204ı"),
      () => ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "000000899", "ı"),
      () => checkDigitIso("NLſVG000122045"),
      () => checkDigitDin("NLſVG012204"),
    ]) {
      expect(strict).toThrow(ValidationError);
    }
  });
});

describe("EVSE ids built from parts", () => {
  it("validate the power outlet id like the parsers do", () => {
    expect(() => EvseIdIso.fromParts("NL", "TNM", "84é")).toThrow(
      new ValidationError("Invalid powerOutletId for ISO format", "power-outlet-id"),
    );
    expect(() => EvseIdIso.fromParts("NL", "TNM", "1".repeat(32))).toThrow(ValidationError);
    expect(() => EvseIdDin.fromParts("+49", "810", "abc")).toThrow(
      new ValidationError("Invalid powerOutletId for DIN format", "power-outlet-id"),
    );
    expect(() => EvseIdDin.fromParts("+49", "810", "1é")).toThrow(ValidationError);
    expect(EvseIdIso.fromParts("nl", "tnm", "e840*6487").toString()).toBe("NL*TNM*EE840*6487");
    expect(EvseIdDin.fromParts("49", "810", "000*438").toString()).toBe("+49*810*000*438");
  });
});
