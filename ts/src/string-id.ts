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

import { type ParseResult, ValidationError, attempt, valueOrNull } from "./parse-result.js";

declare const brand: unique symbol;

/**
 * Branded string: a `StringId<"CountryCode">` is a plain string at runtime that only the
 * `CountryCode` factory can produce at the type level. The symbol is never assigned; it exists
 * only in the type system, so a branded value costs nothing over the underlying string.
 */
export type StringId<B extends string> = string & { readonly [brand]: B };

/** Validation rule of a string identifier: the predicate and the `ValidationError` message. */
export type StringIdSpec = Readonly<{
  isValid: (raw: string) => boolean;
  message: (raw: string) => string;
}>;

/**
 * The three parsing entry points of a single-valued identifier plus its predicate. `from` is
 * strict and throws `ValidationError`; `tryParse` and `parse` are derived from it.
 */
export type StringIdCompanion<T extends string> = Readonly<{
  isValid(raw: string): boolean;
  from(raw: string): T;
  tryParse(raw: string): ParseResult<T>;
  parse(raw: string): T | null;
}>;

/**
 * Builds the companion of a branded string identifier. Valid input is normalized to uppercase
 * (a no-op for the digit-only identifiers); `isValid` stays a boolean rather than a type guard
 * because it accepts the non-normalized input that `from` still has to uppercase.
 */
export function defineStringId<T extends string>(spec: StringIdSpec): StringIdCompanion<T> {
  const from = (raw: string): T => {
    if (!spec.isValid(raw)) {
      throw new ValidationError(spec.message(raw));
    }

    return raw.toUpperCase() as T;
  };
  const tryParse = (raw: string): ParseResult<T> => attempt(() => from(raw));
  const parse = (raw: string): T | null => valueOrNull(tryParse(raw));

  return Object.freeze({ isValid: spec.isValid, from, tryParse, parse });
}
