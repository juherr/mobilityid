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

const PARTY_CODE_REGEX = /^[A-Za-z0-9]{3}$/;

export function isValidPartyCode(value: string): boolean {
  return PARTY_CODE_REGEX.test(value);
}

export class ProviderId {
  public readonly value: string;

  private constructor(value: string) {
    this.value = value;
    Object.freeze(this);
  }

  public static isValid(raw: string): boolean {
    return isValidPartyCode(raw);
  }

  public static from(raw: string): ProviderId {
    if (!ProviderId.isValid(raw)) {
      throw new TypeError("OperatorId must have a length of 3 and be ASCII letters or digits");
    }

    return new ProviderId(raw.toUpperCase());
  }

  public static parse(raw: string): ProviderId | null {
    try {
      return ProviderId.from(raw);
    } catch {
      return null;
    }
  }

  public toString(): string {
    return this.value;
  }
}
