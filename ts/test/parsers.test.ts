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

import { MobilityIdParsers } from "../src/index.js";

describe("MobilityIdParsers", () => {
  it("exposes parsing helpers", () => {
    expect(MobilityIdParsers.parseCountryCode("NL")?.toString()).toBe("NL");
    expect(MobilityIdParsers.parseProviderId("TNM")?.toString()).toBe("TNM");
    expect(MobilityIdParsers.parseContractIdIso("NL-TNM-000122045-U")?.toString()).toBe(
      "NL-TNM-000122045-U",
    );
    expect(MobilityIdParsers.parseEvseId("DE*AB7*E840*6487")?.toString()).toBe("DE*AB7*E840*6487");
  });

  it("returns null on invalid values", () => {
    expect(MobilityIdParsers.parseCountryCode("ZZ")).toBeNull();
    expect(MobilityIdParsers.parseProviderId("T|M")).toBeNull();
    expect(MobilityIdParsers.parseContractIdIso("NL-TNM-000122045-X")).toBeNull();
    expect(MobilityIdParsers.parseEvseId("NL*TNM*840*6487")).toBeNull();
  });
});
