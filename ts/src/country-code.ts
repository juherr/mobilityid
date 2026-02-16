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

const COUNTRY_CODE_REGEX = /^[A-Za-z]{2}$/;

const regionNames = new Intl.DisplayNames(["en"], { type: "region" });

export class CountryCode {
  public readonly value: string;

  private constructor(value: string) {
    this.value = value;
    Object.freeze(this);
  }

  public static isValid(raw: string): boolean {
    if (!COUNTRY_CODE_REGEX.test(raw)) {
      return false;
    }

    const normalized = raw.toUpperCase();
    const display = regionNames.of(normalized);
    return display !== undefined && display !== normalized && display !== "Unknown Region";
  }

  public static from(raw: string): CountryCode {
    if (!CountryCode.isValid(raw)) {
      throw new TypeError("Country Code must be valid according to ISO 3166-1 alpha-2");
    }

    return new CountryCode(raw.toUpperCase());
  }

  public static parse(raw: string): CountryCode | null {
    try {
      return CountryCode.from(raw);
    } catch {
      return null;
    }
  }

  public toString(): string {
    return this.value;
  }
}
