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

package mobilityid

import (
	"fmt"
	"strings"
)

// CalculateISO7064Mod37_2 computes the ISO 7064 Mod 37, 2 check digit for a given input string.
func CalculateISO7064Mod37_2(code string) (string, error) {
	upperCode := strings.ToUpper(code)

	// Check for character validity
	for _, r := range upperCode {
		if !isASCIIUpperOrDigit(r) {
			return "", fmt.Errorf("invalid character '%c' in code '%s'; must consist of uppercase ASCII letters and digits", r, code)
		}
	}

	if len(upperCode) != len(p1s) {
		return "", fmt.Errorf("code must have a length of %d for ISO 7064 Mod 37, 2 calculation, got %d", len(p1s), len(upperCode))
	}

	sumEq := func(ps []isoMatrix, f func(isoMatrix) isoVec) isoVec {
		v := isoVec{0, 0}
		for i, p := range ps {
			ch := rune(upperCode[i])
			m, ok := isoEncoding[ch]
			if !ok {
				return isoVec{v1: -1, v2: -1} // Indicate error
			}
			qr := f(m)
			v = v.Add(qr.Mul(p))
		}
		return v
	}

	t1 := sumEq(p1s, func(m isoMatrix) isoVec { return isoVec{v1: m.m11, v2: m.m12} })
	t2Sum := sumEq(p2s, func(m isoMatrix) isoVec { return isoVec{v1: m.m21, v2: m.m22} })
	t2 := t2Sum.Mul(negP2minus15)

	if t1.v1 == -1 || t2.v1 == -1 { // Propagate error from sumEq
		return "", fmt.Errorf("internal error during character encoding")
	}

	m15 := isoMatrix{
		m11: t1.v1 & 1,
		m12: t1.v2 & 1,
		m21: t2.v1 % 3,
		m22: t2.v2 % 3,
	}

	char, ok := isoDecoding[m15]
	if !ok {
		return "", fmt.Errorf("undecodable matrix for check digit: %+v", m15)
	}

	return string(char), nil
}

// VerifyISO7064Mod37_2 verifies an input string against its ISO 7064 Mod 37, 2 check digit.
// This function assumes the input string *includes* the check digit at the end.
func VerifyISO7064Mod37_2(codeWithCD string) bool {
	if len(codeWithCD) < 1 {
		return false
	}
	normalized := strings.ToUpper(codeWithCD)
	codeWithoutCD := normalized[:len(normalized)-1]
	expectedCD, err := CalculateISO7064Mod37_2(codeWithoutCD)
	if err != nil {
		return false // Cannot calculate check digit, so cannot verify
	}
	return expectedCD == string(normalized[len(normalized)-1])
}
