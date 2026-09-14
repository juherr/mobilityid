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

import { isValidPartyCode } from "./provider-id.js";

const OPERATOR_DIN_REGEX = /^[0-9]{3,6}$/;

export class OperatorIdIso {
  public readonly value: string;

  private constructor(value: string) {
    this.value = value;
    Object.freeze(this);
  }

  public static isValid(raw: string): boolean {
    return isValidPartyCode(raw);
  }

  public static from(raw: string): OperatorIdIso {
    if (!OperatorIdIso.isValid(raw)) {
      throw new TypeError("OperatorId must have a length of 3 and be ASCII letters or digits");
    }

    return new OperatorIdIso(raw.toUpperCase());
  }

  public static parse(raw: string): OperatorIdIso | null {
    try {
      return OperatorIdIso.from(raw);
    } catch {
      return null;
    }
  }

  public toString(): string {
    return this.value;
  }
}

export class OperatorIdDin {
  public readonly value: string;

  private constructor(value: string) {
    this.value = value;
    Object.freeze(this);
  }

  public static isValid(raw: string): boolean {
    return OPERATOR_DIN_REGEX.test(raw);
  }

  public static from(raw: string): OperatorIdDin {
    if (!OperatorIdDin.isValid(raw)) {
      throw new TypeError("OperatorId must have a length of 3-6 chars and be digits");
    }

    return new OperatorIdDin(raw.toUpperCase());
  }

  public static parse(raw: string): OperatorIdDin | null {
    try {
      return OperatorIdDin.from(raw);
    } catch {
      return null;
    }
  }

  public toString(): string {
    return this.value;
  }
}

export type OperatorId = OperatorIdIso | OperatorIdDin;
