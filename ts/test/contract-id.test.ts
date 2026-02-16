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

import { ContractId, ContractIdStandards } from "../src/index.js";

describe("ContractId", () => {
  it("accepts all canonical Scala parsing variants", () => {
    const iso = [
      "NL-TNM-000122045-U",
      "NL-TNM-000122045-U",
      "Nl-TnM-000122045-U",
      "nl-TNm-000122045-u",
      "NLTNM000122045",
    ];

    const emi3 = [
      "NL-TNM-C00122045-K",
      "NL-TNM-C00122045-K",
      "Nl-TnM-C00122045-K",
      "nl-TNm-C00122045-k",
      "NLTNMC00122045",
    ];

    const din = [
      "NL-TNM-122045-0",
      "NL-TNM-122045-0",
      "Nl-TnM-122045-0",
      "nl-TNm-122045-0",
      "NL*TNM*122045*0",
      "NLTNM122045",
    ];

    for (const raw of iso) {
      expect(ContractId.parseStrict(ContractIdStandards.ISO, raw).toString()).toBe(
        "NL-TNM-000122045-U",
      );
    }
    for (const raw of emi3) {
      expect(ContractId.parseStrict(ContractIdStandards.EMI3, raw).toString()).toBe(
        "NL-TNM-C00122045-K",
      );
    }
    for (const raw of din) {
      expect(ContractId.parseStrict(ContractIdStandards.DIN, raw).toString()).toBe(
        "NL-TNM-122045-0",
      );
    }
  });

  it("parses ISO, EMI3 and DIN formats", () => {
    expect(ContractId.parse(ContractIdStandards.ISO, "NL-TNM-000122045-U")?.toString()).toBe(
      "NL-TNM-000122045-U",
    );
    expect(ContractId.parse(ContractIdStandards.EMI3, "NL-TNM-C00122045-K")?.toString()).toBe(
      "NL-TNM-C00122045-K",
    );
    expect(ContractId.parse(ContractIdStandards.DIN, "NL*TNM*122045*0")?.toString()).toBe(
      "NL-TNM-122045-0",
    );
  });

  it("is case insensitive", () => {
    expect(ContractId.parseStrict(ContractIdStandards.ISO, "Nl-TnM-000122045-u").toString()).toBe(
      "NL-TNM-000122045-U",
    );
  });

  it("rejects invalid values", () => {
    expect(() => ContractId.parseStrict(ContractIdStandards.ISO, "NL-TNM-000122045-X")).toThrow(
      TypeError,
    );
    expect(() => ContractId.parseStrict(ContractIdStandards.ISO, "NLTNM076")).toThrow(TypeError);
  });

  it("rejects invalid field lengths and characters when creating from parts", () => {
    expect(() => ContractId.fromParts(ContractIdStandards.ISO, "A", "TNM", "000122045")).toThrow(
      TypeError,
    );
    expect(() => ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNMN", "000722345")).toThrow(
      TypeError,
    );
    expect(() => ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "72245")).toThrow(
      TypeError,
    );
    expect(() => ContractId.fromParts(ContractIdStandards.ISO, "NL", "T|M", "000122045")).toThrow(
      TypeError,
    );
    expect(() => ContractId.fromParts(ContractIdStandards.DIN, "NL", "TNM", "000122045")).toThrow(
      TypeError,
    );
  });

  it("rejects ISO to DIN conversion for non-convertible value", () => {
    const iso = ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "012345678");
    expect(() => iso.convertTo(ContractIdStandards.DIN)).toThrow(TypeError);
  });

  it("renders compact forms", () => {
    const contract = ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "000722345");
    expect(contract.toString()).toBe("NL-TNM-000722345-X");
    expect(contract.toCompactString()).toBe("NLTNM000722345X");
    expect(contract.toCompactStringWithoutCheckDigit()).toBe("NLTNM000722345");
  });

  it("converts between standards", () => {
    const iso = ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "000122045");
    expect(iso.convertTo(ContractIdStandards.DIN).toString()).toBe("NL-TNM-012204-5");

    const din = ContractId.parseStrict(ContractIdStandards.DIN, "NL-TNM-012204-5");
    expect(din.convertTo(ContractIdStandards.ISO).toString()).toBe("NL-TNM-000122045-U");
    expect(din.convertTo(ContractIdStandards.EMI3).toString()).toBe("NL-TNM-C00122045-K");

    const emi3 = ContractId.parseStrict(ContractIdStandards.EMI3, "NL-TNM-C00122045-K");
    expect(emi3.convertTo(ContractIdStandards.DIN).toString()).toBe("NL-TNM-012204-5");
    expect(emi3.convertTo(ContractIdStandards.ISO).toString()).toBe("NL-TNM-C00122045-K");
  });

  it("exposes provider party id", () => {
    const contract = ContractId.fromParts(ContractIdStandards.ISO, "NL", "TNM", "000722345");
    expect(contract.partyId.toCompactString()).toBe("NLTNM");
  });
});
