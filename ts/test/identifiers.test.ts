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

import { OperatorIdDin, OperatorIdIso, PartyId } from "../src/index.js";

describe("PartyId", () => {
  it("parses with dash, star, or compact format", () => {
    expect(PartyId.parse("NL-TNM")?.toCompactString()).toBe("NLTNM");
    expect(PartyId.parse("NL*TNM")?.toCompactString()).toBe("NLTNM");
    expect(PartyId.parse("NLTNM")?.toCompactString()).toBe("NLTNM");
  });

  it("renders with dash", () => {
    expect(PartyId.parse("NL*TNM")?.toString()).toBe("NL-TNM");
  });

  it("rejects nonsense values", () => {
    const nonsense = [
      "NLTNMA",
      "XYTNM",
      "NL%(@$",
      " NLTNM",
      "\nLTNM",
      "",
      "XY-TNMaargh",
      "НЛ-TNM",
      "NLT-NM",
    ];
    for (const value of nonsense) {
      expect(PartyId.parse(value)).toBeNull();
    }
  });
});

describe("Operator IDs", () => {
  it("validates DIN operator IDs", () => {
    expect(() => OperatorIdDin.from("12")).toThrow(TypeError);
    expect(() => OperatorIdDin.from("12A")).toThrow(TypeError);
    expect(() => OperatorIdDin.from("1234567")).toThrow(TypeError);
    expect(() => OperatorIdDin.from("12345")).not.toThrow();
  });

  it("validates ISO operator IDs", () => {
    expect(() => OperatorIdIso.from("AB")).toThrow(TypeError);
    expect(() => OperatorIdIso.from("ABCD")).toThrow(TypeError);
    expect(() => OperatorIdIso.from("AB2")).not.toThrow();
  });
});
