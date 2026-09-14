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

type Matrix = Readonly<{ m11: number; m12: number; m21: number; m22: number }>;
type Vec = Readonly<{ v1: number; v2: number }>;

const checkDigitInputRegex = /^[A-Z0-9]+$/;

const matrixKey = (m: Matrix): string => `${m.m11},${m.m12},${m.m21},${m.m22}`;

const multiplyMatrix = (a: Matrix, b: Matrix): Matrix => ({
  m11: a.m11 * b.m11 + a.m12 * b.m21,
  m12: a.m11 * b.m12 + a.m12 * b.m22,
  m21: a.m21 * b.m11 + a.m22 * b.m21,
  m22: a.m21 * b.m12 + a.m22 * b.m22,
});

const addVec = (a: Vec, b: Vec): Vec => ({
  v1: a.v1 + b.v1,
  v2: a.v2 + b.v2,
});

const multiplyVec = (v: Vec, m: Matrix): Vec => ({
  v1: v.v1 * m.m11 + v.v2 * m.m21,
  v2: v.v1 * m.m12 + v.v2 * m.m22,
});

const decodeMatrix = (x: number): Matrix => ({
  m11: x & 1,
  m12: (x >> 1) & 1,
  m21: (x >> 2) & 3,
  m22: x >> 4,
});

const encodingValues: Readonly<Record<string, number>> = {
  "0": 0,
  "1": 16,
  "2": 32,
  "3": 4,
  "4": 20,
  "5": 36,
  "6": 8,
  "7": 24,
  "8": 40,
  "9": 2,
  A: 18,
  B: 34,
  C: 6,
  D: 22,
  E: 38,
  F: 10,
  G: 26,
  H: 42,
  I: 1,
  J: 17,
  K: 33,
  L: 5,
  M: 21,
  N: 37,
  O: 9,
  P: 25,
  Q: 41,
  R: 3,
  S: 19,
  T: 35,
  U: 7,
  V: 23,
  W: 39,
  X: 11,
  Y: 27,
  Z: 43,
};

const encoding = new Map<string, Matrix>();
const decoding = new Map<string, string>();

for (const [char, value] of Object.entries(encodingValues)) {
  const matrix = decodeMatrix(value);
  encoding.set(char, matrix);
  decoding.set(matrixKey(matrix), char);
}

const p1: Matrix = { m11: 0, m12: 1, m21: 1, m22: 1 };
const p2: Matrix = { m11: 0, m12: 1, m21: 1, m22: 2 };

const p1s: Matrix[] = [];
const p2s: Matrix[] = [];

let currentP1 = p1;
let currentP2 = p2;
for (let index = 0; index < 14; index += 1) {
  p1s.push(currentP1);
  p2s.push(currentP2);
  currentP1 = multiplyMatrix(currentP1, p1);
  currentP2 = multiplyMatrix(currentP2, p2);
}

const negP2minus15: Matrix = { m11: 0, m12: 2, m21: 2, m22: 1 };

export function checkDigitIso(code: string): string {
  const normalized = code.toUpperCase();

  if (normalized.length !== p1s.length || normalized.length !== p2s.length) {
    throw new TypeError(`Code must have a length of ${p1s.length}`);
  }

  if (!checkDigitInputRegex.test(normalized)) {
    throw new TypeError("Code must consist of uppercase ASCII letters and digits");
  }

  const sumEq = (matrices: Matrix[], toVec: (matrix: Matrix) => Vec): Vec => {
    let value: Vec = { v1: 0, v2: 0 };

    for (let index = 0; index < matrices.length; index += 1) {
      const character = normalized.charAt(index);
      const matrix = encoding.get(character);
      if (!matrix) {
        throw new TypeError(`Invalid character: ${character}.`);
      }

      const positionMatrix = matrices[index];
      if (!positionMatrix) {
        throw new TypeError(`Missing matrix at index: ${index}.`);
      }

      const qr = toVec(matrix);
      value = addVec(value, multiplyVec(qr, positionMatrix));
    }

    return value;
  };

  const t1 = sumEq(p1s, (matrix) => ({ v1: matrix.m11, v2: matrix.m12 }));
  const t2 = multiplyVec(
    sumEq(p2s, (matrix) => ({ v1: matrix.m21, v2: matrix.m22 })),
    negP2minus15,
  );

  const m15: Matrix = {
    m11: t1.v1 & 1,
    m12: t1.v2 & 1,
    m21: t2.v1 % 3,
    m22: t2.v2 % 3,
  };

  const decoded = decoding.get(matrixKey(m15));
  if (!decoded) {
    throw new TypeError(`Undecodable matrix: ${matrixKey(m15)}.`);
  }

  return decoded;
}
