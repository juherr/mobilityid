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
	"strconv"
	"strings"
)

var dinToNumericValue map[rune]int

func init() {
	dinToNumericValue = make(map[rune]int)
	// '0' to '9'
	for i := 0; i <= 9; i++ {
		dinToNumericValue[rune('0'+i)] = i
	}
	// 'A' to 'Z'
	for i := 0; i <= 25; i++ {
		dinToNumericValue[rune('A'+i)] = 10 + i
	}
}

// CalculateDIN7064ModXY computes a DIN 7064 Mod X, Y check digit for a given input string.
func CalculateDIN7064ModXY(code string) (string, error) {
	upperCode := strings.ToUpper(code)

	// Based on Scala's implementation, it expects only ASCII uppercase and digits.
	// The Scala test CheckDigitDin("INTNM" + "%06d".format(instance)) also implies this.
	for _, r := range upperCode {
		if _, ok := dinToNumericValue[r]; !ok {
			return "", fmt.Errorf("invalid character '%c' in code '%s'; must consist of uppercase ASCII letters and digits", r, code)
		}
	}

	// Scala's 'go' function
	var goDin func(rest []int, acc int, coefficient int) int
	goDin = func(rest []int, acc int, coefficient int) int {
		if len(rest) == 0 {
			return acc
		}
		current := rest[0]
		var stepResult int
		var newCoefficient int

		if current < 10 {
			stepResult = current * (1 << coefficient) // current * 2^coefficient
			newCoefficient = coefficient + 1
		} else {
			stepResult = (current/10)*(1<<coefficient) + (current%10)*(1<<(coefficient+1)) // (current/10)*2^coeff + (current%10)*2^(coeff+1)
			newCoefficient = coefficient + 2
		}
		return goDin(rest[1:], acc+stepResult, newCoefficient)
	}

	lookupResults := make([]int, len(upperCode))
	for i, r := range upperCode {
		lookupResults[i] = dinToNumericValue[r]
	}

	sum := goDin(lookupResults, 0, 0)
	mod := sum % 11

	if mod >= 10 {
		return "X", nil
	}
	return strconv.Itoa(mod), nil
}

// VerifyDIN7064ModXY verifies an input string against its DIN 7064 Mod X, Y check digit.
// This function assumes the input string *includes* the check digit at the end.
func VerifyDIN7064ModXY(codeWithCD string) bool {
	if len(codeWithCD) < 1 {
		return false
	}
	normalized := strings.ToUpper(codeWithCD)
	codeWithoutCD := normalized[:len(normalized)-1]
	expectedCD, err := CalculateDIN7064ModXY(codeWithoutCD)
	if err != nil {
		return false // Cannot calculate check digit, so cannot verify
	}
	return expectedCD == string(normalized[len(normalized)-1])
}
