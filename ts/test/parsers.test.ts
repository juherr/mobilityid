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

import { MobilityIdParsers } from "../src/index.js";

describe("MobilityIdParsers", () => {
  it("exposes parsing helpers", () => {
    expect(MobilityIdParsers.parseCountryCode("NL")).toBe("NL");
    expect(MobilityIdParsers.parseProviderId("TNM")).toBe("TNM");
    expect(MobilityIdParsers.parseContractIdIso("NL-TNM-000122045-U")?.toString()).toBe(
      "NL-TNM-000122045-U",
    );
    expect(MobilityIdParsers.parseEvseId("DE*AB7*E840*6487")?.toString()).toBe("DE*AB7*E840*6487");
  });

  it("covers every identifier", () => {
    expect(MobilityIdParsers.parsePhoneCountryCode("+31")).toBe("+31");
    expect(MobilityIdParsers.parseOperatorIdIso("TNM")).toBe("TNM");
    expect(MobilityIdParsers.parseOperatorIdDin("456")).toBe("456");
    expect(MobilityIdParsers.parsePartyId("NL*TNM")?.toString()).toBe("NL-TNM");
    expect(MobilityIdParsers.parseContractIdDin("NL-TNM-722345-8")?.toString()).toBe(
      "NL-TNM-722345-8",
    );
    expect(MobilityIdParsers.parseContractIdEmi3("NL-TNM-C00722345-N")?.toString()).toBe(
      "NL-TNM-C00722345-N",
    );
    expect(MobilityIdParsers.parseEvseIdIso("NL*TNM*E840*6487")?.toString()).toBe(
      "NL*TNM*E840*6487",
    );
    expect(MobilityIdParsers.parseEvseIdDin("+49*810*000*438")?.toString()).toBe("+49*810*000*438");
  });

  it("returns null on invalid values", () => {
    expect(MobilityIdParsers.parseCountryCode("ZZ")).toBeNull();
    expect(MobilityIdParsers.parseProviderId("T|M")).toBeNull();
    expect(MobilityIdParsers.parseContractIdIso("NL-TNM-000122045-X")).toBeNull();
    expect(MobilityIdParsers.parseEvseId("NL*TNM*840*6487")).toBeNull();
  });
});
