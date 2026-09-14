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

// Matrix from Scala's MatrixUtil
type isoMatrix struct {
	m11, m12, m21, m22 int
}

// Mul performs matrix multiplication (this * m)
func (a isoMatrix) Mul(b isoMatrix) isoMatrix {
	return isoMatrix{
		m11: a.m11*b.m11 + a.m12*b.m21,
		m12: a.m11*b.m12 + a.m12*b.m22,
		m21: a.m21*b.m11 + a.m22*b.m21,
		m22: a.m21*b.m12 + a.m22*b.m22,
	}
}

// Vec from Scala's MatrixUtil
type isoVec struct {
	v1, v2 int
}

// Add performs vector addition (v + v)
func (a isoVec) Add(b isoVec) isoVec {
	return isoVec{
		v1: a.v1 + b.v1,
		v2: a.v2 + b.v2,
	}
}

// Mul performs vector-matrix multiplication (v * m)
func (a isoVec) Mul(m isoMatrix) isoVec {
	return isoVec{
		v1: a.v1*m.m11 + a.v2*m.m21,
		v2: a.v1*m.m12 + a.v2*m.m22,
	}
}

// Constants from Scala's MatrixUtil
var (
	p1 = isoMatrix{0, 1, 1, 1}
	p2 = isoMatrix{0, 1, 1, 2}

	p1s []isoMatrix
	p2s []isoMatrix

	negP2minus15 = isoMatrix{0, 2, 2, 1}
)

func init() {
	// Initialize p1s and p2s arrays, similar to Scala's Iterator.iterate
	p1s = make([]isoMatrix, 14)
	p2s = make([]isoMatrix, 14)

	currentP1 := p1
	currentP2 := p2
	for i := 0; i < 14; i++ {
		p1s[i] = currentP1
		p2s[i] = currentP2
		currentP1 = currentP1.Mul(p1)
		currentP2 = currentP2.Mul(p2)
	}
}

// Helper function from Scala's `isAsciiUpperOrDigit`
func isASCIIUpperOrDigit(r rune) bool {
	return (r >= 'A' && r <= 'Z') || (r >= '0' && r <= '9')
}

// LookupTables from Scala's LookupTables
var (
	isoEncoding map[rune]isoMatrix
	isoDecoding map[isoMatrix]rune
)

func isoDecode(x int) isoMatrix {
	return isoMatrix{
		m11: (x & 1),
		m12: ((x >> 1) & 1),
		m21: ((x >> 2) & 3),
		m22: (x >> 4),
	}
}

func init() {
	// Build encoding map
	isoEncodingValues := map[rune]int{
		'0': 0, '1': 16, '2': 32,
		'3': 4, '4': 20, '5': 36,
		'6': 8, '7': 24, '8': 40,
		'9': 2, 'A': 18, 'B': 34,
		'C': 6, 'D': 22, 'E': 38,
		'F': 10, 'G': 26, 'H': 42,
		'I': 1, 'J': 17, 'K': 33,
		'L': 5, 'M': 21, 'N': 37,
		'O': 9, 'P': 25, 'Q': 41,
		'R': 3, 'S': 19, 'T': 35,
		'U': 7, 'V': 23, 'W': 39,
		'X': 11, 'Y': 27, 'Z': 43,
	}

	isoEncoding = make(map[rune]isoMatrix)
	isoDecoding = make(map[isoMatrix]rune)

	for char, val := range isoEncodingValues {
		matrix := isoDecode(val)
		isoEncoding[char] = matrix
		isoDecoding[matrix] = char
	}
}
