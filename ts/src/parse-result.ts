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

/**
 * Thrown by the strict factories and parsers when the input is not a valid identifier. It extends
 * `TypeError` for compatibility with callers that catch `TypeError`; `attempt` captures exactly
 * this class so that a genuine programming `TypeError` (`undefined is not a function`) is never
 * mistaken for an invalid input.
 */
export class ValidationError extends TypeError {
  public constructor(message: string) {
    super(message);
    this.name = "ValidationError";
  }
}

/**
 * Outcome of a `tryParse` call: the parsed value, or the reason the strict parser would have
 * thrown. Narrow on `ok`; both variants are frozen like the domain objects.
 */
export type ParseResult<T> =
  | Readonly<{ ok: true; value: T }>
  | Readonly<{ ok: false; error: string }>;

export function success<T>(value: T): ParseResult<T> {
  return Object.freeze({ ok: true, value });
}

export function failure<T>(error: string): ParseResult<T> {
  return Object.freeze({ ok: false, error });
}

/**
 * Runs a strict parser and captures its `ValidationError` as a failure. Any other error, a plain
 * `TypeError` included, is a bug rather than an invalid input and keeps propagating.
 */
export function attempt<T>(parse: () => T): ParseResult<T> {
  try {
    return success(parse());
  } catch (error) {
    if (error instanceof ValidationError) {
      return failure(error.message);
    }
    throw error;
  }
}
