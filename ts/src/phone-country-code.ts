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

const PHONE_COUNTRY_CODE_REGEX = /^\+?[0-9]{1,3}$/;

export class PhoneCountryCode {
  public readonly value: string;

  private constructor(value: string) {
    this.value = value;
    Object.freeze(this);
  }

  public static isValid(raw: string): boolean {
    return PHONE_COUNTRY_CODE_REGEX.test(raw);
  }

  public static from(raw: string): PhoneCountryCode {
    if (!PhoneCountryCode.isValid(raw)) {
      throw new TypeError(
        `phone Country Code must start with a '+' sign and be followed by 1-3 digits. (Was: ${raw})`,
      );
    }

    return new PhoneCountryCode(raw);
  }

  public static parse(raw: string): PhoneCountryCode | null {
    try {
      return PhoneCountryCode.from(raw);
    } catch {
      return null;
    }
  }

  public toString(): string {
    return this.value;
  }
}
