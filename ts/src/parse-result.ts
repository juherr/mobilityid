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
 * Machine-readable reason of a parse failure, stable across releases: count failures or report
 * a broken record on it, show `error` to humans. A component code is given only when the failing
 * component can be told apart reliably; anything else is `format`. The codes check structure
 * and the standards' tables (ISO 3166-1), never real-world registrations.
 *
 * - `format`: the overall shape is not recognized, or the failing part cannot be told apart.
 * - `country-code`: ISO 3166-1 alpha-2 country code.
 * - `phone-country-code`: DIN phone country code.
 * - `provider-id`: provider id of a contract id, party code of a party id.
 * - `operator-id`: operator id of an EVSE id (ISO or DIN).
 * - `id-type`: ISO EVSE id without the `E` id type in front of the power outlet id.
 * - `power-outlet-id`: power outlet id of an EVSE id.
 * - `instance`: instance value of a contract id.
 * - `check-digit`: check digit of a contract id differs from the computed one.
 * - `unconvertible`: a contract id cannot be converted to the requested standard.
 */
export type ParseFailureReason =
  | "format"
  | "country-code"
  | "phone-country-code"
  | "provider-id"
  | "operator-id"
  | "id-type"
  | "power-outlet-id"
  | "instance"
  | "check-digit"
  | "unconvertible";

/**
 * Thrown by the strict factories and parsers when the input is not a valid identifier. It extends
 * `TypeError` for compatibility with callers that catch `TypeError`; `attempt` captures exactly
 * this class so that a genuine programming `TypeError` (`undefined is not a function`) is never
 * mistaken for an invalid input. `reason` is the code `tryParse` reports.
 */
export class ValidationError extends TypeError {
  public readonly reason: ParseFailureReason;

  public constructor(message: string, reason: ParseFailureReason = "format") {
    super(message);
    this.name = "ValidationError";
    this.reason = reason;
  }
}

/**
 * Outcome of a `tryParse` call: the parsed value, or the message and reason code the strict
 * parser would have thrown. Narrow on `ok`; both variants are frozen like the domain objects.
 */
export type ParseResult<T> =
  | Readonly<{ ok: true; value: T }>
  | Readonly<{ ok: false; error: string; reason: ParseFailureReason }>;

export function success<T>(value: T): ParseResult<T> {
  return Object.freeze({ ok: true, value });
}

export function failure<T>(error: string, reason: ParseFailureReason): ParseResult<T> {
  return Object.freeze({ ok: false, error, reason });
}

/** Collapses a `ParseResult` to the tolerant contract: the value, or `null` on failure. */
export function valueOrNull<T>(result: ParseResult<T>): T | null {
  return result.ok ? result.value : null;
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
      return failure(error.message, error.reason);
    }
    throw error;
  }
}
