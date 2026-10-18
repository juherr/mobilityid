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
 * Uppercases ASCII letters only. `String.prototype.toUpperCase` also maps some non-ASCII letters
 * to ASCII ones ('ſ' U+017F to 'S', 'ı' U+0131 to 'I'), which would let the ASCII-only patterns
 * accept them; every factory and parser normalizes through this function instead, like the
 * other ports.
 */
export function asciiUpperCase(value: string): string {
  return value.replace(/[a-z]+/g, (letters) => letters.toUpperCase());
}
