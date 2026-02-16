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

const values = new Map<string, number>();

for (let index = 0; index <= 9; index += 1) {
  values.set(String(index), index);
}

for (let index = 0; index < 26; index += 1) {
  values.set(String.fromCharCode("A".charCodeAt(0) + index), 10 + index);
}

export function checkDigitDin(code: string): string {
  const normalized = code.toUpperCase();
  const lookup = Array.from(normalized, (char) => {
    const value = values.get(char);
    if (value === undefined) {
      throw new TypeError(
        `invalid character '${char}' in code '${code}'; must consist of uppercase ASCII letters and digits`,
      );
    }

    return value;
  });

  let sum = 0;
  let coefficient = 0;

  for (const current of lookup) {
    if (current < 10) {
      sum += current * 2 ** coefficient;
      coefficient += 1;
      continue;
    }

    sum += Math.floor(current / 10) * 2 ** coefficient + (current % 10) * 2 ** (coefficient + 1);
    coefficient += 2;
  }

  const mod = sum % 11;

  if (mod >= 10) {
    return "X";
  }

  return String(mod);
}
