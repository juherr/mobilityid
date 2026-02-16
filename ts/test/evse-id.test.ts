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

import { EvseId, EvseIdDin, EvseIdIso } from "../src/index.js";

describe("EvseId", () => {
  it("parses ISO input", () => {
    expect(EvseId.parse("DE*AB7*E840*6487")?.toString()).toBe("DE*AB7*E840*6487");
    expect(EvseId.parse("DEAB7E8406487")?.toString()).toBe("DE*AB7*E8406487");
    expect(EvseId.parse("DE*DES*E*BMW*0113*2")?.toString()).toBe("DE*DES*E*BMW*0113*2");
  });

  it("parses DIN input", () => {
    expect(EvseId.parse("+49*810*000*438")?.toString()).toBe("+49*810*000*438");
    expect(EvseId.parse("49*810*000*438")?.toString()).toBe("+49*810*000*438");
  });

  it("rejects invalid values", () => {
    expect(EvseId.parse("+49*AB7*840*6487")).toBeNull();
    expect(EvseId.parse("NL*TNM*840*6487")).toBeNull();
    expect(EvseId.parse("ZZ*TNM*E840*64878")).toBeNull();
  });

  it("enforces EVSE size boundaries", () => {
    expect(EvseId.parse("DEAB7E1")).not.toBeNull();
    expect(EvseId.parse("DE*AB7*E1234567890ABCDEFGHIJ1234567890")).not.toBeNull();
    expect(EvseId.parse("DE*AB7*E1234567890ABCDEFGHIJ1234567890AB")).toBeNull();

    expect(EvseId.parse("+49*810*1")).not.toBeNull();
    expect(EvseId.parse("+49*810*12345678901234567890123456789012")).not.toBeNull();
    expect(EvseId.parse("+49*810*123456789012345678901234567890123")).toBeNull();
  });

  it("rejects mixed ISO and DIN combinations", () => {
    expect(() => EvseId.fromParts("+31", "ABC", "840*6487")).toThrow(TypeError);
    expect(() => EvseId.fromParts("+31", "745", "E840*6487")).toThrow(TypeError);
    expect(() => EvseId.fromParts("+31", "745", "840*6487E")).toThrow(TypeError);
  });

  it("is case insensitive", () => {
    expect(EvseId.fromParts("Nl", "tnM", "E000122045").toString()).toBe(
      EvseId.fromParts("NL", "TNM", "E000122045").toString(),
    );
  });

  it("supports strict creation by parts", () => {
    expect(EvseId.fromParts("NL", "TNM", "840*6487").toString()).toBe("NL*TNM*E840*6487");
    expect(EvseId.fromParts("+31", "745", "840*6487").toString()).toBe("+31*745*840*6487");
    expect(() => EvseId.fromParts("+31", "ABC", "840*6487")).toThrow(TypeError);
  });

  it("supports ISO helper behavior", () => {
    const iso = EvseIdIso.parseStrict("NL*TNM*E840*6487");
    expect(iso.toCompactString()).toBe("NLTNME8406487");
    expect(iso.partyId.toCompactString()).toBe("NLTNM");
  });

  it("rejects parsing a DIN string as ISO and an ISO string as DIN", () => {
    expect(EvseIdIso.parse("+49*810*000*438")).toBeNull();
    expect(EvseIdDin.parse("DE*AB7*E840*6487")).toBeNull();
  });

  it("exposes DIN constructor", () => {
    expect(EvseIdDin.fromParts("31", "745", "840*6487").toString()).toBe("+31*745*840*6487");
  });
});
